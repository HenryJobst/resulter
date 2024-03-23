package de.jobst.resulter.application.certificate;

import java.util.List;
import java.util.Objects;

public record ParagraphDefinition(List<TabStopDefinition> tabStops, List<ParagraphDefinitionBlock> blocks,
                                  Integer marginTop, Integer marginBottom, Integer marginLeft, Integer marginRight) {

    public record TabStopDefinition(Float position, String alignment) {}

    public record ParagraphDefinitionBlock(Block block, Integer tabPosition) {

        int getTabPosition() {
            return Objects.requireNonNullElse(tabPosition, 1);
        }
    }
}
