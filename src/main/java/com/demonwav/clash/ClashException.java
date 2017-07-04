package com.demonwav.clash;

public class ClashException extends RuntimeException {

    ClashException(final Exception e) {
        super(e);
    }

    ClashException(final String message) {
        super(message);
    }

    // These two classes basically just exist to assist in testing
    public static class Dashes extends ClashException {
        Dashes(String message) {
            super(message);
        }
    }

    public static class Whitespace extends ClashException {

        Whitespace(String message) {
            super(message);
        }
    }
}
