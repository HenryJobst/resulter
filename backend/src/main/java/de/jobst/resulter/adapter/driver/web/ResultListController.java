package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CupScoreListDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateStatDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateStatsDto;
import de.jobst.resulter.adapter.driver.web.mapper.EventCertificateStatMapper;
import de.jobst.resulter.application.port.CertificateService;
import de.jobst.resulter.application.port.EventCertificateQueryService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.domain.CupScoreList;
import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.EventCertificateStatId;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.ResultListId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class ResultListController {

    private final ResultListService resultListService;
    private final CertificateService certificateService;
    private final EventCertificateQueryService eventCertificateQueryService;

    public ResultListController(
            ResultListService resultListService,
            CertificateService certificateService,
            EventCertificateQueryService eventCertificateQueryService) {
        this.resultListService = resultListService;
        this.certificateService = certificateService;
        this.eventCertificateQueryService = eventCertificateQueryService;
    }

    @GetMapping("/event/{id}/certificate_stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateStatsDto> getCertificateStats(@PathVariable Long id) {
        var batchResult = eventCertificateQueryService.getCertificateStats(EventId.of(id));
        List<EventCertificateStatDto> eventCertificateStatDtos = EventCertificateStatMapper.toDtos(
                batchResult.eventCertificateStats(), batchResult.eventMap(), batchResult.personMap());
        return ResponseEntity.ok(new EventCertificateStatsDto(eventCertificateStatDtos));
    }

    @PutMapping("/result_list/{id}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CupScoreListDto>> calculateResultListScore(@PathVariable Long id) {
        List<CupScoreList> cupScoreLists = resultListService.calculateScore(ResultListId.of(id));
        return ResponseEntity.ok(
                cupScoreLists.stream().map(CupScoreListDto::from).toList());
    }

    @GetMapping("/result_list/{id}/cup_score_lists")
    public ResponseEntity<List<CupScoreListDto>> getCupScoreLists(@PathVariable Long id) {
        List<CupScoreListDto> cupScoreLists = resultListService.getCupScoreLists(ResultListId.of(id)).stream()
                .map(CupScoreListDto::from)
                .toList();
        return ResponseEntity.ok(cupScoreLists);
    }

    @GetMapping("/result_list/{id}/certificate")
    public ResponseEntity<ByteArrayResource> getCertificate(
            @PathVariable Long id, @RequestParam String classResultShortName, @RequestParam Long personId) {
        CertificateService.Certificate certificate = resultListService.createCertificate(
                ResultListId.of(id), ClassResultShortName.of(classResultShortName),
                PersonId.of(personId));
        if (null != certificate) {
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION.toLowerCase(),
                            "attachment; filename=\"" + certificate.filename() + "\"")
                    .contentLength(certificate.size())
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(certificate.resource());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/event_certificate_stat/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEventCertificateStat(@PathVariable Long id) {
        resultListService.deleteEventCertificateStat(EventCertificateStatId.of(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/certificate_schema")
    public ResponseEntity<String> getCertificateSchema() {
        return ResponseEntity.ok(certificateService.getCertificateSchema());
    }
}
