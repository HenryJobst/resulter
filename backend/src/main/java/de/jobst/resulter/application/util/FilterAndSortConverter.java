package de.jobst.resulter.application.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;

public class FilterAndSortConverter {

    public static Pageable mapOrderProperties(Pageable pageable, Function<Sort.Order, String> mapPropertyFunction) {
        List<Sort.Order> mappedOrders = pageable.getSort()
            .stream()
            .map(order -> new Sort.Order(order.getDirection(), mapPropertyFunction.apply(order)))
            .toList();
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(mappedOrders));
    }
}
