package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.ResultListDto;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.application.ResultListService;
import de.jobst.resulter.application.certificate.CertificateService;
import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@Slf4j
public class ResultListController {

    private final EventService eventService;
    private final ResultListService resultListService;

    @Autowired
    public ResultListController(EventService eventService, ResultListService resultListService) {
        this.eventService = eventService;
        this.resultListService = resultListService;
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @PutMapping("/result_list/{id}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResultListDto> calculateResultListScore(@PathVariable Long id) {
        try {
            ResultList resultList = resultListService.calculateScore(ResultListId.of(id));
            /*
            if (null != resultList) {
                return ResponseEntity.ok(ResultListDto.from(resultList));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            */
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
