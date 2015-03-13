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
