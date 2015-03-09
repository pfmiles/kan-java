package com.github.pfmiles.kanjava.compile;

/**
 * @author pf-miles
 * 
 */
public class DynaCompilationException extends RuntimeException {

    private static final long serialVersionUID = 1709191981044609320L;

    public DynaCompilationException() {
        super();
    }

    public DynaCompilationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynaCompilationException(String message) {
        super(message);
    }

    public DynaCompilationException(Throwable cause) {
        super(cause);
    }

}
