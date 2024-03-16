package de.jobst.resulter.application.certificate;

public record MediaParagraph(int marginTop, int marginLeft, int marginBottom, int width, String media)
    implements Paragraph {}
