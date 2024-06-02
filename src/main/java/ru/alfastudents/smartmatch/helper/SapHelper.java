package ru.alfastudents.smartmatch.helper;

import org.springframework.stereotype.Component;
import ru.alfastudents.smartmatch.Manager;

import java.util.Scanner;

@Component
public class SapHelper extends BaseHelper {

    public void updateManager(Manager manager){
        String[] values = getAdditionalManagerInfo(manager.getId());
        manager.setIsSick(Boolean.valueOf(values[1]));
        manager.setIsVacation(Boolean.valueOf(values[1]));
    }

    private String[] getAdditionalManagerInfo(String managerId){
        String filename = "/sap_managers.csv";
        String[] result = null;

        Scanner scanner = getScannerFromFileCsv(filename);

        while (scanner.hasNextLine()) {
            String[] values = scanner.nextLine().split(COMMA_DELIMITER);
            if (values[0].equals(managerId))
                result = values;
        }

        return result;
    }

}
