package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CupDetailedDto;
import de.jobst.resulter.adapter.driver.web.dto.CupDto;
import de.jobst.resulter.adapter.driver.web.dto.CupScoreListDto;
import de.jobst.resulter.adapter.driver.web.dto.CupTypeDto;
import de.jobst.resulter.application.CupService;
import de.jobst.resulter.application.ResultListService;
import de.jobst.resulter.application.config.ApiResponse;
import de.jobst.resulter.application.config.LocalizableString;
import de.jobst.resulter.application.config.MessageKeys;
import de.jobst.resulter.application.config.ResponseUtil;
import de.jobst.resulter.domain.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.*;

@RestController
@Slf4j
public class CupController {

    private final ResultListService resultListService;
    private final CupService cupService;

    @Autowired
    public CupController(ResultListService resultListService, CupService cupService) {
        this.resultListService = resultListService;
        this.cupService = cupService;
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        log.error(e.getStackTrace().toString());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @GetMapping("/cup_types")
    public ResponseEntity<ApiResponse<List<CupTypeDto>>> handleCupTypes(HttpServletRequest request) {
        List<CupTypeDto> cupTypes = Arrays.stream(CupType.values()).map(CupTypeDto::from).toList();
        return ResponseUtil.success(cupTypes,
            LocalizableString.of(MessageKeys.SUCCESSFULLY_RETRIEVED), request.getRequestURI());
    }

    @GetMapping("/cup")
    public ResponseEntity<ApiResponse<Page<CupDto>>> searchCups(@RequestParam(required = false) Optional<String> filter,
                                                                @PageableDefault(size = 1000) Pageable pageable,
                                                                HttpServletRequest request) {
        Page<Cup> cups = cupService.findAll(filter.orElse(null),
            pageable != null ?
            FilterAndSortConverter.mapOrderProperties(pageable, CupDto::mapOrdersDtoToDomain) :
            Pageable.unpaged());
        return ResponseUtil.success(new PageImpl<>(cups.getContent().stream().map(CupDto::from).toList(),
            FilterAndSortConverter.mapOrderProperties(cups.getPageable(), CupDto::mapOrdersDomainToDto),
            cups.getTotalElements()),
            LocalizableString.of(MessageKeys.SUCCESSFULLY_RETRIEVED),
            request.getRequestURI());
    }

    @GetMapping("/cup/{id}")
    public ResponseEntity<CupDto> getCup(@PathVariable Long id) {
        try {
            Optional<Cup> cup = cupService.findById(CupId.of(id));
            return cup.map(value -> ResponseEntity.ok(CupDto.from(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cup/{id}/results")
    public ResponseEntity<CupDetailedDto> getCupDetailed(@PathVariable Long id) {
        try {
            return cupService.getCupDetailed(CupId.of(id))
                .map(x -> ResponseEntity.ok(CupDetailedDto.from(x)))
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/cup/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CupDto> updateCup(@PathVariable Long id, @RequestBody CupDto cupDto) {
        try {
            Cup cup = cupService.updateCup(CupId.of(id),
                CupName.of(cupDto.name()),
                CupType.fromValue(cupDto.type().id()),
                Year.of(cupDto.year()),
                cupDto.events() == null ?
                new ArrayList<>() :
                cupDto.events().stream().map(x -> EventId.of(x.id())).toList());
            if (null != cup) {
                return ResponseEntity.ok(CupDto.from(cup));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/cup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CupDto> createCup(@RequestBody CupDto cupDto) {
        try {
            Cup cup = cupService.createCup(cupDto.name(),
                CupType.fromValue(cupDto.type().id()),
                Year.of(cupDto.year()),
                cupDto.events() == null ?
                new ArrayList<>() :
                cupDto.events().stream().map(x -> EventId.of(x.id())).filter(ObjectUtils::isNotEmpty).toList());
            if (null != cup) {
                return ResponseEntity.ok(CupDto.from(cup));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/cup/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> deleteCup(@PathVariable Long id) {
        try {
            boolean success = cupService.deleteCup(CupId.of(id));
            if (success) {
                return ResponseEntity.ok(Boolean.TRUE);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/cup/{id}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CupScoreListDto>> calculateCup(@PathVariable Long id) {
        try {
            List<CupScoreListDto> cupScoreLists = cupService.calculateScore(CupId.of(id));
            return ResponseEntity.ok(cupScoreLists);
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
