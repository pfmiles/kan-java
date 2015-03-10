package com.github.pfmiles.kanjava.compile;

/**
 * @author pf-miles
 * 
 */
class DynaCompilationException extends RuntimeException {

    DynaCompilationException() {
        super();
    }

    DynaCompilationException(String message, Throwable cause) {
        super(message, cause);
    }

    DynaCompilationException(String message) {
        super(message);
    }

    DynaCompilationException(Throwable cause) {
        super(cause);
    }
}
