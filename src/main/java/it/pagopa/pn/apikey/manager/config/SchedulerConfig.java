package it.pagopa.pn.apikey.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class SchedulerConfig {

    @Bean("apikeyManagerScheduler")
    public Scheduler scheduler() {
        return Schedulers.boundedElastic();
    }

}
