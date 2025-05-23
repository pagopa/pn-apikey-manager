package it.pagopa.pn.apikey.manager;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import static it.pagopa.pn.apikey.manager.utils.SpringApplicationUtils.buildSpringApplicationWithListener;

@SpringBootApplication
@EnableScheduling
public class ApiKeyManagerApplication {

    public static void main(String[] args) {
        buildSpringApplicationWithListener().run(args);
    }
}
