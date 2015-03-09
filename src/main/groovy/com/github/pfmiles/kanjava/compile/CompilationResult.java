package com.github.pfmiles.kanjava.compile;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.tools.JavaFileObject;

/**
 * 编译结果
 * 
 * @author pf-miles 2014-4-5 下午10:18:59
 */
public class CompilationResult {

    private String errMsg;
    private Set<JavaClassFile> classFiles;

    public CompilationResult(String errMsg) {
        this.errMsg = errMsg;
    }

    public CompilationResult(Iterable<JavaFileObject> list) {
        this.classFiles = new LinkedHashSet<JavaClassFile>();
        for (JavaFileObject f : list) {
            this.classFiles.add((JavaClassFile) f);
        }
    }

    /**
     * 编译是否出错
     */
    public boolean isError() {
        return errMsg != null;
    }

    /**
     * 获取错误信息
     */
    public String getErrMsg() {
        return this.errMsg;
    }

    /**
     * 获取编译好的class类文件，以内存文件的形式
     */
    public Set<JavaClassFile> getClassFiles() {
        return this.classFiles;
    }

}
