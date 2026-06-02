package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.SegmentPI;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.PerformanceIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SplitTimeAnalysisServiceImplTest {

    SplitTimeAnalysisServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SplitTimeAnalysisServiceImpl();
    }

    // -------------------------------------------------------------------------
    // buildRuntimeMap
    // -------------------------------------------------------------------------

    @Test
    void buildRuntimeMap_returnsEmptyMap_whenClassResultsNull() {
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.of(1L),
                null, null, null, null);
        assertThat(service.buildRuntimeMap(rl)).isEmpty();
    }

    @Test
    void buildRuntimeMap_returnsEmptyMap_whenNoRaceResults() {
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.of(1L),
                null, null, null, List.of());
        assertThat(service.buildRuntimeMap(rl)).isEmpty();
    }

    @Test
    void buildRuntimeMap_populatesMapWithValidRuntime() {
        PersonRaceResult prr = PersonRaceResult.of("H21", 42L, null, null, 300.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult pr = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(42L), null, List.of(prr));
        ClassResult cr = ClassResult.of("Herren 21", "H21", Gender.M, List.of(pr), null);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.of(1L),
                null, null, null, List.of(cr));

        Map<RuntimeKey, Double> map = service.buildRuntimeMap(rl);

        assertThat(map).hasSize(1);
        assertThat(map).containsKey(new RuntimeKey(42L, "H21", 1));
        assertThat(map.get(new RuntimeKey(42L, "H21", 1))).isEqualTo(300.0);
    }

    @Test
    void buildRuntimeMap_skipsZeroRuntime() {
        PersonRaceResult prr = PersonRaceResult.of("H21", 1L, null, null, 0.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult pr = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr));
        ClassResult cr = ClassResult.of("Herren 21", "H21", Gender.M, List.of(pr), null);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.of(1L),
                null, null, null, List.of(cr));

        assertThat(service.buildRuntimeMap(rl)).isEmpty();
    }

    // -------------------------------------------------------------------------
    // countRunnersPerClass
    // -------------------------------------------------------------------------

    @Test
    void countRunnersPerClass_returnsEmptyMap_whenNoLists() {
        assertThat(service.countRunnersPerClass(List.of())).isEmpty();
    }

    @Test
    void countRunnersPerClass_countsCorrectly() {
        SplitTimeList stl1 = splitTimeList("H21", 1L, List.of());
        SplitTimeList stl2 = splitTimeList("H21", 2L, List.of());
        SplitTimeList stl3 = splitTimeList("D21", 3L, List.of());

        Map<String, Integer> counts = service.countRunnersPerClass(List.of(stl1, stl2, stl3));

        assertThat(counts).containsEntry("H21", 2).containsEntry("D21", 1);
    }

    // -------------------------------------------------------------------------
    // calculateSegmentTimes
    // -------------------------------------------------------------------------

    @Test
    void calculateSegmentTimes_returnsEmpty_whenNoSplitsAndNoRuntime() {
        SplitTimeList stl = splitTimeList("H21", 1L, List.of());
        Map<RuntimeKey, Double> runtimeMap = Map.of();

        List<SegmentTime> result = service.calculateSegmentTimes(stl, runtimeMap);

        assertThat(result).isEmpty();
    }

    @Test
    void calculateSegmentTimes_returnsSingleSegment_whenOnlyRuntime() {
        SplitTimeList stl = splitTimeList("H21", 1L, List.of());
        Map<RuntimeKey, Double> runtimeMap = Map.of(new RuntimeKey(1L, "H21", 1), 300.0);

        List<SegmentTime> result = service.calculateSegmentTimes(stl, runtimeMap);

        // S→F: 300s
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().fromControl()).isEqualTo("S");
        assertThat(result.getFirst().toControl()).isEqualTo("F");
    }

    @Test
    void calculateSegmentTimes_calculatesSegmentsCorrectly() {
        SplitTimeList stl = splitTimeList("H21", 1L, List.of(
                SplitTime.of("31", 60.0, SplitTimeListId.empty()),
                SplitTime.of("32", 150.0, SplitTimeListId.empty())
        ));
        Map<RuntimeKey, Double> runtimeMap = Map.of(new RuntimeKey(1L, "H21", 1), 200.0);

        List<SegmentTime> segments = service.calculateSegmentTimes(stl, runtimeMap);

        // S→31: 60s, 31→32: 90s, 32→F: 50s
        assertThat(segments).hasSize(3);
        assertThat(segments.get(0).fromControl()).isEqualTo("S");
        assertThat(segments.get(0).toControl()).isEqualTo("31");
        assertThat(segments.get(0).timeSeconds()).isEqualTo(60.0);
        assertThat(segments.get(1).timeSeconds()).isEqualTo(90.0);
        assertThat(segments.get(2).toControl()).isEqualTo("F");
        assertThat(segments.get(2).timeSeconds()).isEqualTo(50.0);
    }

    @Test
    void calculateSegmentTimes_skipsSegmentWithNoRuntime() {
        SplitTimeList stl = splitTimeList("H21", 1L, List.of(
                SplitTime.of("31", 60.0, SplitTimeListId.empty())
        ));
        // No runtime in map → finish not added
        Map<RuntimeKey, Double> runtimeMap = Map.of();

        List<SegmentTime> segments = service.calculateSegmentTimes(stl, runtimeMap);

        // Only S→31
        assertThat(segments).hasSize(1);
        assertThat(segments.getFirst().toControl()).isEqualTo("31");
    }

    // -------------------------------------------------------------------------
    // calculateAllTimesPerSegment
    // -------------------------------------------------------------------------

    @Test
    void calculateAllTimesPerSegment_collectsTimesPerSegmentKey() {
        SplitTimeList stl1 = splitTimeList("H21", 1L, List.of(
                SplitTime.of("31", 60.0, SplitTimeListId.empty())
        ));
        SplitTimeList stl2 = splitTimeList("H21", 2L, List.of(
                SplitTime.of("31", 70.0, SplitTimeListId.empty())
        ));
        Map<RuntimeKey, Double> runtimeMap = Map.of(
                new RuntimeKey(1L, "H21", 1), 100.0,
                new RuntimeKey(2L, "H21", 1), 110.0
        );

        Map<SegmentKey, List<Double>> result = service.calculateAllTimesPerSegment(
                List.of(stl1, stl2), runtimeMap);

        SegmentKey sToControl = new SegmentKey("H21", "S", "31");
        assertThat(result).containsKey(sToControl);
        assertThat(result.get(sToControl)).containsExactlyInAnyOrder(60.0, 70.0);
    }

    // -------------------------------------------------------------------------
    // calculateReferenceTimesPerSegment
    // -------------------------------------------------------------------------

    @Test
    void calculateReferenceTimesPerSegment_averagesTopThree() {
        // 5 runners on same segment: 10, 20, 30, 40, 50 → top 3 avg = (10+20+30)/3 = 20
        List<SplitTimeList> lists = List.of(
                splitTimeListWithSplit("H21", 1L, "31", 10.0),
                splitTimeListWithSplit("H21", 2L, "31", 20.0),
                splitTimeListWithSplit("H21", 3L, "31", 30.0),
                splitTimeListWithSplit("H21", 4L, "31", 40.0),
                splitTimeListWithSplit("H21", 5L, "31", 50.0)
        );
        Map<RuntimeKey, Double> runtimeMap = Map.of(
                new RuntimeKey(1L, "H21", 1), 100.0,
                new RuntimeKey(2L, "H21", 1), 100.0,
                new RuntimeKey(3L, "H21", 1), 100.0,
                new RuntimeKey(4L, "H21", 1), 100.0,
                new RuntimeKey(5L, "H21", 1), 100.0
        );

        Map<SegmentKey, Double> refs = service.calculateReferenceTimesPerSegment(lists, runtimeMap);

        SegmentKey key = new SegmentKey("H21", "S", "31");
        assertThat(refs).containsKey(key);
        assertThat(refs.get(key)).isEqualTo(20.0);
    }

    @Test
    void calculateReferenceTimesPerSegment_returnsEmpty_whenNoSegmentTimes() {
        Map<SegmentKey, Double> refs = service.calculateReferenceTimesPerSegment(List.of(), Map.of());
        assertThat(refs).isEmpty();
    }

    // -------------------------------------------------------------------------
    // calculateSegmentPIs
    // -------------------------------------------------------------------------

    @Test
    void calculateSegmentPIs_skipsSegmentsWithoutReferenceTime() {
        List<SegmentTime> segs = List.of(new SegmentTime(0, "S", "31", 60.0));
        Map<SegmentKey, Double> refs = Map.of(); // no reference

        List<SegmentPI> pis = service.calculateSegmentPIs(segs, refs, "H21");

        assertThat(pis).isEmpty();
    }

    @Test
    void calculateSegmentPIs_calculatesPI() {
        List<SegmentTime> segs = List.of(new SegmentTime(0, "S", "31", 120.0));
        Map<SegmentKey, Double> refs = Map.of(new SegmentKey("H21", "S", "31"), 60.0);

        List<SegmentPI> pis = service.calculateSegmentPIs(segs, refs, "H21");

        assertThat(pis).hasSize(1);
        assertThat(pis.getFirst().pi().value()).isEqualTo(2.0); // 120/60
    }

    // -------------------------------------------------------------------------
    // calculateMedian
    // -------------------------------------------------------------------------

    @Test
    void calculateMedian_returnsNull_whenEmpty() {
        assertThat(service.calculateMedian(List.of())).isNull();
    }

    @Test
    void calculateMedian_returnsSingleValue() {
        assertThat(service.calculateMedian(List.of(5.0))).isEqualTo(5.0);
    }

    @Test
    void calculateMedian_returnsMiddleValue_oddCount() {
        assertThat(service.calculateMedian(List.of(3.0, 1.0, 2.0))).isEqualTo(2.0);
    }

    @Test
    void calculateMedian_returnsAverageOfMiddleTwo_evenCount() {
        assertThat(service.calculateMedian(List.of(1.0, 2.0, 3.0, 4.0))).isEqualTo(2.5);
    }

    // -------------------------------------------------------------------------
    // isMistakeBase / isMistake
    // -------------------------------------------------------------------------

    @Test
    void isMistakeBase_detectsMistake_whenBothThresholdsExceeded() {
        // PI = 1.60 → diff = 60%, medianDiff = 10% → diff > 10+25=35% AND timeLoss = refTime*(pi-1)
        SegmentPI seg = new SegmentPI(0, "S", "31", 160.0, 100.0, new PerformanceIndex(1.60));
        MistakeResult result = service.isMistakeBase(seg, 10.0);

        assertThat(result.isMistake()).isTrue();
        assertThat(result.diffPercent()).isCloseTo(60.0, org.assertj.core.data.Offset.offset(0.001));
        assertThat(result.timeLossSeconds()).isCloseTo(60.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void isMistakeBase_notMistake_whenRelativeThresholdNotExceeded() {
        // PI = 1.10 → diff = 10%, medianDiff = 0% → 10 <= 0+25=25 → no mistake
        SegmentPI seg = new SegmentPI(0, "S", "31", 110.0, 100.0, new PerformanceIndex(1.10));
        MistakeResult result = service.isMistakeBase(seg, 0.0);

        assertThat(result.isMistake()).isFalse();
    }

    @Test
    void isMistakeBase_notMistake_whenAbsoluteThresholdNotExceeded() {
        // diff > median+25, but timeLoss = 5s < 30s → not a mistake
        SegmentPI seg = new SegmentPI(0, "S", "31", 35.0, 30.0, new PerformanceIndex(35.0 / 30.0));
        MistakeResult result = service.isMistakeBase(seg, 0.0);

        assertThat(result.isMistake()).isFalse();
    }

    @Test
    void isMistake_wrapsIsMistakeBase() {
        SegmentPI seg = new SegmentPI(0, "S", "31", 160.0, 100.0, new PerformanceIndex(1.60));
        assertThat(service.isMistake(seg, 10.0)).isTrue();
    }

    // -------------------------------------------------------------------------
    // calculateNormalPI
    // -------------------------------------------------------------------------

    @Test
    void calculateNormalPI_returnsNull_whenTooFewSegments() {
        List<SegmentPI> segs = List.of(
                segPI("S", "31", 1.1),
                segPI("31", "32", 1.0)
        );
        assertThat(service.calculateNormalPI(segs)).isNull();
    }

    @Test
    void calculateNormalPI_calculatesFromNonMistakeSegments() {
        // 5 segments, all with PI=1.1 (10% slower than reference, no mistakes)
        List<SegmentPI> segs = List.of(
                segPI("S", "31", 1.1),
                segPI("31", "32", 1.1),
                segPI("32", "33", 1.1),
                segPI("33", "34", 1.1),
                segPI("34", "F", 1.1)
        );
        PerformanceIndex normalPI = service.calculateNormalPI(segs);

        assertThat(normalPI).isNotNull();
        assertThat(normalPI.value()).isCloseTo(1.1, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void calculateNormalPI_returnsNull_whenTooFewNonMistakeSegments() {
        // 4 segments: 2 at PI=1.0 (normal) + 2 big outliers at PI=5.0 (timeLoss=400s)
        // median diff = 200%, threshold = 225%, outlier diff=400% > 225% AND 400s > 30s → MISTAKE
        // → only 2 non-mistake segments < MIN_NON_MISTAKE_SEGMENTS=3 → returns null
        List<SegmentPI> segs = List.of(
                new SegmentPI(0, "S", "31", 100.0, 100.0, new PerformanceIndex(1.0)),
                new SegmentPI(1, "31", "32", 100.0, 100.0, new PerformanceIndex(1.0)),
                new SegmentPI(2, "32", "33", 500.0, 100.0, new PerformanceIndex(5.0)),
                new SegmentPI(3, "33", "34", 500.0, 100.0, new PerformanceIndex(5.0))
        );
        assertThat(service.calculateNormalPI(segs)).isNull();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SplitTimeList splitTimeList(String className, long personId, List<SplitTime> splits) {
        return new SplitTimeList(
                SplitTimeListId.empty(),
                EventId.of(1L),
                ResultListId.of(1L),
                ClassResultShortName.of(className),
                PersonId.of(personId),
                RaceNumber.of((byte) 1),
                splits
        );
    }

    private SplitTimeList splitTimeListWithSplit(String className, long personId,
                                                  String control, double time) {
        return splitTimeList(className, personId,
                List.of(SplitTime.of(control, time, SplitTimeListId.empty())));
    }

    private SegmentPI segPI(String from, String to, double piValue) {
        double ref = 100.0;
        double runner = ref * piValue;
        return new SegmentPI(0, from, to, runner, ref, new PerformanceIndex(piValue));
    }
}
