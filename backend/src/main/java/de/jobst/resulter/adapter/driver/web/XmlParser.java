package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.jaxb.ResultList;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

@Component
public class XmlParser {

    private final Unmarshaller unmarshaller;

    public XmlParser(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public ResultList parseXmlFile(InputStream inputStream) throws Exception {
        StreamSource source = new StreamSource(inputStream);
        return (ResultList) unmarshaller.unmarshal(source);
    }
}

