package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.domain.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class XmlController {

    public static final String FILE = "file";

    private final XMLImportService importService;

    @Autowired
    public XmlController(XMLImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@RequestParam(FILE) MultipartFile file) {
        try {
            Event event = importService.importFile(file.getInputStream()).event();
            return ResponseEntity.ok(event.toString());
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return ResponseEntity.internalServerError().build();
        }
    }

}
