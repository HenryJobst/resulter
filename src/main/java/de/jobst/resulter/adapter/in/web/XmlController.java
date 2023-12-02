package de.jobst.resulter.adapter.in.web;

import de.jobst.resulter.adapter.in.web.jaxb.ResultList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Controller
@Slf4j
public class XmlController {

    public static final String FILE = "file";
    private final XmlParser xmlParser;

    @Autowired
    public XmlController(XmlParser xmlParser) {
        this.xmlParser = xmlParser;
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<ResultList> handleFileUpload(@RequestParam(FILE) MultipartFile file) {
        try {
            ResultList resultList = xmlParser.parseXmlFile(file.getInputStream());
            if (Objects.nonNull(resultList)) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
