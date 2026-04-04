package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.CupService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.RaceService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.application.port.CupScoreListRepository;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryCode;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.CountryName;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.CupScoreList;
import de.jobst.resulter.domain.CupScoreListId;
import de.jobst.resulter.domain.CupType;
import de.jobst.resulter.domain.Discipline;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.OrganisationName;
import de.jobst.resulter.domain.OrganisationShortName;
import de.jobst.resulter.domain.OrganisationType;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.RaceId;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.QueryCountHolder;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "openapi.config.contact.name=Test",
            "openapi.config.contact.email=test@example.com",
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost/issuer",
            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwks",
            "resulter.media-file-path=./target/test-media",
            "resulter.media-file-path-thumbnails=./target/test-media/thumbnails",
            "security.prometheus.api-token=test-token"
        })
@ActiveProfiles({"nosecurity"})
@Import(NPlusOneReviewIT.QueryCountProxyConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NPlusOneReviewIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private CountryService countryService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private de.jobst.resulter.application.port.EventService eventService;

    @Autowired
    private RaceService raceService;

    @Autowired
    private CupService cupService;

    @Autowired
    private ResultListService resultListService;

    @Autowired
    private CupScoreListRepository cupScoreListRepository;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void runNPlusOneBenchmark() {
        CountryId countryId = ensureCountry();
        OrganisationId organisationId = ensureOrganisation(countryId);

        Map<String, Long> result = new LinkedHashMap<>();

        // /cup/{id}/results with 1, 10, 50 events
        Cup cup1 = createCupWithEvents("cup-n1", 1, organisationId);
        Cup cup10 = createCupWithEvents("cup-n10", 10, organisationId);
        Cup cup50 = createCupWithEvents("cup-n50", 50, organisationId);
        result.put("GET /cup/{id}/results (1 event)", measure("/cup/" + cup1.getId().value() + "/results"));
        result.put("GET /cup/{id}/results (10 events)", measure("/cup/" + cup10.getId().value() + "/results"));
        result.put("GET /cup/{id}/results (50 events)", measure("/cup/" + cup50.getId().value() + "/results"));

        // /event pagination with 20/100
        seedEvents(organisationId, 120, "event-page");
        result.put("GET /event?page=0&size=20", measure("/event?page=0&size=20"));
        result.put("GET /event?page=0&size=100", measure("/event?page=0&size=100"));

        // /event/all with growth 1/10/50
        seedEvents(organisationId, 1, "event-all-small");
        result.put("GET /event/all (after +1 event)", measure("/event/all"));
        seedEvents(organisationId, 9, "event-all-medium");
        result.put("GET /event/all (after +10 events total)", measure("/event/all"));
        seedEvents(organisationId, 40, "event-all-large");
        result.put("GET /event/all (after +50 events total)", measure("/event/all"));

        // /organisation pagination with 20/100
        seedOrganisations(countryId, 120);
        result.put("GET /organisation?page=0&size=20", measure("/organisation?page=0&size=20"));
        result.put("GET /organisation?page=0&size=100", measure("/organisation?page=0&size=100"));

        // /result_list/{id}/cup_score_lists with varying number of CupScoreLists
        ResultListId resultListSmall = createResultListWithCupScoreLists(organisationId, 1, "rl-small");
        ResultListId resultListMedium = createResultListWithCupScoreLists(organisationId, 10, "rl-medium");
        ResultListId resultListLarge = createResultListWithCupScoreLists(organisationId, 50, "rl-large");
        result.put("GET /result_list/{id}/cup_score_lists (1 list)",
                measure("/result_list/" + resultListSmall.value() + "/cup_score_lists"));
        result.put("GET /result_list/{id}/cup_score_lists (10 lists)",
                measure("/result_list/" + resultListMedium.value() + "/cup_score_lists"));
        result.put("GET /result_list/{id}/cup_score_lists (50 lists)",
                measure("/result_list/" + resultListLarge.value() + "/cup_score_lists"));

        System.out.println("\\n==== N+1 BENCHMARK QUERY COUNTS ====\n");
        result.forEach((k, v) -> System.out.println(k + " -> " + v));
        System.out.println("\\n====================================\n");
    }

    private long measure(String path) {
        QueryCountHolder.clear();
        try {
            mockMvc.perform(get(path)).andExpect(status().is2xxSuccessful());
        } catch (Exception e) {
            throw new IllegalStateException("Request failed for " + path, e);
        }
        return QueryCountHolder.getGrandTotal().getTotal();
    }

    private CountryId ensureCountry() {
        List<Country> countries = countryService.findAll();
        if (!countries.isEmpty()) {
            return countries.getFirst().getId();
        }
        Country created = countryService.createCountry(CountryCode.of("ZZZ"), CountryName.of("Queryland"));
        return created.getId();
    }

    private OrganisationId ensureOrganisation(CountryId countryId) {
        List<Organisation> organisations = organisationService.findAll();
        if (!organisations.isEmpty()) {
            return organisations.getFirst().getId();
        }
        String token = suffix();
        Organisation created = organisationService.createOrganisation(
                OrganisationName.of("NPlusOne Root " + token),
                OrganisationShortName.of("N1R" + token.substring(0, 3)),
                OrganisationType.OTHER,
                countryId,
                List.of());
        if (created == null) {
            throw new IllegalStateException("Could not create fallback organisation");
        }
        return created.getId();
    }

    private Cup createCupWithEvents(String prefix, int count, OrganisationId organisationId) {
        List<EventId> eventIds = seedEvents(organisationId, count, prefix);
        return cupService.createCup(prefix + "-" + suffix(), CupType.ADD, Year.now(), eventIds);
    }

    private List<EventId> seedEvents(OrganisationId organisationId, int count, String prefix) {
        List<EventId> eventIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String token = prefix + "-" + i + "-" + suffix();
            Event event = eventService.createEvent(
                    "Event " + token,
                    ZonedDateTime.now().plusDays(i),
                    Set.of(organisationId),
                    Discipline.LONG.value(),
                    false);
            Race race = raceService.findOrCreate(Race.of(RaceId.empty(), event.getId(), "Race " + token, (byte) 1));
            if (race.getId() == null) {
                throw new IllegalStateException("Race ID should not be null");
            }
            eventIds.add(event.getId());
        }
        return eventIds;
    }

    private void seedOrganisations(CountryId countryId, int count) {
        for (int i = 0; i < count; i++) {
            String token = "ORG-" + i + "-" + suffix();
            organisationService.createOrganisation(
                    OrganisationName.of("Organisation " + token),
                    OrganisationShortName.of("O" + Math.abs(token.hashCode() % 1000000)),
                    OrganisationType.OTHER,
                    countryId,
                    List.of());
        }
    }

    private ResultListId createResultListWithCupScoreLists(OrganisationId organisationId, int listCount, String prefix) {
        List<EventId> eventIds = seedEvents(organisationId, 1, prefix + "-event");
        EventId eventId = eventIds.getFirst();
        Race race = raceService.findAllByEventIds(List.of(eventId)).stream().findFirst().orElseThrow();

        ResultList resultList = new ResultList(
                ResultListId.empty(),
                eventId,
                race.getId(),
                "benchmark",
                ZonedDateTime.now(),
                "COMPLETE",
                null);
        ResultList persisted = resultListService.findOrCreate(resultList);

        List<CupScoreList> cupScoreLists = new ArrayList<>();
        for (int i = 0; i < listCount; i++) {
            Cup cup = cupService.createCup(
                    prefix + "-cup-" + i + "-" + suffix(),
                    CupType.ADD,
                    Year.now(),
                    List.of(eventId));
            cupScoreLists.add(new CupScoreList(
                    CupScoreListId.empty(),
                    CupId.of(cup.getId().value()),
                    persisted.getId(),
                    List.of(),
                    "benchmark",
                    ZonedDateTime.now()));
        }
        cupScoreListRepository.saveAll(cupScoreLists);
        return persisted.getId();
    }

    private String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @TestConfiguration
    static class QueryCountProxyConfig {

        @Bean
        PostgreSQLContainer<?> postgresContainer() {
            PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("resulter_test")
                    .withUsername("test")
                    .withPassword("test");
            container.start();
            return container;
        }

        @Bean
        @Primary
        DataSource dataSource(PostgreSQLContainer<?> postgresContainer) {
            DriverManagerDataSource raw = new DriverManagerDataSource();
            raw.setDriverClassName(postgresContainer.getDriverClassName());
            raw.setUrl(postgresContainer.getJdbcUrl());
            raw.setUsername(postgresContainer.getUsername());
            raw.setPassword(postgresContainer.getPassword());

            return ProxyDataSourceBuilder.create(raw)
                    .name("nplusone-review")
                    .countQuery()
                    .build();
        }
    }
}
