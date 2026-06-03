package de.jobst.resulter.application.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationUtilTest {

    // -------------------------------------------------------------------------
    // BatchUtils
    // -------------------------------------------------------------------------

    @Test
    void batchUtils_nullItems_doesNothing() {
        AtomicInteger callCount = new AtomicInteger(0);
        BatchUtils.processInBatches(null, batch -> callCount.incrementAndGet());
        assertThat(callCount.get()).isZero();
    }

    @Test
    void batchUtils_emptyItems_doesNothing() {
        AtomicInteger callCount = new AtomicInteger(0);
        BatchUtils.processInBatches(List.of(), batch -> callCount.incrementAndGet());
        assertThat(callCount.get()).isZero();
    }

    @Test
    void batchUtils_itemsFitInOneBatch_singleCallWithAllItems() {
        List<Integer> collected = new ArrayList<>();
        BatchUtils.processInBatches(List.of(1, 2, 3), collected::addAll);
        assertThat(collected).containsExactly(1, 2, 3);
    }

    @Test
    void batchUtils_itemsSpanMultipleBatches_correctlySplit() {
        List<List<Integer>> batches = new ArrayList<>();
        List<Integer> items = List.of(1, 2, 3, 4, 5);
        BatchUtils.processInBatches(items, 2, batches::add);
        assertThat(batches).hasSize(3);
        assertThat(batches.get(0)).containsExactly(1, 2);
        assertThat(batches.get(1)).containsExactly(3, 4);
        assertThat(batches.get(2)).containsExactly(5);
    }

    @Test
    void batchUtils_nonListCollection_convertedToList() {
        List<Integer> collected = new ArrayList<>();
        // Set ist kein List → wird intern zu ArrayList konvertiert
        BatchUtils.processInBatches(Set.of(42), collected::addAll);
        assertThat(collected).containsExactly(42);
    }

    @Test
    void batchUtils_defaultBatchSizeConstant_is500() {
        assertThat(BatchUtils.DEFAULT_BATCH_SIZE).isEqualTo(500);
    }

    // -------------------------------------------------------------------------
    // FilterAndSortConverter
    // -------------------------------------------------------------------------

    @Test
    void filterAndSortConverter_canBeInstantiated() {
        assertThat(new FilterAndSortConverter()).isNotNull();
    }

    @Test
    void mapOrderProperties_mapsPropertyNamesCorrectly() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("firstName")));

        Pageable result = FilterAndSortConverter.mapOrderProperties(
                pageable, order -> "person_" + order.getProperty());

        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getSort().getOrderFor("person_firstName")).isNotNull();
        assertThat(result.getSort().getOrderFor("person_firstName").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void mapOrderProperties_multipleOrders_allMapped() {
        Pageable pageable = PageRequest.of(1, 20,
                Sort.by(Sort.Order.asc("lastName"), Sort.Order.desc("birthDate")));

        Pageable result = FilterAndSortConverter.mapOrderProperties(
                pageable, order -> "col_" + order.getProperty());

        assertThat(result.getPageNumber()).isEqualTo(1);
        List<Sort.Order> orders = result.getSort().toList();
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getProperty()).isEqualTo("col_lastName");
        assertThat(orders.get(1).getProperty()).isEqualTo("col_birthDate");
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
