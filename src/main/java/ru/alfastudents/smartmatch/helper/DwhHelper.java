package ru.alfastudents.smartmatch.helper;

import org.springframework.stereotype.Component;
import ru.alfastudents.smartmatch.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class DwhHelper extends BaseHelper {

    public List<Client> getClientsForAutoAsignee() {
        List<Client> clients = new ArrayList<>();
        String filename = "/dwh_clients.csv";

        Scanner scanner = getScannerFromFileCsv(filename);

        while (scanner.hasNextLine()) {
            clients.add(getClientFromLine(scanner.nextLine()));
        }

        return clients;
    }

    private Client getClientFromLine(String line) {
        String[] values = line.split(COMMA_DELIMITER);
        return new Client(values[0], values[1], values[2], values[3]);
    }

}
