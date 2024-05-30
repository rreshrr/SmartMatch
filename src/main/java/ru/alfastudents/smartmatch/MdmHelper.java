package ru.alfastudents.smartmatch;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

@Component
public class MdmHelper {

    private final String COMMA_DELIMITER = ";";

    public List<Manager> findManagersByRegionAndType(String region, String type) {
        List<Manager> managers = new ArrayList<>();
        String filename = "/mdm_managers.csv";
        try (InputStream inputStream = getClass().getResourceAsStream(filename);
             Scanner scanner = new Scanner(inputStream)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + filename);
            }
            while (scanner.hasNextLine()) {
                Manager tempManager = getManagerFromLine(scanner.nextLine());
                if (tempManager.getRegion().equals(region) && tempManager.getType().equals(type))
                    managers.add(tempManager);
            }
        } catch (Exception e) {
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
        }
        return managers;
    }

    private Manager getManagerFromLine(String line) {
        String[] values = line.split(COMMA_DELIMITER);
        return new Manager(values[0], values[1], values[2], Boolean.valueOf(values[3]), Integer.valueOf(values[4]), values[5], values[6]);
    }
}
