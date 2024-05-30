package ru.alfastudents.smartmatch;

import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

@Component
public class DwhHelper {

    private final String COMMA_DELIMITER = ";";

    public List<Client> getClientsForAutoAsignee() {
        List<Client> clients = new ArrayList<>();
        String filename = "/dwh_clients.csv";
        try (InputStream inputStream = getClass().getResourceAsStream(filename);
             Scanner scanner = new Scanner(inputStream)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + filename);
            }
            while (scanner.hasNextLine()) {
                clients.add(getClientFromLine(scanner.nextLine()));
            }
        } catch (Exception e) {
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
        }
        return clients;
    }

    private Client getClientFromLine(String line) {
        String[] values = line.split(COMMA_DELIMITER);
        return new Client(UUID.randomUUID(), values[1], values[2], values[3]);
    }

}
