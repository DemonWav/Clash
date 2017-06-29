package com.demonwav.clash;

public interface Initializer<T> {
    T initialize(String fieldName, String value);
}
