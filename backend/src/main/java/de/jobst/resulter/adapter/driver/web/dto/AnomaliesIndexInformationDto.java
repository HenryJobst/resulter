package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.AnomaliesIndexInformation;
import de.jobst.resulter.domain.analysis.CheatingClassification;

public record AnomaliesIndexInformationDto(int legNumber, String fromControl, String toControl,
                                           CheatingClassification classification) {
    static AnomaliesIndexInformationDto from(AnomaliesIndexInformation anomaliesIndexInformation) {
        return new AnomaliesIndexInformationDto(anomaliesIndexInformation.legNumber(),
            anomaliesIndexInformation.fromControl().value(),
            anomaliesIndexInformation.toControl().value(),
            anomaliesIndexInformation.classification());
    }
}
