package ru.alfastudents.smartmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alfastudents.smartmatch.dto.Client;
import ru.alfastudents.smartmatch.dto.Manager;
import ru.alfastudents.smartmatch.entity.AutoAssignCase;
import ru.alfastudents.smartmatch.helper.DwhHelper;
import ru.alfastudents.smartmatch.helper.MdmHelper;
import ru.alfastudents.smartmatch.helper.SapHelper;
import ru.alfastudents.smartmatch.repository.AutoAssignCaseRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class AutoAssignService {
    @Autowired
    private final DwhHelper dwhHelper;

    @Autowired
    private final MdmHelper mdmHelper;

    @Autowired
    private final SapHelper sapHelper;

    @Autowired
    private final AutoAssignCaseRepository autoAssignCaseRepository;

    @Autowired
    private final EmailService emailService;

    public List<Client> getClientsForAssignFromDwh(){
        //симулируем обращение к DWH за клиентами
        return dwhHelper.getClientsForAutoAsignee();
    }

    public void updateManagerFromSap(Manager manager){
        //симулируем обращение к SAP за активностью менеджера
        sapHelper.updateManager(manager);
    }

    public List<Manager> getManagersForAssignByRegionAndTypeFromMdm(String region, String type){
        //симулируем обращение к MDM за менеджерами по региону и типу клиента
        return mdmHelper.findManagersByRegionAndType(region, type);
    }

    public void assignClientToManager(Client client, Manager manager){
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
        return (str == null) || str.equals("-");
    }

    public void sendEmailNotifications(){
        List<Manager> managers = mdmHelper.findManagersByIds(autoAssignCaseRepository.findManagerIdByCreatedAtToday());

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
}
