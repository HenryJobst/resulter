package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public record CupBatchResult(
        List<Cup> cups, long totalElements, Pageable resolvedPageable, Map<EventId, Event> eventMap) {}
