package ru.alfastudents.smartmatch.integration.service.simulator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.alfastudents.smartmatch.integration.model.Client;
import ru.alfastudents.smartmatch.integration.service.DwhService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class DwhSimulator extends BaseSimulator implements DwhService {

    @Value("${app.path-to-csv}/dwh_clients.csv")
    private void setAbsoluteFilePath(String absoluteFilePath){
        this.absoluteFilePath = absoluteFilePath;
    }

    @Override
    public List<Client> getClientsForAutoAsign() {

        List<Client> clients = new ArrayList<>();

        Scanner scanner = getScannerWithCsvContent();

        while (scanner.hasNextLine()) {
            clients.add(getClientFromLine(scanner.nextLine()));
        }

        scanner.close();
        return clients;
    }

    private Client getClientFromLine(String line) {
        String[] values = line.split(COMMA_DELIMITER);
        return new Client(values[0], values[1], values[2], values[3], values[4]);
    }
}
