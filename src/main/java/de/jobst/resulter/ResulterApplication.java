package de.jobst.resulter;

import de.jobst.resulter.adapter.out.jpa.inmem.InMemoryEventRepository;
import de.jobst.resulter.application.port.EventRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ResulterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResulterApplication.class, args);
	}

	@Bean
	@ConditionalOnProperty("resulter.repository.inmemory")
	public EventRepository eventRepository() {
        return new InMemoryEventRepository();
	}
}
