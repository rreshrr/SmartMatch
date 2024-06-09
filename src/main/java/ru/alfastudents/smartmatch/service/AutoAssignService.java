package ru.alfastudents.smartmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alfastudents.smartmatch.integration.model.Client;
import ru.alfastudents.smartmatch.integration.model.Manager;
import ru.alfastudents.smartmatch.entity.AutoAssignCase;
import ru.alfastudents.smartmatch.integration.service.simulator.DwhSimulator;
import ru.alfastudents.smartmatch.integration.service.simulator.MdmSimulator;
import ru.alfastudents.smartmatch.integration.service.simulator.SapSimulator;
import ru.alfastudents.smartmatch.repository.AutoAssignCaseRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class AutoAssignService {

    @Autowired
    private final DwhSimulator dwhService;

    @Autowired
    private final MdmSimulator mdmService;

    @Autowired
    private final SapSimulator sapService;

    @Autowired
    private final AutoAssignCaseRepository autoAssignCaseRepository;

    @Autowired
    private final EmailService emailService;

    public List<Client> getClientsForAssignFromDwh(){
        //симулируем обращение к DWH за клиентами
        return dwhService.getClientsForAutoAsign();
    }

    public void updateManagerFromSap(Manager manager){
        //симулируем обращение к SAP за активностью менеджера
        sapService.updateManager(manager);
    }

    public List<Manager> getManagersForAssignByRegionAndTypeFromMdm(String region, String type){
        //симулируем обращение к MDM за менеджерами по региону и типу клиента
        return mdmService.findManagersByRegionAndType(region, type);
    }

    public void assignClientToManager(Client client, Manager manager){
        System.out.println("Assign client " + client.getId() + " for manager " + manager.getId());
        AutoAssignCase autoAssignCase = new AutoAssignCase(client, manager);
        autoAssignCaseRepository.save(autoAssignCase);
        //здесь также должна быть отправка в MDM уведомления о созданной связи
    }

    public void fixErrorOnAssignProcess(Client client, String errorInfo){
        AutoAssignCase autoAssignCase = new AutoAssignCase(client, null, errorInfo);
        autoAssignCaseRepository.save(autoAssignCase);
    }

    public String validateClient(Client client){
        String result = null;
        if (isStringNullOrEmpty(client.getRegion())){
            result = "Нет региона";
        } else if (isStringNullOrEmpty(client.getGrade())) {
            result = "Нет грейда";
        } else if (isStringNullOrEmpty(client.getType())) {
            result = "Не задан тип клиента";
        }
        return result;
    }

    private Boolean isStringNullOrEmpty(String str){
        return (str == null) || str.equals("-");        //"-" для простоты парсинга файла CSV. надо убрать
    }

    public void sendEmailNotifications(){
        List<Manager> managers = mdmService.findManagersByIds(autoAssignCaseRepository.findManagerIdByCreatedAtToday());

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (Manager manager : managers) {
            executorService.submit(() -> sendEmailNotification(manager));
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    private void sendEmailNotification(Manager manager){
        List<String> clientIds = autoAssignCaseRepository.findClientIdByManagerIdAndCreatedAtToday(manager.getId());
        StringBuilder emailText = new StringBuilder("Здравствуйте! На вас были закреплены следующие клиенты:\n");
        for (String clientId : clientIds) {
            emailText.append(clientId).append("\n");
        }
        emailService.sendMessage(manager.getEmail(), "Новые клиенты", emailText.toString());
    }

    public Boolean isSameGrade(Manager manager, Client client){
        return manager.getGrade().equalsIgnoreCase(client.getGrade());
    }

    public int getManagerActualClientCount(Manager manager){
        int actualClientCount = manager.getClientCount();
        return actualClientCount + autoAssignCaseRepository.countSuccesfullByManagerIdAndCreatedAtToday(manager.getId());
    }
}
