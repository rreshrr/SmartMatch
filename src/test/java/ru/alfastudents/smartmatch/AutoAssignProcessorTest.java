package ru.alfastudents.smartmatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.alfastudents.smartmatch.helper.DwhHelper;
import ru.alfastudents.smartmatch.helper.MdmHelper;
import ru.alfastudents.smartmatch.helper.SapHelper;
import ru.alfastudents.smartmatch.repository.AutoAssignCaseRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @BeforeEach
    public void clearSource(){
        dwhHelper.setResourceFilePath(null);
        mdmHelper.setResourceFilePath(null);
        sapHelper.setResourceFilePath(null);
        autoAssignCaseRepository.deleteAll();
    }

    private void changeSource(String testFilePath){
        dwhHelper.setResourceFilePath(testFilePath + "/dwh_clients.csv");
        mdmHelper.setResourceFilePath(testFilePath + "/mdm_managers.csv");
        sapHelper.setResourceFilePath(testFilePath + "/sap_managers.csv");
    }

    @Test
    public void testEmailSend(){
        changeSource("/email-send");
        autoAssignProcessor.process();

        assertEquals(4, autoAssignCaseRepository.findAll().size());

        assertEquals("2", autoAssignCaseRepository.findByManagerId("DVDOMGT8").get(0).getClientId());

        assertEquals("3", autoAssignCaseRepository.findByManagerId("DY9HHAU5").get(0).getClientId());

        assertEquals("4", autoAssignCaseRepository.findByManagerId("DY9HHAU5").get(1).getClientId());

        assertEquals("5", autoAssignCaseRepository.findByManagerId("ANDREY").get(0).getClientId());

    }

    @Test
    public void testHappyPath(){
        changeSource("/happy-path");
        autoAssignProcessor.process();

        assertEquals(3, autoAssignCaseRepository.findAll().size());
    }

    @Test
    public void testDifferentGrade(){
        changeSource("/different-grade");
        autoAssignProcessor.process();

        assertEquals(3, autoAssignCaseRepository.findAll().size());
    }

    @Test
    public void testManagerClientLoad(){
        changeSource("/client-load-on-managers");
        autoAssignProcessor.process();

        assertEquals(2, autoAssignCaseRepository.findAll().size());

        assertEquals("1", autoAssignCaseRepository.findByManagerId("3").get(0).getClientId());

        assertEquals("2", autoAssignCaseRepository.findByManagerId("1").get(0).getClientId());
    }

    @Test
    public void testManagerActivity(){
        changeSource("/active-managers");
        autoAssignProcessor.process();

        assertEquals(2, autoAssignCaseRepository.findAll().size());

        assertEquals("1", autoAssignCaseRepository.findByManagerId("2").get(0).getClientId());

        assertEquals("2", autoAssignCaseRepository.findByManagerId("6").get(0).getClientId());
    }

}