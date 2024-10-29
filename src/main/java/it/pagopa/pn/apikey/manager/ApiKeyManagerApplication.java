package it.pagopa.pn.apikey.manager;

import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiKeyManagerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApiKeyManagerApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        app.run(args);
    }

}
