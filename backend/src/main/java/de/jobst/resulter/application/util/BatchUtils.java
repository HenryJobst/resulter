package de.jobst.resulter.application.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class BatchUtils {

    public static final int DEFAULT_BATCH_SIZE = 500;

    private BatchUtils() {}

    public static <T> void processInBatches(Collection<T> items, Consumer<List<T>> batchProcessor) {
        processInBatches(items, DEFAULT_BATCH_SIZE, batchProcessor);
    }

    public static <T> void processInBatches(Collection<T> items, int batchSize, Consumer<List<T>> batchProcessor) {
        if (items == null || items.isEmpty()) {
            return;
        }

        List<T> itemList = items instanceof List ? (List<T>) items : new ArrayList<>(items);

        for (int start = 0; start < itemList.size(); start += batchSize) {
            int end = Math.min(start + batchSize, itemList.size());
            List<T> batch = itemList.subList(start, end);
            batchProcessor.accept(batch);
        }
    }
}
