package de.jobst.resulter.application.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SegmentTimeAndMistakeResultTest {

    // -------------------------------------------------------------------------
    // SegmentTime
    // -------------------------------------------------------------------------

    @Test
    void segmentTime_accessorsReturnCorrectValues() {
        SegmentTime seg = new SegmentTime(3, "K1", "K2", 45.5);
        assertThat(seg.legNumber()).isEqualTo(3);
        assertThat(seg.fromControl()).isEqualTo("K1");
        assertThat(seg.toControl()).isEqualTo("K2");
        assertThat(seg.timeSeconds()).isEqualTo(45.5);
    }

    @Test
    void segmentTime_equalsAndHashCode() {
        SegmentTime a = new SegmentTime(1, "S", "F", 60.0);
        SegmentTime b = new SegmentTime(1, "S", "F", 60.0);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void segmentTime_toStringContainsValues() {
        SegmentTime seg = new SegmentTime(2, "K3", "K4", 30.0);
        assertThat(seg.toString()).contains("K3").contains("K4");
    }

    // -------------------------------------------------------------------------
    // MistakeResult (package-private record)
    // -------------------------------------------------------------------------

    @Test
    void mistakeResult_accessorsReturnCorrectValues() {
        MistakeResult mr = new MistakeResult(35.0, 120.5, true);
        assertThat(mr.diffPercent()).isEqualTo(35.0);
        assertThat(mr.timeLossSeconds()).isEqualTo(120.5);
        assertThat(mr.isMistake()).isTrue();
    }

    @Test
    void mistakeResult_notAMistake() {
        MistakeResult mr = new MistakeResult(10.0, 15.0, false);
        assertThat(mr.isMistake()).isFalse();
    }

    @Test
    void mistakeResult_equalsAndHashCode() {
        MistakeResult a = new MistakeResult(20.0, 50.0, true);
        MistakeResult b = new MistakeResult(20.0, 50.0, true);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
