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

import javax.tools.JavaFileObject

/**
 * 编译结果
 * 
 * @author pf-miles 2014-4-5 下午10:18:59
 */
class CompilationResult {

    String errMsg;
    Set<JavaClassFile> classFiles;

    CompilationResult(String errMsg) {
        this.errMsg = errMsg;
    }

    CompilationResult(Iterable<JavaFileObject> list) {
        this.classFiles = new LinkedHashSet<JavaClassFile>();
        list.each { this.classFiles << it }
    }

    /**
     * 编译是否出错
     */
    boolean isError() {
        errMsg != null;
    }

    /**
     * 获取错误信息
     */
    String getErrMsg() {
        this.errMsg;
    }

    /**
     * 获取编译好的class类文件，以内存文件的形式
     */
    Set<JavaClassFile> getClassFiles() {
        this.classFiles;
    }
}
