package ru.alfastudents.smartmatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.alfastudents.smartmatch.entity.AutoAssignCase;
import ru.alfastudents.smartmatch.integration.model.Client;
import ru.alfastudents.smartmatch.integration.model.Manager;
import ru.alfastudents.smartmatch.integration.service.DwhService;
import ru.alfastudents.smartmatch.integration.service.MdmService;
import ru.alfastudents.smartmatch.integration.service.SapService;
import ru.alfastudents.smartmatch.repository.AutoAssignCaseRepository;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AutoAssignServiceTest {

    @MockBean
    @Qualifier("dwhService")
    private DwhService dwhService;

    @MockBean
    @Qualifier("mdmService")
    private MdmService mdmService;

    @MockBean
    @Qualifier("sapService")
    private SapService sapService;

    @MockBean
    private EmailService emailService;

    @Autowired
    private AutoAssignService autoAssignService;

    @Autowired
    private AutoAssignCaseRepository autoAssignCaseRepository;

    @BeforeEach
    public void setUp(){
        autoAssignCaseRepository.deleteAll();
    }

    @Order(1)
    @Test
    void getClientsForAssignFromDwhTest() {
        List<Client> clients = List.of(
            new Client("AAAAAA", "Ordinary", "Digital", "Москва", "Лебедев А. Д."),
            new Client("AAAAAB", "Ordinary", "Figital", "Новосибирск", "Захаров П. С."),
            new Client("AAAAAC", "Major", "Digital", "Новокузнецк", "Смирнов И. Г."),
            new Client("AAAAAD", "Major", "Figital", "Липецк", "Тихонов В. Л.")
        );

        when(dwhService.getClientsForAutoAsign()).thenReturn(clients);

        List<Client> actualClients = autoAssignService.getClientsForAssignFromDwh();
        assertNotNull(actualClients);
        assertArrayEquals(clients, actualClients);
    }

    @Order(2)
    @Test
    void updateManagerFromSapTest() {
        Manager manager = new Manager("RUMATI", "Гусев О. К.", "Figital", "Новосибирск", false,
                30, "Ordinary", "vasilev.v03@mail.ru");
        boolean isSick = true;
        boolean isVacation = false;

        doAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            managerArg.setIsSick(isSick);
            managerArg.setIsVacation(isVacation);
            return null;
        }).when(sapService).updateManager(any(Manager.class));

        autoAssignService.updateManagerFromSap(manager);

        assertEquals(isSick, manager.getIsSick());
        assertEquals(isVacation, manager.getIsVacation());
    }

    @Order(3)
    @Test
    void getManagersForAssignByRegionAndTypeFromMdmTest() {
        List<Manager> managers = List.of(
                new Manager("RUMATI", "Гусев О. К.", "Figital", "Москва", false,
                        30, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMATY", "Уткин О. К.", "Figital", "Москва", false,
                        30, "Ordinary", "vasilev.v03@mail.ru")
        );

        when(mdmService.findManagersByRegionAndType(any(), any())).thenReturn(managers);

        List<Manager> actualManagers = autoAssignService.getManagersForAssignByRegionAndTypeFromMdm("Moscow", "Digital");

        assertNotNull(actualManagers);
        assertArrayEquals(managers, actualManagers);
    }

    @Order(4)
    @Test
    void assignClientToManagerTest() {
        Client client = new Client("AAAAAA", "Ordinary", "Digital", "Москва", "Лебедев А. Д.");
        Manager manager = new Manager("RUMATI", "Гусев О. К.", "Figital", "Новосибирск", false,
                30, "Ordinary", "vasilev.v03@mail.ru");

        autoAssignService.assignClientToManager(client, manager);

        assertEquals(1, autoAssignCaseRepository.count());
        AutoAssignCase autoAssignCase = autoAssignCaseRepository.findFirstByClientId(client.getId()).orElseThrow();
        assertEquals(manager.getId(), autoAssignCase.getManagerId());
        assertEquals(client.getId(), autoAssignCase.getClientId());
        assertEquals(client.getName(), autoAssignCase.getClientName());
        assertEquals(manager.getName(), autoAssignCase.getManagerName());
        assertEquals(client.getType(), autoAssignCase.getClientType());
        assertEquals(client.getRegion(), autoAssignCase.getClientRegion());
        assertNull(autoAssignCase.getErrorInfo());
    }

    @Order(5)
    @Test
    void fixErrorOnAssignProcessTest() {
        Client client = new Client("AAAAAA", "Ordinary", "Digital", "Москва", "Лебедев А. Д.");
        String errorText = "error for test";

        autoAssignService.fixErrorOnAssignProcess(client, errorText);

        assertEquals(1, autoAssignCaseRepository.count());
        AutoAssignCase autoAssignCase = autoAssignCaseRepository.findFirstByClientId(client.getId()).orElseThrow();
        assertNull(autoAssignCase.getManagerId());
        assertEquals(client.getId(), autoAssignCase.getClientId());
        assertEquals(client.getName(), autoAssignCase.getClientName());
        assertNull(autoAssignCase.getManagerName());
        assertEquals(client.getType(), autoAssignCase.getClientType());
        assertEquals(client.getRegion(), autoAssignCase.getClientRegion());
        assertEquals(errorText, autoAssignCase.getErrorInfo());
    }

    @Order(6)
    @Test
    void validateClientTest() {
        Client clientWithoutType = new Client("AAAAAA", "Ordinary", null, "Москва", "Лебедев А. Д.");
        Client clientWithoutGrade = new Client("AAAAAB", null, "Figital", "Новосибирск", "Захаров П. С.");
        Client clientWithoutRegion = new Client("AAAAAC", "Major", "Digital", null, "Смирнов И. Г.");
        Client clientGood =  new Client("AAAAAD", "Major", "Figital", "Липецк", "Тихонов В. Л.");
        String emptyTypeError = "Не задан тип клиента";
        String emptyGradeError = "Нет грейда";
        String emptyRegionError = "Нет региона";

        assertEquals(emptyTypeError, autoAssignService.validateClient(clientWithoutType));
        assertEquals(emptyGradeError, autoAssignService.validateClient(clientWithoutGrade));
        assertEquals(emptyRegionError, autoAssignService.validateClient(clientWithoutRegion));
        assertNull(autoAssignService.validateClient(clientGood));
    }

    @Order(7)
    @Test
    void sendEmailNotificationsTest() {
        Client client = new Client("AAAAAA", "Ordinary", "Digital", "Москва", "Лебедев А. Д.");

        Manager manager = new Manager("RUMATI", "Гусев О. К.", "Figital", "Новосибирск", false,
                30, "Ordinary", "vasilev.v03@mail.ru");

        doReturn(Collections.singletonList(manager))
                .when(mdmService).findManagersByIds(any());
        autoAssignService.assignClientToManager(client, manager);

        autoAssignService.sendEmailNotifications();

        verify(emailService).sendMessage(eq(manager.getEmail()), eq("Новые клиенты"), eq("Здравствуйте! На вас были закреплены следующие клиенты:\n" + client.getId() + "\n"));
    }

    @Order(8)
    @Test
    void getManagerActualClientCount() {
        Manager manager = new Manager("RUMATI", "Гусев О. К.", "Figital", "Новосибирск", false,
                30, "Ordinary", "vasilev.v03@mail.ru");

        autoAssignService.assignClientToManager(new Client("AAAAAA", "Ordinary", "Digital", "Москва", "Лебедев А. Д."), manager);
        autoAssignService.assignClientToManager(new Client("AAAAAB", "Ordinary", "Digital", "Москва", "Лебедева Ж. А."), manager);

        assertEquals(32, autoAssignService.getManagerActualClientCount(manager));;
    }

    private <T> void assertArrayEquals(List<T> expected, List<T> actual) {
        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }
}