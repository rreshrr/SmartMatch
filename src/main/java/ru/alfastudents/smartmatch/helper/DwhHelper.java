package ru.alfastudents.smartmatch.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.alfastudents.smartmatch.dto.Client;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class DwhHelper extends BaseHelper {

    @Value("${app.path-to-csv}/dwh_clients.csv")
    private String defaultFilePath;

    public List<Client> getClientsForAutoAsignee() {

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

    @Override
    protected Scanner getScannerWithCsvContent() {
        if (resourceFilePath == null){
            return getScannerFromFileCsv(defaultFilePath);
        } else {
            return getScannerFromResourceFileCsv(resourceFilePath);
        }
    }
}
