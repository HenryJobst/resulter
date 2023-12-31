package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupConfig;
import de.jobst.resulter.domain.CupId;
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
}
