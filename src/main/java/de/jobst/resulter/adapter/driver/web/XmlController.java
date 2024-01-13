package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.domain.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Controller
@CrossOrigin(origins = "http://localhost:5173")
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
            return new ResponseEntity<>(event.toString(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
