package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.CountryKeyDto;
import de.jobst.resulter.domain.Country;
import org.apache.commons.lang3.ObjectUtils;

public class CountryMapper {

    private CountryMapper() {
        // Utility class
    }

    public static CountryKeyDto toKeyDto(Country country) {
        return new CountryKeyDto(
                ObjectUtils.isNotEmpty(country.getId()) ? country.getId().value() : 0,
                country.getName().value());
    }
}
