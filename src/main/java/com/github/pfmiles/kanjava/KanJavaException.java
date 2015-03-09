package com.github.pfmiles.kanjava;

/**
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
public class KanJavaException extends RuntimeException {

    private static final long serialVersionUID = 6381049595271513234L;

    public KanJavaException() {
    }

    public KanJavaException(String errMsg) {
        super(errMsg);
    }

    public KanJavaException(String message, Throwable cause) {
        super(message, cause);
    }

    public KanJavaException(Throwable cause) {
        super(cause);
    }
}
