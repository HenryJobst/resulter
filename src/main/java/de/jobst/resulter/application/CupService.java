package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.domain.*;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CupService {

    private final CupRepository cupRepository;

    public CupService(CupRepository cupRepository) {
        this.cupRepository = cupRepository;
    }

    public List<Cup> findAll() {
        return cupRepository.findAll();
    }

    public Cup findOrCreate(Cup cup) {
        return cupRepository.findOrCreate(cup);
    }

    public Optional<Cup> findById(CupId cupId) {
        return cupRepository.findById(cupId);
    }


    public Cup updateCup(CupId id, CupName name, CupType type, Collection<EventId> eventIds) {

        Optional<Cup> optionalCup = findById(id);
        if (optionalCup.isEmpty()) {
            return null;
        }
        Cup cup = optionalCup.get();
        cup.update(name, type, eventIds);
        return cupRepository.save(cup);
    }

    public Cup createCup(String name, CupType type, Collection<EventId> eventIds) {
        Cup cup = Cup.of(CupId.empty().value(), name, type, eventIds);
        return cupRepository.save(cup);
    }

    public boolean deleteCup(CupId cupId) {
        Optional<Cup> optionalCup = findById(cupId);
        if (optionalCup.isEmpty()) {
            return false;
        }
        Cup cup = optionalCup.get();
        cupRepository.deleteCup(cup);
        return true;
    }
}
