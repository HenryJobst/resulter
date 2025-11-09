package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Country;
import org.apache.commons.lang3.ObjectUtils;

public record CountryKeyDto(Long id, String name) {

    static public CountryKeyDto from(Country country) {
        return new CountryKeyDto(ObjectUtils.isNotEmpty(country.getId()) ? country.getId().value() : 0,
            country.getName().value());
    }
}
