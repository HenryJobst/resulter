package de.jobst.resulter.domain.util;

import lombok.Setter;

import java.util.function.Supplier;

public class ShallowLoadProxy<T> {

    private T value = null;
    @Setter
    private Supplier<T> loader;
    private boolean isLoaded = false;

    public ShallowLoadProxy() {
        this.loader = null;
    }

    public ShallowLoadProxy(T value) {
        this.value = value;
        this.isLoaded = true;
        this.loader = null;
    }

    public ShallowLoadProxy(Supplier<T> loader) {
        this.loader = loader;
    }

    public T get() {

        if (!isLoaded && null != loader) {
            value = load();
        }

        if (isLoaded) {
            return value;
        } else {
            throw new DataNotLoadedException("Data not loaded, get not possible.");
        }
    }

    private T load() {
        try {
            T loadedValue = loader.get();
            isLoaded = true;
            return loadedValue;
        } catch (Exception e) {
            throw new DataLoadException("Error occurred on data loading.", e);
        }
    }

    public void reset() {
        isLoaded = false;
        value = null;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public boolean isEmpty() {
        return !isLoaded();
    }

    public static <T> ShallowLoadProxy<T> empty() {
        return new ShallowLoadProxy<>();
    }

    public static <T> ShallowLoadProxy<T> of(T value) {
        return new ShallowLoadProxy<T>(value);
    }
}

