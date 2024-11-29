package de.jobst.resulter.application.certificate;

public record DocumentDefinition(MarginsDefinition margins, String font, String boldFont, String italicFont,
                                 String boldItalicFont) {}
