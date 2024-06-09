package ru.alfastudents.smartmatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.alfastudents.smartmatch.integration.service.simulator.DwhSimulator;
import ru.alfastudents.smartmatch.integration.service.simulator.MdmSimulator;
import ru.alfastudents.smartmatch.integration.service.simulator.SapSimulator;
import ru.alfastudents.smartmatch.integration.service.DwhService;
import ru.alfastudents.smartmatch.integration.service.MdmService;
import ru.alfastudents.smartmatch.integration.service.SapService;

@Configuration
public class AppConfig {

    @Bean
    public DwhService dwhService() {
        return new DwhSimulator();
    }

    @Bean
    public MdmService mdmService() {
        return new MdmSimulator();
    }

    @Bean
    public SapService sapService() {
        return new SapSimulator();
    }
}
