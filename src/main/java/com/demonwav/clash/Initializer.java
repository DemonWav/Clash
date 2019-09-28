package com.demonwav.clash;

public interface Initializer<T> {
    T initialize(final String fieldName, final String value);
}
