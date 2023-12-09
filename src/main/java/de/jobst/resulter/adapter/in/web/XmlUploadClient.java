package de.jobst.resulter.adapter.in.web;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.MessageFormat;

public class XmlUploadClient {

    public static void main(String[] args) {
        String filePath = "import_files/Zwischenzeiten_IOFv3_WinterOL.xml";
        String uploadUrl = "http://localhost:8080/upload";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        FileSystemResource fileSystemResource = new FileSystemResource(new File(filePath));

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(XmlController.FILE, fileSystemResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String>
                response =
                restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

        System.out.println(MessageFormat.format("Response-Code: {0}\n{1}",
                response.getStatusCode(),
                response.hasBody() ? response.getBody() : ""));
    }
}

