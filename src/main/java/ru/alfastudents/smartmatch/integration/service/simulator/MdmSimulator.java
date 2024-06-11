package ru.alfastudents.smartmatch.integration.service.simulator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.alfastudents.smartmatch.integration.model.Manager;
import ru.alfastudents.smartmatch.integration.service.MdmService;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class MdmSimulator extends BaseSimulator implements MdmService {

    @Value("${app.path-to-csv}/mdm_managers.csv")
    private void setFilePath(String absoluteFilePath){
        this.filePath = absoluteFilePath;
    }

    @Override
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

    @Override
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
        return new Manager(values[0], values[1],  values[2], values[3], Boolean.valueOf(values[4]), Integer.valueOf(values[5]), values[6], values[7]);
    }
}
