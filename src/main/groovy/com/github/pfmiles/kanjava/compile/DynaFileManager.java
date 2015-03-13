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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import com.github.pfmiles.kanjava.JavaSourceFile;

/**
 * 虚拟一个内存中的文件系统，用作动态编译，其结构如下：
 * 
 * <pre>
 * &#47;
 * \....src/
 * \....target/
 * </pre>
 * 
 * 其中，src是源码目录，target是编译好的class文件目录
 * 
 * @author pf-miles 2014-4-6 下午4:29:14
 */
public class DynaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    // all source files, in 'src/ directory'
    private LinkedHashMap<String, Object> srcs = new LinkedHashMap<String, Object>();
    // all compiled class files, in 'target/ directory'
    private LinkedHashMap<String, Object> classes = new LinkedHashMap<String, Object>();

    protected DynaFileManager(StandardJavaFileManager fileManager) {
        super(fileManager);
    }

    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
        if (location instanceof StandardLocation) {
            switch ((StandardLocation) location) {
            case CLASS_OUTPUT:
                if (!kinds.contains(Kind.CLASS))
                    return Collections.emptyList();
                return listMemFile(this.classes, packageName, recurse);
            case CLASS_PATH:
                LinkedHashSet<JavaFileObject> fs = new LinkedHashSet<JavaFileObject>();
                if (kinds.contains(Kind.CLASS)) {
                    fs = listMemFile(this.classes, packageName, recurse);
                }
                for (JavaFileObject jfo : super.list(location, packageName, kinds, recurse)) {
                    fs.add(jfo);
                }
                return fs;
            case SOURCE_OUTPUT:
            case SOURCE_PATH:
                if (!kinds.contains(Kind.SOURCE))
                    return Collections.emptyList();
                return listMemFile(this.srcs, packageName, recurse);
            case ANNOTATION_PROCESSOR_PATH:
            case PLATFORM_CLASS_PATH:
            default:
                return super.list(location, packageName, kinds, recurse);
            }
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<JavaFileObject> listMemFile(LinkedHashMap<String, Object> memFiles, String pkg, boolean recurse) {
        LinkedHashSet<JavaFileObject> ret = new LinkedHashSet<JavaFileObject>();
        // cd pkgPath
        Deque<String> pkgPath = resolvePkgPath(pkg);
        while (!pkgPath.isEmpty()) {
            Object f = memFiles.get(pkgPath.pop());
            if (f == null || !(f instanceof Map)) {
                return ret;
            } else {
                memFiles = (LinkedHashMap<String, Object>) f;
            }
        }
        List<LinkedHashMap<String, Object>> subDirs = new ArrayList<LinkedHashMap<String, Object>>();
        for (Map.Entry<String, Object> e : memFiles.entrySet()) {
            Object f = e.getValue();
            if (f instanceof JavaFileObject) {
                ret.add((JavaFileObject) f);
            } else {
                subDirs.add((LinkedHashMap<String, Object>) f);
            }
        }
        if (recurse)
            ret.addAll(dumpJavaFileObjects(subDirs));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<JavaFileObject> dumpJavaFileObjects(List<LinkedHashMap<String, Object>> dirs) {
        LinkedHashSet<JavaFileObject> ret = new LinkedHashSet<JavaFileObject>();
        for (LinkedHashMap<String, Object> dir : dirs) {
            for (Map.Entry<String, Object> e : dir.entrySet()) {
                if (e.getValue() instanceof JavaFileObject) {
                    ret.add((JavaFileObject) e.getValue());
                } else {
                    ret.addAll(dumpJavaFileObjects(Arrays.asList((LinkedHashMap<String, Object>) e.getValue())));
                }
            }
        }
        return ret;
    }

    private Deque<String> resolvePkgPath(String pkg) {
        Deque<String> ret = new ArrayDeque<String>();
        if (pkg != null)
            for (String s : pkg.split("\\.")) {
                ret.add(s);
            }
        return ret;
    }

    public String inferBinaryName(Location location, JavaFileObject file) {
        if (StandardLocation.CLASS_OUTPUT.equals(location) || StandardLocation.CLASS_PATH.equals(location)
                || StandardLocation.SOURCE_OUTPUT.equals(location) || StandardLocation.SOURCE_PATH.equals(location)) {
            if (file instanceof JavaSourceFile) {
                return null;
            } else if (file instanceof JavaClassFile) {
                return ((JavaClassFile) file).getBinaryClassName();
            }
        }
        return super.inferBinaryName(location, file);
    }

    public boolean isSameFile(FileObject a, FileObject b) {
        if (a == b)
            return true;
        Class<?> ac = a.getClass();
        Class<?> bc = b.getClass();
        if (ac.equals(bc)) {
            if (JavaClassFile.class.equals(ac) || JavaSourceFile.class.equals(ac)) {
                return a.equals(b);
            } else {
                return super.isSameFile(a, b);
            }
        } else {
            return false;
        }
    }

    public boolean hasLocation(Location location) {
        if (!(location instanceof StandardLocation)) {
            return false;
        }
        switch ((StandardLocation) location) {
        case CLASS_OUTPUT:
        case SOURCE_OUTPUT:
        case CLASS_PATH:
        case SOURCE_PATH:
            return true;
        case ANNOTATION_PROCESSOR_PATH:
        case PLATFORM_CLASS_PATH:
        default:
            return false;

        }
    }

    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
        if (Kind.SOURCE != kind && Kind.CLASS != kind)
            return null;
        if (!(location instanceof StandardLocation))
            return null;
        switch ((StandardLocation) location) {
        case CLASS_OUTPUT:
            return this.getMemFileByClassName(className, classes);
        case CLASS_PATH:
            JavaFileObject f = this.getMemFileByClassName(className, classes);
            if (f == null) {
                return super.getJavaFileForInput(location, className, kind);
            } else {
                return f;
            }
        case SOURCE_OUTPUT:
        case SOURCE_PATH:
            return this.getMemFileByClassName(className, srcs);
        case ANNOTATION_PROCESSOR_PATH:
        case PLATFORM_CLASS_PATH:
        default:
            return null;
        }
    }

    // 根据类名获取内存文件，找不到则返回null
    @SuppressWarnings("unchecked")
    private JavaFileObject getMemFileByClassName(String className, LinkedHashMap<String, Object> memFiles) {
        if (className.contains(".")) {
            String pkg = DynaCompileUtil.substringBeforeLast(className, ".");
            // cd pkgPath
            Deque<String> pkgPath = resolvePkgPath(pkg);
            while (!pkgPath.isEmpty()) {
                Object f = memFiles.get(pkgPath.pop());
                if (f == null || !(f instanceof Map)) {
                    return null;
                } else {
                    memFiles = (LinkedHashMap<String, Object>) f;
                }
            }
            Object o = memFiles.get(DynaCompileUtil.substringAfterLast(className, "."));
            if (o instanceof JavaFileObject) {
                return (JavaFileObject) o;
            } else {
                return null;
            }
        } else {
            Object o = memFiles.get(className);
            if (o instanceof JavaFileObject) {
                return (JavaFileObject) o;
            } else {
                return null;
            }
        }
    }

    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
        if (Kind.CLASS != kind && Kind.SOURCE != kind)
            throw new IOException("Unsupported output kind: " + kind);
        if (!(location instanceof StandardLocation))
            throw new IOException("Unsupported output location: " + location);
        switch ((StandardLocation) location) {
        case CLASS_OUTPUT:
            return getOrCreateMemFileByClassName(className, this.classes, JavaClassFile.class);
        case SOURCE_OUTPUT:
            return getOrCreateMemFileByClassName(className, this.srcs, JavaSourceFile.class);
        case CLASS_PATH:
        case SOURCE_PATH:
        case ANNOTATION_PROCESSOR_PATH:
        case PLATFORM_CLASS_PATH:
        default:
            throw new IOException("Unsupported output location: " + location);
        }
    }

    // 获取一个内存文件用作输出，找不到则新建
    @SuppressWarnings("unchecked")
    private JavaFileObject getOrCreateMemFileByClassName(String className, LinkedHashMap<String, Object> memFiles,
            Class<? extends JavaFileObject> fileType) {
        if (className.contains(".")) {
            String pkg = DynaCompileUtil.substringBeforeLast(className, ".");
            // cd pkgPath
            Deque<String> pkgPath = resolvePkgPath(pkg);
            while (!pkgPath.isEmpty()) {
                String cur = pkgPath.pop();
                Object f = memFiles.get(cur);
                if (f != null && !(f instanceof Map)) {
                    throw new DynaCompilationException("Conflict class & pkg name: " + className + ", at: " + cur);
                } else if (f == null) {
                    LinkedHashMap<String, Object> dir = new LinkedHashMap<String, Object>();
                    memFiles.put(cur, dir);
                    memFiles = dir;
                } else {
                    memFiles = (LinkedHashMap<String, Object>) f;
                }
            }
            String simpleName = DynaCompileUtil.substringAfterLast(className, ".");
            Object o = memFiles.get(simpleName);
            if (o != null && !(o instanceof JavaFileObject)) {
                throw new DynaCompilationException("Conflict class & pkg name: " + className + ", at: " + simpleName);
            } else if (o == null) {
                JavaFileObject obj = null;
                if (JavaSourceFile.class.equals(fileType)) {
                    obj = new JavaSourceFile(simpleName + ".java", pkg, "");
                } else {
                    obj = new JavaClassFile(simpleName + ".class", pkg, null);
                }
                memFiles.put(simpleName, obj);
                return obj;
            } else {
                return (JavaFileObject) o;
            }
        } else {
            Object o = memFiles.get(className);
            if (o != null && !(o instanceof JavaFileObject)) {
                throw new DynaCompilationException("Conflict class & pkg name: " + className + ", at: " + className);
            } else if (o == null) {
                JavaFileObject obj = null;
                if (JavaSourceFile.class.equals(fileType)) {
                    obj = new JavaSourceFile(className + ".java", "", "");
                } else {
                    obj = new JavaClassFile(className + ".class", "", null);
                }
                memFiles.put(className, obj);
                return obj;
            } else {
                return (JavaFileObject) o;
            }
        }
    }

    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        if (location instanceof StandardLocation) {
            switch ((StandardLocation) location) {
            case CLASS_OUTPUT:
                if (relativeName.endsWith(".class")) {
                    return this.getJavaFileForInput(location, resolveClassName(packageName, relativeName), Kind.CLASS);
                } else {
                    return null;
                }
            case CLASS_PATH:
                if (relativeName.endsWith(".class")) {
                    JavaFileObject f = this.getJavaFileForInput(location, resolveClassName(packageName, relativeName), Kind.CLASS);
                    if (f == null) {
                        return super.getFileForInput(location, packageName, relativeName);
                    } else {
                        return f;
                    }
                } else {
                    return super.getFileForInput(location, packageName, relativeName);
                }
            case SOURCE_OUTPUT:
            case SOURCE_PATH:
                if (relativeName.endsWith(".java")) {
                    return this.getJavaFileForInput(location, resolveClassName(packageName, relativeName), Kind.SOURCE);
                } else {
                    return null;
                }
            case ANNOTATION_PROCESSOR_PATH:
            case PLATFORM_CLASS_PATH:
            default:
                return super.getFileForInput(location, packageName, relativeName);
            }
        } else {
            return super.getFileForInput(location, packageName, relativeName);
        }
    }

    private String resolveClassName(String pkg, String fileName) {
        if (DynaCompileUtil.isNotBlank(pkg)) {
            return pkg + "." + DynaCompileUtil.substringBeforeLast(fileName, ".");
        } else {
            return DynaCompileUtil.substringBeforeLast(fileName, ".");
        }
    }

    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        if (!(location instanceof StandardLocation))
            throw new IOException("Unsupported output location: " + location);
        switch ((StandardLocation) location) {
        case CLASS_OUTPUT:
            if (relativeName.endsWith(".class")) {
                return this.getJavaFileForOutput(location, resolveClassName(packageName, relativeName), Kind.CLASS, sibling);
            } else {
                throw new IOException("Unsupported output file type in class output location: " + relativeName);
            }
        case SOURCE_OUTPUT:
            if (relativeName.endsWith(".java")) {
                return this.getJavaFileForOutput(location, resolveClassName(packageName, relativeName), Kind.SOURCE, sibling);
            } else {
                throw new IOException("Unsupported output file type in source output location: " + relativeName);
            }
        case CLASS_PATH:
        case SOURCE_PATH:
        case ANNOTATION_PROCESSOR_PATH:
        case PLATFORM_CLASS_PATH:
        default:
            throw new IOException("Unsupported output location: " + location);
        }
    }

}
