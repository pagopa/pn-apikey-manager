package it.pagopa.pn.apikey.manager;

import io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@EnableAutoConfiguration(exclude= {SqsAutoConfiguration.class})
public abstract class CommonTestConfig {
}
