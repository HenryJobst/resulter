package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CupDetailedDto;
import de.jobst.resulter.adapter.driver.web.dto.CupDto;
import de.jobst.resulter.adapter.driver.web.dto.CupScoreListDto;
import de.jobst.resulter.adapter.driver.web.dto.CupTypeDto;
import de.jobst.resulter.adapter.driver.web.mapper.CupDetailedMapper;
import de.jobst.resulter.adapter.driver.web.mapper.CupMapper;
import de.jobst.resulter.application.port.CupService;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.CupName;
import de.jobst.resulter.domain.CupScoreList;
import de.jobst.resulter.domain.CupType;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.aggregations.CupDetailed;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CupController {

    private final CupService cupService;
    private final CupMapper cupMapper;
    private final CupDetailedMapper cupDetailedMapper;

    public CupController(CupService cupService, CupMapper cupMapper, CupDetailedMapper cupDetailedMapper) {
        this.cupService = cupService;
        this.cupMapper = cupMapper;
        this.cupDetailedMapper = cupDetailedMapper;
    }

    @GetMapping("/cup_types")
    public ResponseEntity<List<CupTypeDto>> handleCupTypes() {
        return ResponseEntity.ok(
                Arrays.stream(CupType.values()).map(CupTypeDto::from).toList());
    }

    @GetMapping("/cup/all")
    public ResponseEntity<List<CupDto>> getAllCups() {
        List<Cup> cups = cupService.findAll();
        return ResponseEntity.ok(cupMapper.toDtos(cups));
    }

    @GetMapping("/cup")
    public ResponseEntity<Page<CupDto>> searchCups(
            @RequestParam(required = false) Optional<String> filter, @Nullable Pageable pageable) {
        Page<Cup> cups = cupService.findAll(
                filter.orElse(null),
                pageable != null
                        ? FilterAndSortConverter.mapOrderProperties(pageable, CupDto::mapOrdersDtoToDomain)
                        : Pageable.unpaged());

        return ResponseEntity.ok(new PageImpl<>(
                cupMapper.toDtos(cups.getContent()),
                FilterAndSortConverter.mapOrderProperties(cups.getPageable(), CupDto::mapOrdersDomainToDto),
                cups.getTotalElements()));
    }

    @GetMapping("/cup/{id}")
    public ResponseEntity<CupDto> getCup(@PathVariable Long id) {
        return ResponseEntity.ok(cupMapper.toDtos(List.of(cupService.getById(CupId.of(id)))).getFirst());
    }

    @GetMapping("/cup/{id}/results")
    public ResponseEntity<CupDetailedDto> getCupDetailed(@PathVariable Long id) {
        CupDetailed cupDetailed = cupService.getCupDetailed(CupId.of(id));
        return ResponseEntity.ok(cupDetailedMapper.toDto(cupDetailed));
    }

    @PutMapping("/cup/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CupDto> updateCup(@PathVariable Long id, @RequestBody CupDto cupDto) {
        Cup cup = cupService.updateCup(
                CupId.of(id),
                CupName.of(cupDto.name()),
                CupType.fromValue(cupDto.type().id()),
                Year.of(cupDto.year()),
                cupDto.events() == null
                        ? new ArrayList<>()
                        : cupDto.events().stream().map(x -> EventId.of(x.id())).toList());
        return ResponseEntity.ok(cupMapper.toDtos(List.of(cup)).getFirst());
    }

    @PostMapping("/cup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CupDto> createCup(@RequestBody CupDto cupDto) {
        Cup cup = cupService.createCup(
                cupDto.name(),
                CupType.fromValue(cupDto.type().id()),
                Year.of(cupDto.year()),
                cupDto.events() == null
                        ? new ArrayList<>()
                        : cupDto.events().stream()
                                .map(x -> EventId.of(x.id()))
                                .filter(ObjectUtils::isNotEmpty)
                                .toList());
        return ResponseEntity.ok(cupMapper.toDtos(List.of(cup)).getFirst());
    }

    @DeleteMapping("/cup/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> deleteCup(@PathVariable Long id) {
        cupService.deleteCup(CupId.of(id));
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @PutMapping("/cup/{id}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CupScoreListDto>> calculateCup(@PathVariable Long id) {
        List<CupScoreList> cupScoreLists = cupService.calculateScore(CupId.of(id));
        return ResponseEntity.ok(
                cupScoreLists.stream().map(CupScoreListDto::from).toList());
    }
}
