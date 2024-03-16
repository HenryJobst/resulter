package de.jobst.resulter.application.certificate;

public record TextParagraph(int marginTop, int marginLeft, int fontSize, boolean bold, String text)
    implements Paragraph {}
