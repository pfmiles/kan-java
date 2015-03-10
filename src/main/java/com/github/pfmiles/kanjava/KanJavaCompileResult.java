package com.github.pfmiles.kanjava;

import java.util.List;

/**
 * 
 * kan-java编译结果
 * 
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
public class KanJavaCompileResult {
    private String errMsg;
    private List<Class<?>> classes;

    /**
     * 取得编译错误信息
     */
    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    /**
     * 编译是否成功
     */
    public boolean isSuccess() {
        return errMsg == null || "".equals(errMsg.trim());
    }

    /**
     * 取得编译好的内存class文件
     */
    public List<Class<?>> getClasses() {
        return classes;
    }

    public void setClasses(List<Class<?>> classes) {
        this.classes = classes;
    }

}
