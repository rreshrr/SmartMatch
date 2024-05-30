package ru.alfastudents.smartmatch;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Scanner;

@Component
public class SapHelper {

    public void updateManager(Manager manager){
        String[] values = getAdditionalManagerInfo(manager.getId());
        manager.setIsSick(Boolean.valueOf(values[1]));
        manager.setIsVacation(Boolean.valueOf(values[1]));
    }

    private String[] getAdditionalManagerInfo(String managerId){
        String filename = "/sap_managers.csv";
        String[] result = null;
        try (InputStream inputStream = getClass().getResourceAsStream(filename);
             Scanner scanner = new Scanner(inputStream)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + filename);
            }
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(";");
                if (values[0].equals(managerId))
                    result = values;
            }
        } catch (Exception e) {
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
        }
        return result;
    }

}
