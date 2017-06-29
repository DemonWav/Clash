package com.demonwav.clash;

class ClashException extends RuntimeException {

    ClashException(final Exception e) {
        super(e);
    }

    ClashException(final String message) {
        super(message);
    }
}
