package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.domain.Event;
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

    public XmlController(XMLImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@RequestParam(FILE) MultipartFile file) {
        Event event;
        try {
            event = importService.importFile(file.getInputStream()).event();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(event.toString());
    }

}
