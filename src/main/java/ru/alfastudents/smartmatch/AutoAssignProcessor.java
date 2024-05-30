package ru.alfastudents.smartmatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutoAssignProcessor {

    @Autowired
    private final DwhHelper dwhHelper;

    @Autowired
    private final MdmHelper mdmHelper;

    @Autowired
    private final SapHelper sapHelper;

    @Scheduled(fixedRate=300000)
    public void run(){
        List<Client> clients = dwhHelper.getClientsForAutoAsignee();

        for (Client client : clients) {

            List<Manager> managers = mdmHelper.findManagersByRegionAndType(client.getRegion(), client.getType());

            for (Manager manager : managers) {

                sapHelper.updateManager(manager);

                if (manager.getIsSick() || manager.getIsVacation()){
                    continue;
                }

                if (client.isOrdinary()) {

                }

            }

        }
    }
}
