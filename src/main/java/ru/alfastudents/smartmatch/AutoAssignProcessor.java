package ru.alfastudents.smartmatch;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.alfastudents.smartmatch.helper.DwhHelper;
import ru.alfastudents.smartmatch.helper.MdmHelper;
import ru.alfastudents.smartmatch.helper.SapHelper;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AutoAssignProcessor {

    @Autowired
    private final DwhHelper dwhHelper;

    @Autowired
    private final MdmHelper mdmHelper;

    @Autowired
    private final SapHelper sapHelper;

    @Autowired
    private final AutoAssignCaseRepository autoAssignCaseRepository;

    @Scheduled(fixedRate=300000)
    public void run(){
        List<Client> clients = dwhHelper.getClientsForAutoAsignee();

        for (Client client : clients) {

            int penalty = 0;
            int minLoad = 0;

            List<Manager> managers = mdmHelper.findManagersByRegionAndType(client.getRegion(), client.getType());
            Manager targetManager = null;
            Manager headManager = null;
            for (Manager manager : managers) {

                if (manager.getIsHead()){
                    headManager = manager;
                    continue;
                }

                if (client.isMajor() && manager.isOrdinary()){
                    continue;
                }

                sapHelper.updateManager(manager);

                if (manager.getIsSick() || manager.getIsVacation()){
                    continue;
                }

                if (targetManager == null) {
                    targetManager = manager;
                    if (client.isOrdinary() && manager.isMajor()){
                        penalty = 1;
                    }
                    minLoad = manager.getClientCount();
                } else {
                    if (penalty != 0 && isSameGrade(manager, client)){
                       targetManager = manager;
                       penalty = 0;
                       minLoad = manager.getClientCount();
                       continue;
                    }
                    if (manager.getClientCount() < minLoad){
                        targetManager = manager;
                        minLoad = manager.getClientCount();
                    }
                }

            }

            if (targetManager == null){
                targetManager = headManager;
            }
            if (targetManager != null) {
                assign(targetManager, client);
            } else {
                System.err.println("Cannot find manager for client " + client.getId());
            }
        }
    }

    private Boolean isSameGrade(Manager manager, Client client){
        return manager.getGrade().equals(client.getGrade());
    }

    private void assign(Manager manager, Client client){
        AutoAssignCase autoAssignCase = new AutoAssignCase(client.getId(), manager.getId());
        autoAssignCaseRepository.save(autoAssignCase);
    }

}
