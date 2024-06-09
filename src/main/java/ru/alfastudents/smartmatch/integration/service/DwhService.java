package ru.alfastudents.smartmatch.integration.service;

import ru.alfastudents.smartmatch.integration.model.Client;

import java.util.List;

public interface DwhService {

    List<Client> getClientsForAutoAsign();

}
