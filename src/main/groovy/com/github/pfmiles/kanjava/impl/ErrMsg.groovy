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
