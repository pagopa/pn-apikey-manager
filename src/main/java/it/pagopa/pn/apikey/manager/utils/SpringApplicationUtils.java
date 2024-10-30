package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.ApiKeyManagerApplication;
import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import org.springframework.boot.SpringApplication;

public class SpringApplicationUtils {

    public static SpringApplication buildSpringApplicationWithListener() {
        SpringApplication app = new SpringApplication(ApiKeyManagerApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        return app;
    }
}
