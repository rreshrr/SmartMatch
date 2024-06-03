package ru.alfastudents.smartmatch.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.alfastudents.smartmatch.dto.Manager;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class MdmHelper extends BaseHelper {

    @Value("${app.path-to-csv}/mdm_managers.csv")
    private String defaultFilePath;

    public List<Manager> findManagersByRegionAndType(String region, String type) {
        List<Manager> managers = new ArrayList<>();

        Scanner scanner = getScannerWithCsvContent();

        while (scanner.hasNextLine()) {
            Manager tempManager = getManagerFromLine(scanner.nextLine());
            if (tempManager.getRegion().equalsIgnoreCase(region) && tempManager.getType().equalsIgnoreCase(type))
                managers.add(tempManager);
        }
        scanner.close();
        return managers;
    }

    public List<Manager> findManagersByIds(List<String> ids){
        List<Manager> managers = new ArrayList<>();

        Scanner scanner = getScannerWithCsvContent();

        while (scanner.hasNextLine()) {
            Manager tempManager = getManagerFromLine(scanner.nextLine());
            if (ids.contains(tempManager.getId())){
                managers.add(tempManager);
            }
        }
        scanner.close();
        return managers;
    }

    private Manager getManagerFromLine(String line) {
        String[] values = line.split(COMMA_DELIMITER);
        return new Manager(values[0], values[1], values[2], Boolean.valueOf(values[3]), Integer.valueOf(values[4]), values[5], values[6]);
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
