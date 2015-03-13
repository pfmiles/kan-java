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
package com.github.pfmiles.kanjava.impl;

/**
 * 
 * 错误信息
 * 
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
class ErrMsg {

    long row;
    long col;
    String msg;

    ErrMsg(long row, long col, String msg) {
        this.row = row;
        this.col = col;
        this.msg = msg;
    }

    def String toString(){
        "Error at row: " + row + ", col: " + col + ", reason: " + msg
    }
}
