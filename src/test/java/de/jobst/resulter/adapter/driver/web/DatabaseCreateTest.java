package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.TestConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@DataJdbcTest(properties = {"spring.test.database.replace=NONE", "resulter.repository.inmemory=false"})
@ContextConfiguration(classes = {TestConfig.class}, loader = AnnotationConfigContextLoader.class)
@ComponentScan(basePackages = {"de.jobst.resulter.application", "de.jobst.resulter.adapter.driver.web",
    "de.jobst.resulter.adapter.driven.jdbc"})
@EntityScan(basePackages = {"de.jobst.resulter.adapter.driver.web", "de.jobst.resulter.adapter.driven.jdbc"})
@EnableJdbcRepositories(basePackages = {"de.jobst.resulter.adapter.driven.jdbc"})
@ExtendWith(SpringExtension.class)
@Profile("testcontainer")
class DatabaseCreateTest {

    @Test
    void test_create_database() throws Exception {
        TestRestTemplate restTemplate = new TestRestTemplate();
        String url = "http://localhost:8080/createDatabase";
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));


    }
}
