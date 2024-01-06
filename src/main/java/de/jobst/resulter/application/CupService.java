package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
public class CupService {

    private final CupRepository cupRepository;

    public CupService(CupRepository cupRepository) {
        this.cupRepository = cupRepository;
    }

    public List<Cup> findAll(CupConfig cupConfig) {
        return cupRepository.findAll(cupConfig);
    }

    public Cup findOrCreate(Cup cup) {
        return cupRepository.findOrCreate(cup);
    }

    public Optional<Cup> findById(CupId cupId, CupConfig cupConfig) {
        return cupRepository.findById(cupId, cupConfig);
    }

    @NonNull
    public static CupConfig getCupConfig(Boolean shallowEvents, EventConfig eventConfig) {
        EnumSet<CupConfig.ShallowCupLoads> shallowLoads = EnumSet.noneOf(CupConfig.ShallowCupLoads.class);
        if (shallowEvents) {
            shallowLoads.add(CupConfig.ShallowCupLoads.EVENTS);
        }
        return CupConfig.of(shallowLoads, eventConfig);
    }

    public Cup updateCup(CupId id, CupName name, CupType type, Events events) {

        EventConfig eventConfig = EventConfig.empty();
        CupConfig cupConfig = getCupConfig(false, eventConfig);
        Optional<Cup> optionalCup = findById(id, cupConfig);
        if (optionalCup.isEmpty()) {
            return null;
        }
        Cup cup = optionalCup.get();
        cup.update(name, type, events);
        return cupRepository.save(cup);
    }

    public Cup createCup(String name, CupType type, Events events) {
        Cup cup = Cup.of(CupId.empty().value(), name, type, events.value());
        return cupRepository.save(cup);
    }

    public boolean deleteCup(CupId cupId) {
        Optional<Cup> optionalCup = findById(cupId, CupConfig.full());
        if (optionalCup.isEmpty()) {
            return false;
        }
        Cup cup = optionalCup.get();
        cupRepository.deleteCup(cup);
        return true;
    }
}
