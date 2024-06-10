package ru.alfastudents.smartmatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.alfastudents.smartmatch.integration.model.Client;
import ru.alfastudents.smartmatch.integration.model.Manager;
import ru.alfastudents.smartmatch.processor.AutoAssignProcessor;
import ru.alfastudents.smartmatch.service.AutoAssignService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.argThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AutoAssignProcessorTest {

    @MockBean
    private AutoAssignService autoAssignService;

    @Autowired
    private AutoAssignProcessor autoAssignProcessor;

    @Order(1)
    @Test
    public void testHappyPath(){
        List<Client> clients = List.of(
            new Client("AAAAAA", "Ordinary", "Digital", "Москва", "Лебедев А. Д."),
            new Client("AAAAAB", "Ordinary", "Figital", "Новосибирск", "Захаров П. С."),
            new Client("AAAAAC", "Major", "Digital", "Новокузнецк", "Смирнов И. Г."),
            new Client("AAAAAD", "Major", "Figital", "Липецк", "Тихонов В. Л.")
        );

        List<Manager> managers = List.of(
            new Manager("RUMATI", "Гусев О. К.", "Figital", "Новосибирск", false,
                    30, "Ordinary", "vasilev.v03@mail.ru"),
            new Manager("RUMATY", "Уткин О. К.", "Digital", "Новокузнецк", false,
                    50, "Major", "vasilev.v03@mail.ru"),
            new Manager("RUMATA", "Киселев О. К.", "Digital", "Москва", false,
                    0, "Ordinary", "vasilev.v03@mail.ru"),
            new Manager("RUMAAA", "Тимофеев О. К.", "Figital", "Липецк", false,
                        1, "Major", "vasilev.v03@mail.ru")
        );

        //настраиваем поведение autoAssignService
        when(autoAssignService.getClientsForAssignFromDwh()).thenReturn(clients);
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Москва"), eq("Digital")))
                .thenReturn(Collections.singletonList(managers.get(2)));
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Новосибирск"), eq("Figital")))
                .thenReturn(Collections.singletonList(managers.get(0)));
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Липецк"), eq("Figital")))
                .thenReturn(Collections.singletonList(managers.get(3)));
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Новокузнецк"), eq("Digital")))
                .thenReturn(Collections.singletonList(managers.get(1)));

        doAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            managerArg.setIsSick(false);
            managerArg.setIsVacation(false);
            return null;
        }).when(autoAssignService).updateManagerFromSap(any(Manager.class));

        when(autoAssignService.validateClient(any(Client.class))).thenCallRealMethod();
        when(autoAssignService.isSameGrade(any(Manager.class), any(Client.class))).thenCallRealMethod();
        when(autoAssignService.getManagerActualClientCount(any(Manager.class))).thenAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            return managerArg.getClientCount();
        });

        //запускаем процесс
        autoAssignProcessor.process();

        //проверка результата
        verify(autoAssignService).assignClientToManager(clients.get(0), managers.get(2));
        verify(autoAssignService).assignClientToManager(clients.get(1), managers.get(0));
        verify(autoAssignService).assignClientToManager(clients.get(2), managers.get(1));
        verify(autoAssignService).assignClientToManager(clients.get(3), managers.get(3));
        verify(autoAssignService).sendEmailNotifications();
    }

    @Order(2)
    @Test
    public void testDifferentGrade(){
        List<Client> clients = List.of(
            new Client("AAAAAA", "Ordinary", "Figital", "Москва", "Лебедев А. Д."),
            new Client("AAAAAD", "Major", "Figital", "Москва", "Тихонов В. Л."),
            new Client("AAAAAB", "Ordinary", "Figital", "Казань", "Лебедев А. Д."),
            new Client("AAAAAС", "Major", "Figital", "Тула", "Лебедев А. Д.")
        );

        List<Manager> managers = List.of(
                new Manager("RUMAAA", "Тимофеев О. К.", "Figital", "Москва", false,
                        1, "Major", "vasilev.v03@mail.ru"),
                new Manager("RUMATA", "Киселев О. К.", "Figital", "Москва", false,
                        0, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMABA", "Крыжовник О. К.", "Figital", "Казань", false,
                        1, "Major", "vasilev.v03@mail.ru"),
                new Manager("RUMACA", "Апельсин О. К.", "Figital", "Тула", false,
                        1, "Ordinary", "vasilev.v03@mail.ru")
        );

        //настраиваем поведение autoAssignService
        when(autoAssignService.getClientsForAssignFromDwh()).thenReturn(clients);
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Казань"), eq("Figital")))
                .thenReturn(Collections.singletonList(managers.get(2)));
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Тула"), eq("Figital")))
                .thenReturn(Collections.singletonList(managers.get(3)));
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Москва"), eq("Figital")))
                .thenReturn(managers.subList(0,2));

        doAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            managerArg.setIsSick(false);
            managerArg.setIsVacation(false);
            return null;
        }).when(autoAssignService).updateManagerFromSap(any(Manager.class));

        when(autoAssignService.validateClient(any(Client.class))).thenCallRealMethod();
        when(autoAssignService.isSameGrade(any(Manager.class), any(Client.class))).thenCallRealMethod();
        when(autoAssignService.getManagerActualClientCount(any(Manager.class))).thenAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            return managerArg.getClientCount();
        });

        //запускаем процесс
        autoAssignProcessor.process();

        //проверка результата
        verify(autoAssignService).assignClientToManager(clients.get(0), managers.get(1));
        verify(autoAssignService).assignClientToManager(clients.get(1), managers.get(0));
        verify(autoAssignService).assignClientToManager(clients.get(2), managers.get(2));
        verify(autoAssignService).fixErrorOnAssignProcess(argThat(client -> client.equals(clients.get(3))), eq("Нет менеджеров"));
        verify(autoAssignService).sendEmailNotifications();
    }

    @Order(3)
    @Test
    public void testManagerClientLoad(){
        List<Client> clients = List.of(
                new Client("AAAAAA", "Ordinary", "Figital", "Москва", "Лебедев А. Д.")
        );

        List<Manager> managers = List.of(
                new Manager("RUMAAA", "Тимофеев О. К.", "Figital", "Москва", false,
                        4, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMATA", "Киселев О. К.", "Figital", "Москва", false,
                        3, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMABA", "Крыжовник О. К.", "Figital", "Москва", false,
                        1, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMACA", "Апельсин О. К.", "Figital", "Москва", false,
                        2, "Ordinary", "vasilev.v03@mail.ru")
        );

        //настраиваем поведение autoAssignService
        when(autoAssignService.getClientsForAssignFromDwh()).thenReturn(clients);
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Москва"), eq("Figital")))
                .thenReturn(managers);

        doAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            managerArg.setIsSick(false);
            managerArg.setIsVacation(false);
            return null;
        }).when(autoAssignService).updateManagerFromSap(any(Manager.class));

        when(autoAssignService.validateClient(any(Client.class))).thenCallRealMethod();
        when(autoAssignService.isSameGrade(any(Manager.class), any(Client.class))).thenCallRealMethod();
        when(autoAssignService.getManagerActualClientCount(any(Manager.class))).thenAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            return managerArg.getClientCount();
        });

        //запускаем процесс
        autoAssignProcessor.process();

        //проверка результата
        verify(autoAssignService).assignClientToManager(clients.get(0), managers.get(2));
        verify(autoAssignService).sendEmailNotifications();
    }

    @Order(4)
    @Test
    public void testManagerActivity(){
        List<Client> clients = List.of(
                new Client("AAAAAA", "Ordinary", "Figital", "Москва", "Лебедев А. Д."),
                new Client("AAAAAB", "Ordinary", "Figital", "Казань", "Лебедева Б. Ю.")
        );

        List<Manager> managers = List.of(
                new Manager("RUMATA", "Киселев О. К.", "Figital", "Москва", false,
                        0, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMATB", "Компотов О. К.", "Figital", "Казань", false,
                        0, "Ordinary", "vasilev.v03@mail.ru")

        );

        //настраиваем поведение autoAssignService
        when(autoAssignService.getClientsForAssignFromDwh()).thenReturn(clients);
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Москва"), eq("Figital")))
                .thenReturn(Collections.singletonList(managers.get(0)));
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Казань"), eq("Figital")))
                .thenReturn(Collections.singletonList(managers.get(1)));

        doAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            managerArg.setIsSick(true);
            managerArg.setIsVacation(false);
            return null;
        }).when(autoAssignService).updateManagerFromSap(managers.get(0));

        doAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            managerArg.setIsSick(false);
            managerArg.setIsVacation(true);
            return null;
        }).when(autoAssignService).updateManagerFromSap(managers.get(1));

        when(autoAssignService.validateClient(any(Client.class))).thenCallRealMethod();
        when(autoAssignService.isSameGrade(any(Manager.class), any(Client.class))).thenCallRealMethod();
        when(autoAssignService.getManagerActualClientCount(any(Manager.class))).thenAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            return managerArg.getClientCount();
        });

        //запускаем процесс
        autoAssignProcessor.process();

        //проверка результата
        verify(autoAssignService).fixErrorOnAssignProcess(argThat(client -> client.equals(clients.get(0))), eq("Нет менеджеров"));
        verify(autoAssignService).fixErrorOnAssignProcess(argThat(client -> client.equals(clients.get(1))), eq("Нет менеджеров"));
        verify(autoAssignService).sendEmailNotifications();
    }

    @Order(5)
    @Test
    public void testHeadManager(){
        List<Client> clients = List.of(
                new Client("AAAAAA", "Ordinary", "Figital", "Москва", "Лебедев А. Д.")
        );

        List<Manager> managers = List.of(
                new Manager("RUMATA", "Киселев О. К.", "Figital", "Москва", true,
                        0, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMATB", "Компотов О. К.", "Figital", "Казань", false,
                        0, "Ordinary", "vasilev.v03@mail.ru")
        );

        //настраиваем поведение autoAssignService
        when(autoAssignService.getClientsForAssignFromDwh()).thenReturn(clients);
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Москва"), eq("Figital")))
                .thenReturn(Collections.singletonList(managers.get(0)));
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Казань"), eq("Figital")))
                .thenReturn(Collections.singletonList(managers.get(1)));

        doAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            managerArg.setIsSick(false);
            managerArg.setIsVacation(false);
            return null;
        }).when(autoAssignService).updateManagerFromSap(any(Manager.class));

        when(autoAssignService.validateClient(any(Client.class))).thenCallRealMethod();
        when(autoAssignService.isSameGrade(any(Manager.class), any(Client.class))).thenCallRealMethod();
        when(autoAssignService.getManagerActualClientCount(any(Manager.class))).thenAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            return managerArg.getClientCount();
        });

        //запускаем процесс
        autoAssignProcessor.process();

        //проверка результата
        verify(autoAssignService).assignClientToManager(clients.get(0), managers.get(0));
        verify(autoAssignService).sendEmailNotifications();
    }

    @Order(6)
    @Test
    public void testErrors(){
        List<Client> clients = List.of(
                new Client("AAAAAA", null, "Figital", "Москва", "Лебедев А. Д."),
                new Client("AAAAAB", "Ordinary", null, "Москва", "Захаров П. С."),
                new Client("AAAAAC", "Ordinary", "Figital", null, "Смирнов И. Г."),
                new Client("AAAAAD", "Ordinary", "Figital", "Казань", "Тихонов В. Л.")
        );

        List<Manager> managers = List.of(
                new Manager("RUMAAA", "Тимофеев О. К.", "Figital", "Москва", false,
                        4, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMATA", "Киселев О. К.", "Figital", "Москва", false,
                        3, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMABA", "Крыжовник О. К.", "Figital", "Москва", false,
                        1, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMACA", "Апельсин О. К.", "Figital", "Москва", false,
                        2, "Ordinary", "vasilev.v03@mail.ru")
        );

        //настраиваем поведение autoAssignService
        when(autoAssignService.getClientsForAssignFromDwh()).thenReturn(clients);
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Москва"), eq("Figital")))
                .thenReturn(managers);

        doAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            managerArg.setIsSick(false);
            managerArg.setIsVacation(false);
            return null;
        }).when(autoAssignService).updateManagerFromSap(any(Manager.class));

        when(autoAssignService.validateClient(any(Client.class))).thenCallRealMethod();
        when(autoAssignService.isSameGrade(any(Manager.class), any(Client.class))).thenCallRealMethod();
        when(autoAssignService.getManagerActualClientCount(any(Manager.class))).thenAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            return managerArg.getClientCount();
        });

        //запускаем процесс
        autoAssignProcessor.process();

        //проверка результата
        verify(autoAssignService).fixErrorOnAssignProcess(argThat(client -> client.equals(clients.get(0))), eq("Нет грейда"));
        verify(autoAssignService).fixErrorOnAssignProcess(argThat(client -> client.equals(clients.get(1))), eq("Не задан тип клиента"));
        verify(autoAssignService).fixErrorOnAssignProcess(argThat(client -> client.equals(clients.get(2))), eq("Нет региона"));
        verify(autoAssignService).fixErrorOnAssignProcess(argThat(client -> client.equals(clients.get(3))), eq("Нет менеджеров"));

        verify(autoAssignService).sendEmailNotifications();
    }

    @Order(7)
    @Test
    public void testParallelProcessing(){
        List<Client> clients = List.of(
                new Client("AAAAAA", "Ordinary", "Figital", "Москва", "Лебедев А. Д."),
                new Client("AAAAAB", "Ordinary", "Figital", "Москва", "Захаров П. С."),
                new Client("AAAAAC", "Ordinary", "Figital", "Москва", "Смирнов И. Г."),
                new Client("AAAAAD", "Ordinary", "Figital", "Москва", "Тихонов В. Л."),
                new Client("AAAAAE", "Ordinary", "Figital", "Москва", "Лебедев А. Д."),
                new Client("AAAAAF", "Ordinary", "Figital", "Москва", "Захаров П. С."),
                new Client("AAAAAG", "Ordinary", "Figital", "Москва", "Смирнов И. Г."),
                new Client("AAAAAH", "Ordinary", "Figital", "Москва", "Тихонов В. Л."),
                new Client("AAAAAV", "Ordinary", "Figital", "Москва", "Тихонов В. Л.")
        );

        List<Manager> managers = List.of(
                new Manager("RUMATI", "Гусев О. К.", "Figital", "Москва", false,
                        30, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMATY", "Уткин О. К.", "Figital", "Москва", false,
                        30, "Ordinary", "vasilev.v03@mail.ru"),
                new Manager("RUMATD", "Уткин О. К.", "Figital", "Москва", false,
                        30, "Ordinary", "vasilev.v03@mail.ru")
        );

        Map<String, Integer> managerLoading = Collections.synchronizedMap(new HashMap<>());
        managerLoading.put("RUMATI", 0);
        managerLoading.put("RUMATY", 0);
        managerLoading.put("RUMATD", 0);
        //настраиваем поведение autoAssignService
        when(autoAssignService.getClientsForAssignFromDwh()).thenReturn(clients);
        when(autoAssignService.getManagersForAssignByRegionAndTypeFromMdm(eq("Москва"), eq("Figital")))
                .thenReturn(managers);

        doAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            managerArg.setIsSick(false);
            managerArg.setIsVacation(false);
            return null;
        }).when(autoAssignService).updateManagerFromSap(any(Manager.class));

        when(autoAssignService.validateClient(any(Client.class))).thenCallRealMethod();
        when(autoAssignService.isSameGrade(any(Manager.class), any(Client.class))).thenCallRealMethod();
        when(autoAssignService.getManagerActualClientCount(any(Manager.class))).thenAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(0);
            return managerArg.getClientCount() + managerLoading.get(managerArg.getId());
        });
        doAnswer(invocationOnMock -> {
            Manager managerArg = invocationOnMock.getArgument(1);
            managerLoading.merge(managerArg.getId(), 1, Integer::sum);
            return null;
        }).when(autoAssignService).assignClientToManager(any(Client.class), any(Manager.class));

        //запускаем процесс
        autoAssignProcessor.process();

        //проверка результата
        verify(autoAssignService).assignClientToManager(argThat(client -> client.equals(clients.get(0))), any(Manager.class));
        verify(autoAssignService).assignClientToManager(argThat(client -> client.equals(clients.get(1))), any(Manager.class));
        verify(autoAssignService).assignClientToManager(argThat(client -> client.equals(clients.get(2))), any(Manager.class));
        verify(autoAssignService).assignClientToManager(argThat(client -> client.equals(clients.get(3))), any(Manager.class));
        verify(autoAssignService).assignClientToManager(argThat(client -> client.equals(clients.get(4))), any(Manager.class));
        verify(autoAssignService).assignClientToManager(argThat(client -> client.equals(clients.get(5))), any(Manager.class));
        verify(autoAssignService).assignClientToManager(argThat(client -> client.equals(clients.get(6))), any(Manager.class));
        verify(autoAssignService).assignClientToManager(argThat(client -> client.equals(clients.get(7))), any(Manager.class));

        assertEquals(managerLoading.get("RUMATI"), 3);      //нагрузка должна быть равномерна распределена по менеджерам, так как они одинаковы
        assertEquals(managerLoading.get("RUMATY"), 3);
        assertEquals(managerLoading.get("RUMATD"), 3);

        verify(autoAssignService).sendEmailNotifications();
    }
}