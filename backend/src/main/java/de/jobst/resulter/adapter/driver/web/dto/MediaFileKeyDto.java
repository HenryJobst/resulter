package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.adapter.driver.web.constraints.ValidId;

public record MediaFileKeyDto(@ValidId Long id, String fileName, String thumbnailContent) {}
