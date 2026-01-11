package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.springapp.config.ApiResponse;
import de.jobst.resulter.springapp.config.LocalizableString;
import de.jobst.resulter.springapp.config.MessageKeys;
import de.jobst.resulter.springapp.config.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class XmlController {

    public static final String FILE = "file";

    private final XMLImportService importService;
    private final OrganisationService organisationService;
    private final EventCertificateService eventCertificateService;
    private final ResultListService resultListService;
    private final SplitTimeListRepository splitTimeListRepository;

    public XmlController(
            XMLImportService importService,
            OrganisationService organisationService,
            EventCertificateService eventCertificateService,
            ResultListService resultListService,
            SplitTimeListRepository splitTimeListRepository) {
        this.importService = importService;
        this.organisationService = organisationService;
        this.eventCertificateService = eventCertificateService;
        this.resultListService = resultListService;
        this.splitTimeListRepository = splitTimeListRepository;
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<ApiResponse<EventDto>> handleFileUpload(
            @RequestParam(FILE) MultipartFile file,
            HttpServletRequest request) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Uploaded file is empty");
            }

            // Import file
            XMLImportService.ImportResult result = importService.importFile(file.getInputStream());
            Event event = result.event();

            // Convert to DTO
            Boolean hasSplitTimes = hasSplitTimes(event);
            EventDto eventDto = EventDto.from(event, organisationService, eventCertificateService, hasSplitTimes);

            log.info("Successfully imported event: {}", eventDto.name());
            return ResponseUtil.success(
                    eventDto,
                    LocalizableString.of(MessageKeys.SUCCESSFULLY_RETRIEVED),
                    request.getRequestURI()
            );

        } catch (IOException e) {
            log.error("Failed to read uploaded file", e);
            throw new IllegalArgumentException("Failed to read uploaded file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error importing XML file", e);
            throw new IllegalArgumentException("Failed to import event: " + e.getMessage(), e);
        }
    }

    private Boolean hasSplitTimes(Event event) {
        return resultListService.findByEventId(event.getId()).stream()
                .anyMatch(resultList -> !splitTimeListRepository.findByResultListId(resultList.getId()).isEmpty());
    }

}
