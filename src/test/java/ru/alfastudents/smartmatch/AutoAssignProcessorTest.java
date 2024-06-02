package ru.alfastudents.smartmatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.alfastudents.smartmatch.helper.DwhHelper;
import ru.alfastudents.smartmatch.helper.MdmHelper;
import ru.alfastudents.smartmatch.helper.SapHelper;
import ru.alfastudents.smartmatch.repository.AutoAssignCaseRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AutoAssignProcessorTest {

    @Autowired
    private DwhHelper dwhHelper;

    @Autowired
    private MdmHelper mdmHelper;

    @Autowired
    private SapHelper sapHelper;

    @Autowired
    private AutoAssignProcessor autoAssignProcessor;

    @Autowired
    private AutoAssignCaseRepository autoAssignCaseRepository;

    private final String TEST_RESOURCES_DIRECTORY = "/home/andreyoskin/IdeaProjects/SmartMatch/SmartMatch/src/test/resources";
    @BeforeEach
    public void clearSource(){
        dwhHelper.setSourceFilePath(null);
        mdmHelper.setSourceFilePath(null);
        sapHelper.setSourceFilePath(null);
        autoAssignCaseRepository.deleteAll();
    }

    private void changeSource(String testFilePath){
        dwhHelper.setSourceFilePath(TEST_RESOURCES_DIRECTORY + testFilePath + "dwh_clients.csv");
        mdmHelper.setSourceFilePath(TEST_RESOURCES_DIRECTORY + testFilePath + "mdm_managers.csv");
        sapHelper.setSourceFilePath(TEST_RESOURCES_DIRECTORY + testFilePath + "sap_managers.csv");
    }

    @Test
    public void testBasicAutoAssign(){
        changeSource("/basic-auto-assign/");
        autoAssignProcessor.process();
        assertEquals(4, autoAssignCaseRepository.findAll().stream().count());
    }

}