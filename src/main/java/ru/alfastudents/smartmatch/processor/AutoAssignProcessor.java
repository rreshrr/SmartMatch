package ru.alfastudents.smartmatch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.alfastudents.smartmatch.integration.model.Client;
import ru.alfastudents.smartmatch.integration.model.Manager;
import ru.alfastudents.smartmatch.service.AutoAssignService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@RequiredArgsConstructor
@Component
public class AutoAssignProcessor {

    @Autowired
    private final AutoAssignService autoAssignService;

    private final ConcurrentHashMap<String, Lock> managerLocks = new ConcurrentHashMap<>();

    @Scheduled(cron = "0 0 21 * * *") // Каждый день в 21:00
    public void process() {
        System.out.println("AutoAssignProcess started");
        // Получаем клиентов для назначения из DWH
        List<Client> clients = autoAssignService.getClientsForAssignFromDwh();

        // Создаем пул потоков
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // Обрабатываем каждого клиента в отдельном потоке
        for (Client client : clients) {
            executorService.submit(() -> {
                try {
                    processClient(client);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
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

    private void processClient(Client client) throws InterruptedException {
        System.out.println(Thread.currentThread() + ": client "+ client.getId() + " started by ");
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

            // Далее необходимо блокировать менеджера для получения верной информации о его нагрузке
            Lock managerLock = managerLocks.computeIfAbsent(manager.getId(), k -> new ReentrantLock());

            if (managerLock.tryLock(50, TimeUnit.MILLISECONDS)) { //если в теч. 50 мс не получили лок, то пропускаем менеджера - для избежания взаимных блокировок
                try {
                    if (targetManager == null) {
                        targetManager = manager;
                        if (client.isOrdinary() && manager.isMajor()) {
                            penalty = 1;
                        }
                        minLoad = autoAssignService.getManagerActualClientCount(manager);
                    } else {
                        if (penalty != 0 && autoAssignService.isSameGrade(manager, client)) {
                            managerLocks.get(targetManager.getId()).unlock();                   //когда нашли менеджера подходящего больше, чем targetManager, то снимаем лок с targetManager
                            targetManager = manager;
                            penalty = 0;
                            minLoad = autoAssignService.getManagerActualClientCount(manager);
                            continue;
                        }

                        // Выбираем менеджера, если он имеет меньше клиентов
                        if (autoAssignService.getManagerActualClientCount(manager) < minLoad) {
                            managerLocks.get(targetManager.getId()).unlock();                   //когда нашли менеджера подходящего больше, чем targetManager, то снимаем лок с targetManager
                            targetManager = manager;
                            minLoad = autoAssignService.getManagerActualClientCount(manager);
                        }
                    }
                } finally {
                    if (targetManager != manager) {
                        managerLock.unlock();
                    }
                }
            }

        }

        // Назначаем клиента менеджеру
        if (targetManager != null) {
            autoAssignService.assignClientToManager(client, targetManager);
            managerLocks.get(targetManager.getId()).unlock();
        } else if (headManager != null) {
            autoAssignService.assignClientToManager(client, headManager);
        } else {
            autoAssignService.fixErrorOnAssignProcess(client, "Нет менеджеров");
        }

    }
}
