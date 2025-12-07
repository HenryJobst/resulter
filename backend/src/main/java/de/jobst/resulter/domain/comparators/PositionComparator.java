package de.jobst.resulter.domain.comparators;

import de.jobst.resulter.domain.Position;

import java.util.Comparator;

public class PositionComparator {

    public final static Comparator<Position> COMPARATOR = Comparator.comparing(Position::value,
    Comparator.nullsLast(Comparator.naturalOrder()));
}
