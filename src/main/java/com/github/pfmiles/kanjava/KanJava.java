package com.github.pfmiles.kanjava;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;

import com.github.pfmiles.kanjava.compile.CompilationResult;
import com.github.pfmiles.kanjava.compile.DynaCompileClassLoader;
import com.github.pfmiles.kanjava.compile.DynaCompileUtil;
import com.github.pfmiles.kanjava.compile.JavaClassFile;
import com.github.pfmiles.kanjava.impl.Cuttable;
import com.github.pfmiles.kanjava.impl.ErrMsg;
import com.github.pfmiles.kanjava.impl.Hook;
import com.github.pfmiles.kanjava.impl.KanJavaProcessor;

/**
 * kan-java编译工具
 * 
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
public class KanJava {

    // 需要砍掉的feature
    private List<Cuttable> cuts;

    // ast walker的各hook点的mapping: hookInterfaceClass -> hookList
    private Map<Class<? extends Hook>, List<Hook>> hooks;

    private static final class CpHolder {
        public static final String DYNA_CP = DynaCompileUtil.getClassPath();
    }

    /**
     * 创建KanJava编译工具实例
     * 
     * @param cuts
     *            希望被砍掉的功能列表(更“平凡”的说法应当是：希望被执行的AST"钩子"组实现列表)
     */
    public KanJava(Cuttable... cuts) {
        this.hooks = new HashMap<Class<? extends Hook>, List<Hook>>();
        if (cuts != null) {
            this.cuts = Arrays.asList(cuts);
            for (Cuttable cut : cuts) {
                for (Hook h : cut.getHooks()) {
                    List<Class<? extends Hook>> interfaceClses = resolveHookInterfaceClses(h);
                    for (Class<? extends Hook> interfaceCls : interfaceClses) {
                        List<Hook> hks = this.hooks.get(interfaceCls);
                        if (hks == null) {
                            hks = new ArrayList<Hook>();
                            this.hooks.put(interfaceCls, hks);
                        }
                        hks.add(h);
                    }
                }
            }
        }
    }

    // 找到指定hook实例所实现的所有hook接口
    private List<Class<? extends Hook>> resolveHookInterfaceClses(Hook h) {
        List<Class<? extends Hook>> ret = new ArrayList<Class<? extends Hook>>();
        Class<?> cls = h.getClass();
        if (Hook.class.isAssignableFrom(cls)) {
            Class<?> sup = cls.getSuperclass();
            ret.addAll(rsvHookIntersFromSupCls(sup));
            Class<?>[] inters = cls.getInterfaces();
            if (inters != null) {
                for (Class<?> inter : inters) {
                    ret.addAll(rsvHookIntersFromInter(inter));
                }
            }
        }
        if (ret.isEmpty())
            throw new RuntimeException("No corresponding hook interface found, hook of type: '" + cls.getName() + "' is not a valid hook.");
        return ret;
    }

    private List<Class<? extends Hook>> rsvHookIntersFromInter(Class<?> inter) {
        if (inter == null || !Hook.class.isAssignableFrom(inter))
            return Collections.emptyList();
        List<Class<? extends Hook>> ret = new ArrayList<Class<? extends Hook>>();
        if (isDirectHookSubInterface(inter)) {
            ret.add(inter.asSubclass(Hook.class));
        } else {
            Class<?>[] inters = inter.getInterfaces();
            if (inters != null)
                for (Class<?> i : inters)
                    ret.addAll(rsvHookIntersFromInter(i));
        }
        return ret;
    }

    // 判断传入interface是否Hook接口的直接子类
    private boolean isDirectHookSubInterface(Class<?> inter) {
        Class<?>[] superInters = inter.getInterfaces();
        if (superInters != null)
            for (Class<?> superInter : superInters) {
                if (superInter.equals(Hook.class)) {
                    return true;
                }
            }
        return false;
    }

    private List<Class<? extends Hook>> rsvHookIntersFromSupCls(Class<?> sup) {
        if (sup == null || !Hook.class.isAssignableFrom(sup))
            return Collections.emptyList();
        List<Class<? extends Hook>> ret = new ArrayList<Class<? extends Hook>>();
        Class<?> supsup = sup.getSuperclass();
        ret.addAll(rsvHookIntersFromSupCls(supsup));
        Class<?>[] inters = sup.getInterfaces();
        if (inters != null) {
            for (Class<?> inter : inters) {
                ret.addAll(rsvHookIntersFromInter(inter));
            }
        }
        return ret;
    }

    public List<Cuttable> getCuts() {
        return cuts;
    }

    /**
     * 做语法定制检查, 并动态编译String形式的java源码, kan-java将会自动分析出编译所需的classpath
     * 
     * @param sources
     *            源文件列表, 不能为空
     * @return 编译结果，包括class bytes和可能的错误信息
     * @throws KanJavaException
     */
    public KanJavaCompileResult checkAndCompile(List<JavaSourceFile> sources) {
        return checkAndCompile(sources, CpHolder.DYNA_CP);
    }

    /**
     * 做语法定制检查, 并动态编译String形式的java源码
     * 
     * @param sources
     *            源文件列表, 不能为空
     * @param clsPathStr
     *            编译时用的classpath字符串, 可为空
     * @return 编译结果，包括class bytes和可能的错误信息
     * @throws KanJavaException
     */
    public KanJavaCompileResult checkAndCompile(List<JavaSourceFile> sources, String clsPathStr) {
        return _compile(sources, clsPathStr, 0);
    }

    /**
     * 做语法定制检查, 并动态编译String形式的java源码
     * 
     * @param sources
     *            源文件列表, 不能为空
     * @param clsPathJars
     *            编译时作为classpath的jar包文件列表, 可为空
     * @return 编译结果，包括class bytes和可能的错误信息
     * @throws KanJavaException
     */
    public KanJavaCompileResult checkAndCompile(List<JavaSourceFile> sources, List<DiskJarFile> clsPathJars) {
        return checkAndCompile(sources, toClassPathStr(clsPathJars));
    }

    /**
     * 仅作语言特性定制的检查，报告可能的错误，但不会将源码编译为字节码; 注意此方法的报错并不会包括javac编译错误
     */
    public KanJavaCompileResult procOnly(List<JavaSourceFile> sources) {
        return _compile(sources, null, 1);
    }

    /**
     * 仅作编译，将传入源码编译为字节码，但不做语言特性定制的检查; 假定语言特性检查已由前置流程"onlyCheck"方法调用做掉
     */
    public KanJavaCompileResult compileOnly(List<JavaSourceFile> sources, String clsPathStr) {
        return _compile(sources, clsPathStr, 2);
    }

    /**
     * 仅作编译，将传入源码编译为字节码，但不做语言特性定制的检查; 假定语言特性检查已由前置流程"onlyCheck"方法调用做掉;
     * 且在编译时使用kan-java动态分析出的classpath
     */
    public KanJavaCompileResult compileOnly(List<JavaSourceFile> sources) {
        return compileOnly(sources, CpHolder.DYNA_CP);
    }

    /**
     * 仅作编译，将传入源码编译为字节码，但不做语言特性定制的检查; 假定语言特性检查已由前置流程"onlyCheck"方法调用做掉
     */
    public KanJavaCompileResult compileOnly(List<JavaSourceFile> sources, List<DiskJarFile> clsPathJars) {
        return _compile(sources, toClassPathStr(clsPathJars), 2);
    }

    private KanJavaCompileResult _compile(List<JavaSourceFile> sources, String clsPathStr, int procExe) {
        if (sources == null || sources.isEmpty())
            throw new KanJavaException("Compiling sources must not be null or empty.");
        List<Processor> procs = new ArrayList<Processor>();
        KanJavaProcessor processor = new KanJavaProcessor(this.hooks);
        procs.add(processor);
        CompilationResult rst = DynaCompileUtil.compile(new LinkedHashSet<JavaSourceFile>(sources), clsPathStr, procs, procExe);
        StringBuilder sb = new StringBuilder();
        KanJavaCompileResult ret = new KanJavaCompileResult();
        if (rst.isError()) {
            sb.append(rst.getErrMsg()).append("\n");
        }
        if (!processor.isSuccess()) {
            for (ErrMsg msg : processor.getErrMsgs()) {
                sb.append(msg.toString()).append("\n");
            }
        }
        if (sb.length() != 0)
            ret.setErrMsg(sb.toString());
        if (ret.isSuccess())
            try {
                ret.setClasses(loadCompiledClasses(rst.getClassFiles()));
            } catch (ClassNotFoundException e) {
                throw new KanJavaException(e);
            }
        return ret;
    }

    // 生成动态编译的classpath，应包括所有传入的jar包
    private static String toClassPathStr(Collection<DiskJarFile> cpJars) {
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

    private List<Class<?>> loadCompiledClasses(Set<JavaClassFile> classFiles) throws ClassNotFoundException {
        List<Class<?>> ret = new ArrayList<Class<?>>();
        Map<String, byte[]> clsMap = new HashMap<String, byte[]>();
        for (JavaClassFile f : classFiles)
            clsMap.put(f.getBinaryClassName(), f.getData());
        DynaCompileClassLoader loader = new DynaCompileClassLoader(getParentClsLoader(), clsMap);
        for (JavaClassFile f : classFiles)
            ret.add(loader.loadClass(f.getBinaryClassName()));
        return ret;
    }

    private static ClassLoader getParentClsLoader() {
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        if (ctxLoader != null) {
            try {
                ctxLoader.loadClass(KanJava.class.getName());
                return ctxLoader;
            } catch (ClassNotFoundException e) {
            }
        }
        return KanJava.class.getClassLoader();
    }

}
