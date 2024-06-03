package ru.alfastudents.smartmatch.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.alfastudents.smartmatch.dto.Manager;
import java.util.Scanner;

@Service
public class SapHelper extends BaseHelper {

    @Value("${app.path-to-csv}/sap_managers.csv")
    private String defaultFilePath;

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

    @Override
    protected Scanner getScannerWithCsvContent() {
        if (resourceFilePath == null){
            return getScannerFromFileCsv(defaultFilePath);
        } else {
            return getScannerFromResourceFileCsv(resourceFilePath);
        }
    }

}
