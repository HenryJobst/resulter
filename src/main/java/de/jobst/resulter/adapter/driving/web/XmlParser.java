package de.jobst.resulter.adapter.driving.web;

import de.jobst.resulter.adapter.driving.web.jaxb.ResultList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

@Component
public class XmlParser {

    private final Unmarshaller unmarshaller;

    @Autowired
    public XmlParser(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public ResultList parseXmlFile(InputStream inputStream) throws Exception {
        StreamSource source = new StreamSource(inputStream);
        return (ResultList) unmarshaller.unmarshal(source);
    }
}

