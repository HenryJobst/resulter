package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Country;
import org.apache.commons.lang3.ObjectUtils;

public record CountryDto(Long id,
                         String name,
                         String code) {
    static public CountryDto from(Country country) {
        return new CountryDto(
                ObjectUtils.isNotEmpty(country.getId()) ?
                        country.getId().value() : 0,
                country.getName().value(),
                country.getCode().value()
        );
    }
}
