package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CupScoreListDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateStatDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateStatsDto;
import de.jobst.resulter.application.ResultListService;
import de.jobst.resulter.application.certificate.CertificateService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
public class ResultListController {

    private final ResultListService resultListService;
    private final CertificateService certificateService;

    @Autowired
    public ResultListController(ResultListService resultListService,
                                CertificateService certificateService) {
        this.resultListService = resultListService;
        this.certificateService = certificateService;
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @GetMapping("/event/{id}/certificate_stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateStatsDto> getCertificateStats(@PathVariable Long id) {
        try {
            List<EventCertificateStatDto> eventCertificateStatDtos =
                resultListService.getCertificateStats(EventId.of(id));
            return ResponseEntity.ok(new EventCertificateStatsDto(eventCertificateStatDtos));
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/result_list/{id}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CupScoreListDto>> calculateResultListScore(@PathVariable Long id) {
        try {
            List<CupScoreListDto> cupScoreLists = resultListService.calculateScore(ResultListId.of(id));
            return ResponseEntity.ok(cupScoreLists);
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/result_list/{id}/cup_score_lists")
    public ResponseEntity<List<CupScoreListDto>> getCupScoreLists(@PathVariable Long id) {
        try {
            List<CupScoreListDto> cupScoreLists =
                resultListService.getCupScoreLists(ResultListId.of(id)).stream().map(CupScoreListDto::from).toList();
            return ResponseEntity.ok(cupScoreLists);
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/result_list/{id}/certificate")
    public ResponseEntity<ByteArrayResource> getCertificate(@PathVariable Long id,
                                                            @RequestParam String classResultShortName,
                                                            @RequestParam Long personId) {
        try {
            CertificateService.Certificate certificate = resultListService.createCertificate(ResultListId.of(id),
                ClassResultShortName.of(classResultShortName),
                PersonId.of(personId));
            if (null != certificate) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION.toLowerCase(),
                        "attachment; filename=\"" + certificate.filename() + "\"")
                    .contentLength(certificate.size())
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(certificate.resource());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/event_certificate_stat/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEventCertificateStat(@PathVariable Long id) {
        try {
            resultListService.deleteEventCertificateStat(EventCertificateStatId.of(id));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/certificate_schema")
    public ResponseEntity<String> getCertificateSchema() {
        try {
            return ResponseEntity.ok(certificateService.getCertificateSchema());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
