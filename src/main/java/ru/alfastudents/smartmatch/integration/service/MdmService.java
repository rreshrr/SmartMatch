package ru.alfastudents.smartmatch.integration.service;

import ru.alfastudents.smartmatch.integration.model.Manager;
import java.util.List;

public interface MdmService {

    List<Manager> findManagersByRegionAndType(String region, String type);

    List<Manager> findManagersByIds(List<String> ids);
}
