package de.jobst.resulter.application.certificate;

public record TextBlock(Text text) implements Block {

    public record RGBColor(int r, int g, int b) {}

    public record Text(String content, String font, boolean bold, boolean italic, Float fontSize, RGBColor color) {}

}
