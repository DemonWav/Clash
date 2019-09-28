package com.demonwav.clash;

public interface Creator<T> {
    T createDefault(final String fieldName);
}
