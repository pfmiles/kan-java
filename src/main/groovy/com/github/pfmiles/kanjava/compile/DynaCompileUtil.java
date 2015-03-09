package com.github.pfmiles.kanjava.compile;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.github.pfmiles.kanjava.DiskJarFile;
import com.github.pfmiles.kanjava.JavaSourceFile;

/**
 * 将传入的内存java文件结合内存classpath，编译为内存class文件
 * 
 * @author pf-miles 2014-4-3 下午4:29:31
 */
public class DynaCompileUtil {

    /**
     * 传入源码以及classpath中的jar包,编译出class文件(内存文件)
     * 
     * @param srcs
     *            源码文件
     * @param cpJarFiles
     *            准备放入classpath的jar包(存在于硬盘上)，可为null
     * @return 编译结果
     */
    public static CompilationResult compile(Set<JavaSourceFile> srcs, Set<DiskJarFile> cpJarFiles, Iterable<? extends Processor> processors) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DynaFileManager fileManager = new DynaFileManager(compiler.getStandardFileManager(null, null, null));
        try {
            List<String> options = Arrays.asList("-classpath", toClassPathStr(cpJarFiles));
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
            StringWriter out = new StringWriter();
            CompilationTask task = compiler.getTask(out, fileManager, diagnostics, options, null, srcs);
            if (processors != null)
                task.setProcessors(processors);
            if (!task.call()) {
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    try {
                        out.append("Error on line " + diagnostic.getLineNumber() + " in " + diagnostic).append('\n')
                                .append("Code: \n" + diagnostic.getSource().getCharContent(true));
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

    // 生成动态编译的classpath，应包括所有传入的jar包
    private static String toClassPathStr(Set<DiskJarFile> cpJars) {
        Set<String> ps = new HashSet<String>();
        if (cpJars != null)
            for (DiskJarFile f : cpJars) {
                ps.add(f.getAbsolutePath());
            }

        StringBuilder sb = new StringBuilder();
        for (String s : ps) {
            if (sb.length() != 0)
                sb.append(File.pathSeparator);
            sb.append(s);
        }
        return sb.toString();
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
}
