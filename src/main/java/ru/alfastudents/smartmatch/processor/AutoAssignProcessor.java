package ru.alfastudents.smartmatch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.alfastudents.smartmatch.dto.Client;
import ru.alfastudents.smartmatch.dto.Manager;
import ru.alfastudents.smartmatch.service.AutoAssignService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class AutoAssignProcessor {

    @Autowired
    private final AutoAssignService autoAssignService;

    @Scheduled(cron = "0 0 21 * * *") // Каждый день в 21:00
    public void process() {
        System.out.println("AutoAssignProcess started");
        // Получаем клиентов для назначения из DWH
        List<Client> clients = autoAssignService.getClientsForAssignFromDwh();

        // Создаем пул потоков
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // Обрабатываем каждого клиента в отдельном потоке
        for (Client client : clients) {
            executorService.submit(() -> processClient(client));
        }

        // Завершаем работу ExecutorService
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        System.out.println("AutoAssignProcess finished");

        // Отправляем уведомления по электронной почте
        autoAssignService.sendEmailNotifications();

        System.out.println("Notification sended");
    }

    private void processClient(Client client) {
        int penalty = 0;
        int minLoad = 0;
        Manager targetManager = null;
        Manager headManager = null;

        String error = autoAssignService.validateClient(client);

        if (error != null) {
            autoAssignService.fixErrorOnAssignProcess(client, error);
            return;
        }

        // Получаем список менеджеров для назначения по региону и типу клиента из MDM
        List<Manager> managers = autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(client.getRegion(), client.getType());

        // Проходимся по каждому менеджеру
        for (Manager manager : managers) {
            // Главного менеджера запоминаем, но пока пропускаем
            if (manager.getIsHead()) {
                headManager = manager;
                continue;
            }

            // Пропускаем менеджера, если клиент Major, а менеджер Ordinary
            if (client.isMajor() && manager.isOrdinary()) {
                continue;
            }

            // Обновляем данные менеджера из SAP
            autoAssignService.updateManagerFromSap(manager);

            // Пропускаем менеджера, если он болеет или в отпуске
            if (manager.getIsSick() || manager.getIsVacation()) {
                continue;
            }

            // Находим подходящего менеджера
            if (targetManager == null) {
                targetManager = manager;
                if (client.isOrdinary() && manager.isMajor()) {
                    penalty = 1;
                }
                minLoad = manager.getClientCount();
            } else {
                if (penalty != 0 && autoAssignService.isSameGrade(manager, client)) {
                    targetManager = manager;
                    penalty = 0;
                    minLoad = manager.getClientCount();
                    continue;
                }

                // Выбираем менеджера, если он имеет меньше клиентов
                if (manager.getClientCount() < minLoad) {
                    targetManager = manager;
                    minLoad = manager.getClientCount();
                }
            }
        }

        // Назначаем клиента менеджеру
        if (targetManager != null) {
            autoAssignService.assignClientToManager(client, targetManager);
        } else if (headManager != null) {
            autoAssignService.assignClientToManager(client, headManager);
        } else {
            autoAssignService.fixErrorOnAssignProcess(client, "Нет менеджеров");
        }
    }
}
