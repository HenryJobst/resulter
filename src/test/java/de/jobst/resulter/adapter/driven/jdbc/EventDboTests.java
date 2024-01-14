package de.jobst.resulter.adapter.driven.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EventDboTests {

    @Autowired
    EventJdbcRepository eventJdbcRepository;

    @Test
    void testCreate() {

        EventDbo eventDbo = new EventDbo("test");

        eventJdbcRepository.save(eventDbo);

        //	EventDbo reloaded = events.findById(eventDbo.getId().longValue()).get();

    }
}
