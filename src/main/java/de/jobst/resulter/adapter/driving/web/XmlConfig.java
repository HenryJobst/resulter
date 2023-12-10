package de.jobst.resulter.adapter.driving.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class XmlConfig {

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        //marshaller.setClassesToBeBound(RaceCourseData.class);
        dmarshaller.setPackagesToScan("de.jobst.resulter.adapter.driving.web.jaxb");
        return marshaller;
    }
}
