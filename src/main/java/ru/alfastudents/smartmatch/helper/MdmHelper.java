package ru.alfastudents.smartmatch.helper;

import org.springframework.stereotype.Component;
import ru.alfastudents.smartmatch.Manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class MdmHelper extends BaseHelper {

    public List<Manager> findManagersByRegionAndType(String region, String type) {
        List<Manager> managers = new ArrayList<>();
        String filename = "/mdm_managers.csv";

        Scanner scanner = getScannerFromFileCsv(filename);

        while (scanner.hasNextLine()) {
            Manager tempManager = getManagerFromLine(scanner.nextLine());
            if (tempManager.getRegion().equals(region) && tempManager.getType().equals(type))
                managers.add(tempManager);
        }

        return managers;
    }

    private Manager getManagerFromLine(String line) {
        String[] values = line.split(COMMA_DELIMITER);
        return new Manager(values[0], values[1], values[2], Boolean.valueOf(values[3]), Integer.valueOf(values[4]), values[5], values[6]);
    }
}
