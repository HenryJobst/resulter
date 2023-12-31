package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupId;
import org.springframework.stereotype.Service;

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
}
