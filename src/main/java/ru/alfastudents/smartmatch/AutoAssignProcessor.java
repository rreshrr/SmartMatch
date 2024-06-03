package ru.alfastudents.smartmatch;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.alfastudents.smartmatch.dto.Client;
import ru.alfastudents.smartmatch.dto.Manager;
import ru.alfastudents.smartmatch.service.AutoAssigneService;
import java.util.List;

@RequiredArgsConstructor
@Component
public class AutoAssignProcessor {

    @Autowired
    private final AutoAssigneService autoAssigneService;

    @Scheduled(cron = "0 0 21 * * *") // Каждый день в 21:00
    public void process() {
        System.out.println("AutoAssignProcess started");
        // Получаем клиентов для назначения из DWH
        List<Client> clients = autoAssigneService.getClientsForAssignFromDwh();

        // Обрабатываем каждого клиента
        for (Client client : clients) {
            int penalty = 0;
            int minLoad = 0;
            Manager targetManager = null;
            Manager headManager = null;

            // Получаем список менеджеров для назначения по региону и типу клиента из MDM
            List<Manager> managers = autoAssigneService.getManagersForAssignByRegionAndTypeFromMdm(client.getRegion(), client.getType());

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
                autoAssigneService.updateManagerFromSap(manager);

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
                    if (penalty != 0 && autoAssigneService.isSameGrade(manager, client)) {
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

            // Если не найден подходящий менеджер, используем главу отделения
            if (targetManager == null) {
                targetManager = headManager;
            }

            // Назначаем клиента подходящему менеджеру
            if (targetManager != null) {
                autoAssigneService.assign(targetManager, client);
            } else {
                // Выводим ошибку, если не удалось найти менеджера для клиента
                System.err.println("Cannot find manager for client " + client.getId());
            }
        }

        System.out.println("AutoAssignProcess finished");

        // Отправляем уведомления по электронной почте
        autoAssigneService.sendEmailNotifications();

        System.out.println("Notification sended");
    }
}
