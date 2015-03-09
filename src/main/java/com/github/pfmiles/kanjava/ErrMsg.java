package com.github.pfmiles.kanjava;

/**
 * 
 * 错误信息
 * 
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
public class ErrMsg {

    private long row;
    private long col;
    private String msg;

    public ErrMsg(long row, long col, String msg) {
        this.row = row;
        this.col = col;
        this.msg = msg;
    }

    public long getRow() {
        return row;
    }

    public void setRow(long row) {
        this.row = row;
    }

    public long getCol() {
        return col;
    }

    public void setCol(long col) {
        this.col = col;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String toString() {
        return "Error at row: " + row + ", col: " + col + ", reason: " + msg;
    }

}
