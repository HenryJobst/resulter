package de.jobst.resulter.adapter.driver.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class XmlUploadClient {

    public static void main(String[] args) {
        List<String> filePathee = Arrays.asList(
               /* "import_files/Zwischenzeiten Kat 7.MKOL.xml",
                "import_files/schorf23_ergsi.xml",
                "import_files/40_FrühlingsOL_Kategorienn_Zwischenzeiten_30.xml",
                "import_files/Zwischenzeiten_IOFv3_WinterOL.xml",
                "import_files/14.Otto-Spahn-OL_Ergebnisse-SI.xml",
                "import_files/20230903_194236_Zwischenzeiten.xml",
                "import_files/20231015_Herbst-OL_oe.xml",
                "import_files/32nebel_tag_SI.xml",
                "import_files/48_Teu_Ergeb_SI_Kat.xml"
                */
        );

        String uploadUrl = "http://localhost:8080/api/upload";

        filePathee.forEach((filePath) -> {
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

            String responseBody = response.hasBody() ? response.getBody() : "";

            if (StringUtils.isNotEmpty(responseBody)) {

            }
            System.out.println(MessageFormat.format("Response-Code: {0}\n{1}",
                    response.getStatusCode(),
                    responseBody));
        });
    }
}

