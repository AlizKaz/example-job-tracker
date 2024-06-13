package com.thirty3.job.job_tracker.configuration;

import java.util.Collection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "env.config.enabled", havingValue = "true")
@Component
public class AppContextRefreshedEventPropertiesPrinter {

  @EventListener
  public void handleContextRefreshed(ContextRefreshedEvent event) {
    ConfigurableEnvironment env =
        (ConfigurableEnvironment) event.getApplicationContext().getEnvironment();

    env.getPropertySources().stream()
        .filter(ps -> ps instanceof MapPropertySource)
        .map(ps -> ((MapPropertySource) ps).getSource().keySet())
        .flatMap(Collection::stream)
        .distinct()
        .sorted()
        //                .forEach(key -> LOGGER.info("{}={}", key, env.getProperty(key)));    }
        .forEach(key -> System.out.println("*****" + key + "=" + env.getProperty(key)));
  }
}
