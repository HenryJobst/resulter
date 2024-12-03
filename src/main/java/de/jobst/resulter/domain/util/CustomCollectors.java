package de.jobst.resulter.domain.util;

import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.PersonWithScore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

public class CustomCollectors {

    public static Collector<PersonWithScore, ?, Map<ClassResultShortName, ClassResultShortNameScoreSummary>> groupingByClassResultShortNameAndScoreSumming() {
        return Collector.of(HashMap::new,
            (map, person) -> map.merge(person.classResultShortName(),
                new ClassResultShortNameScoreSummary(person.score(), new HashSet<>(List.of(person.id()))),
                (existing, incoming) -> {
                    existing.sumScore(incoming.getScore());
                    existing.getIds().addAll(incoming.getIds());
                    return existing;
                }), (map1, map2) -> {
                map2.forEach((key, value) -> map1.merge(key, value, (existing, incoming) -> {
                    existing.sumScore(incoming.getScore());
                    existing.getIds().addAll(incoming.getIds());
                    return existing;
                }));
                return map1;
            });
    }

}

