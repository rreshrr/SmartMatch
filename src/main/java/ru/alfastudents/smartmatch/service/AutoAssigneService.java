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

@RequiredArgsConstructor
@Service
public class AutoAssigneService {
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
        return dwhHelper.getClientsForAutoAsignee();
    }

    public void updateManagerFromSap(Manager manager){
        sapHelper.updateManager(manager);
    }

    public List<Manager> getManagersForAssignByRegionAndTypeFromMdm(String region, String type){
        return mdmHelper.findManagersByRegionAndType(region, type);
    }

    public void assign(Manager manager, Client client){
        AutoAssignCase autoAssignCase = new AutoAssignCase(client.getId(), manager.getId());
        autoAssignCaseRepository.save(autoAssignCase);
    }

    public void sendEmailNotifications(){
        List<Manager> managers = mdmHelper.findManagersByIds(autoAssignCaseRepository.findManagerIdByCreatedAtToday());
        for (Manager manager : managers) {
            List<String> clientIds = autoAssignCaseRepository.findClientIdByManagerIdAndCreatedAtToday(manager.getId());
            StringBuilder emailText = new StringBuilder("Здравствуйте! На вас были закреплены следующие клиенты:\n");
            for (String clientId : clientIds) {
                emailText.append(clientId).append("\n");
            }
            emailService.sendMessage(manager.getEmail(), "Новые клиенты", emailText.toString());
        }
    }

    public Boolean isSameGrade(Manager manager, Client client){
        return manager.getGrade().equals(client.getGrade());
    }
}
