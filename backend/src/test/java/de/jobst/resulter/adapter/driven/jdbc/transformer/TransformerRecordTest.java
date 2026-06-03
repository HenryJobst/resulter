package de.jobst.resulter.adapter.driven.jdbc.transformer;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.ExampleMatcher;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TransformerRecordTest {

    @Test
    void mappingFilterNodeTransformResult_twoArgConstructor_setsFields() {
        Map<String, ValueWithMatcher> map = Map.of("k", new ValueWithMatcher("v", ExampleMatcher.StringMatcher.CONTAINING));
        MappingFilterNodeTransformResult result = new MappingFilterNodeTransformResult("filter", map);
        assertThat(result.filterString()).isEqualTo("filter");
        assertThat(result.filterMap()).isEqualTo(map);
    }

    @Test
    void mappingFilterNodeTransformResult_oneArgConstructor_usesEmptyMap() {
        MappingFilterNodeTransformResult result = new MappingFilterNodeTransformResult("where x = 1");
        assertThat(result.filterString()).isEqualTo("where x = 1");
        assertThat(result.filterMap()).isEmpty();
    }

    @Test
    void valueWithMatcher_accessorsReturnCorrectValues() {
        ValueWithMatcher vm = new ValueWithMatcher("hello", ExampleMatcher.StringMatcher.STARTING);
        assertThat(vm.value()).isEqualTo("hello");
        assertThat(vm.matcher()).isEqualTo(ExampleMatcher.StringMatcher.STARTING);
    }

    @Test
    void valueWithMatcher_equalsAndHashCode() {
        ValueWithMatcher a = new ValueWithMatcher("x", ExampleMatcher.StringMatcher.EXACT);
        ValueWithMatcher b = new ValueWithMatcher("x", ExampleMatcher.StringMatcher.EXACT);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
