package ru.alfastudents.smartmatch.integration.service.simulator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.alfastudents.smartmatch.integration.model.Manager;
import ru.alfastudents.smartmatch.integration.service.SapService;
import java.util.Scanner;

@Service
public class SapSimulator extends BaseSimulator implements SapService {

    @Value("${app.path-to-csv}/sap_managers.csv")
    private void setFilePath(String absoluteFilePath){
        this.filePath = absoluteFilePath;
    }

    @Override
    public void updateManager(Manager manager){
        String[] values = getAdditionalManagerInfo(manager.getId());
        manager.setIsVacation(Boolean.valueOf(values[1]));
        manager.setIsSick(Boolean.valueOf(values[2]));
    }

    private String[] getAdditionalManagerInfo(String managerId){

        String[] result = null;

        Scanner scanner = getScannerWithCsvContent();

        while (scanner.hasNextLine()) {
            String[] values = scanner.nextLine().split(COMMA_DELIMITER);
            if (values[0].equals(managerId))
                result = values;
        }
        scanner.close();
        return result;
    }
}
