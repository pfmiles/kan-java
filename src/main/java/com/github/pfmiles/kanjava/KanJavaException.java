/*******************************************************************************
 * Copyright (c) 2015 pf-miles.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     pf-miles - initial API and implementation
 ******************************************************************************/
package com.github.pfmiles.kanjava;

/**
 * 发生未预料的系统错误
 * 
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
