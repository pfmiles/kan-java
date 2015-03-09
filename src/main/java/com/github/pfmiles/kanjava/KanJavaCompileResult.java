package com.github.pfmiles.kanjava;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
public class KanJavaCompileResult {
    private String errMsg;
    private List<JavaClassFile> clsFiles;

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
        return errMsg == null || errMsg.trim() == "";
    }

    public void setCompiledClsFiles(Set<JavaClassFile> classFiles) {
        this.clsFiles = new ArrayList<JavaClassFile>(classFiles);
    }

    /**
     * 取得编译好的内存class文件
     * 
     */
    public List<JavaClassFile> getCompiledClassFiles() {
        return this.clsFiles;
    }

}
