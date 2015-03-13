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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import com.github.pfmiles.kanjava.JavaSourceFile;
import com.github.pfmiles.kanjava.KanJavaException;

/**
 * 将传入的内存java文件结合内存classpath，编译为内存class文件
 * 
 * @author pf-miles 2014-4-3 下午4:29:31
 */
public class DynaCompileUtil {

    public static final String PATH_SEP = "/";

    /**
     * 传入源码以及classpath中的jar包,编译出class文件(内存文件)
     * 
     * @param srcs
     *            源码文件
     * @param cpJarFilePathStr
     *            classpath字符串，可为null
     * @param procExe
     *            是否仅仅执行annotation processor或仅仅执行编译： 0: 既检查又编译, 1: 仅执行检查, 2:
     *            仅执行编译
     * @return 编译结果
     */
    public static CompilationResult compile(Set<JavaSourceFile> srcs, String cpJarFilePathStr, Iterable<? extends Processor> processors, int procExe) {
        // JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaCompiler compiler = null;
        try {
            compiler = Class.forName("com.sun.tools.javac.api.JavacTool").asSubclass(JavaCompiler.class).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DynaFileManager fileManager = new DynaFileManager(compiler.getStandardFileManager(null, null, null));
        try {
            List<String> options = new ArrayList<String>();
            if (cpJarFilePathStr != null) {
                options.add("-classpath");
                options.add(cpJarFilePathStr);
            }
            switch (procExe) {
            case 1:
                options.add("-proc:only");
                break;
            case 2:
                options.add("-proc:none");
            default:
                break;
            }
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
            StringWriter out = new StringWriter();
            CompilationTask task = compiler.getTask(out, fileManager, diagnostics, options, null, srcs);
            if (processors != null)
                task.setProcessors(processors);
            if (!task.call()) {
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    try {
                        out.append("Error on line " + diagnostic.getLineNumber() + " in " + diagnostic).append('\n')
                                .append("Code: \n" + diagnostic.getSource().getCharContent(true)).append("\n");
                    } catch (IOException e) {
                        // ignore...
                        out.append("Error on line " + diagnostic.getLineNumber() + " in " + diagnostic).append('\n');
                    }
                }
                return new CompilationResult(out.toString());
            } else {
                try {
                    return new CompilationResult(fileManager.list(StandardLocation.CLASS_OUTPUT, null, new HashSet<Kind>(Arrays.asList(Kind.CLASS)),
                            true));
                } catch (IOException e) {
                    throw new DynaCompilationException(e);
                }
            }
        } finally {
            try {
                fileManager.close();
            } catch (IOException e) {
                throw new DynaCompilationException(e);
            }
        }
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String substringBeforeLast(String str, String separator) {
        if (isEmpty(str) || isEmpty(separator)) {
            return str;
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(separator)) {
            return "";
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1 || pos == (str.length() - separator.length())) {
            return "";
        }
        return str.substring(pos + separator.length());
    }

    /**
     * Analyze class path for hot compilation
     * 
     * @return
     */
    public static String getClassPath() {
        StringBuilder sb = new StringBuilder();

        // include classpath system properties
        for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
            String k = (String) e.getKey();
            if (k.endsWith("class.path")) {
                if (sb.length() != 0)
                    sb.append(File.pathSeparator);
                sb.append(e.getValue());
            }
        }

        // if url class loader, include all classpath urls
        ClassLoader loader = getParentClsLoader();
        if (loader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) loader).getURLs()) {
                String dropinccPath = toFilePath(url);
                if (dropinccPath != null && !"".equals(dropinccPath) && sb.indexOf(dropinccPath) == -1) {
                    if (sb.length() != 0)
                        sb.append(File.pathSeparator);
                    sb.append(dropinccPath);
                }
            }
        }

        // include paths analyzed from file system
        URL url = DynaCompileUtil.class.getResource("DynaCompileUtil.class");
        if (url != null) {
            String dropinccPath = null;
            if ("jar".equalsIgnoreCase(url.getProtocol())) {
                String path = toFilePath(url);
                // could not handle nested jars
                dropinccPath = path != null ? path.substring(path.indexOf(":") + 1, path.indexOf("!")) : null;
            } else if ("file".equalsIgnoreCase(url.getProtocol())) {
                String path = toFilePath(url);
                dropinccPath = path != null ? path.substring(0,
                        path.lastIndexOf(PATH_SEP + DynaCompileUtil.class.getName().replace(".", PATH_SEP) + ".class")) : null;
            }
            if (dropinccPath != null && !"".equals(dropinccPath) && sb.indexOf(dropinccPath) == -1) {
                if (sb.length() != 0)
                    sb.append(File.pathSeparator);
                sb.append(dropinccPath);
            }
        }
        return sb.toString();
    }

    private static String toFilePath(URL url) {
        String protocal = url.getProtocol();
        if (!("jar".equalsIgnoreCase(protocal) || "file".equalsIgnoreCase(protocal)))
            return null;
        try {
            File f = new File(url.toURI().getSchemeSpecificPart());
            if (f.exists()) {
                return f.toURI().getSchemeSpecificPart();
            } else {
                return null;
            }
        } catch (URISyntaxException e) {
            throw new KanJavaException(e);
        }
    }

    /**
     * Get the proper parent class loader for hot compilation class loaders.
     * 
     * @return
     */
    public static ClassLoader getParentClsLoader() {
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        if (ctxLoader != null) {
            try {
                ctxLoader.loadClass(DynaCompileUtil.class.getName());
                return ctxLoader;
            } catch (ClassNotFoundException e) {
            }
        }
        return DynaCompileUtil.class.getClassLoader();
    }
}
