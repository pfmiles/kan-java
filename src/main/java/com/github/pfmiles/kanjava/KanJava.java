package com.github.pfmiles.kanjava;

import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * 创建KanJava编译工具实例
     * 
     * @param cuts
     *            希望被砍掉的功能列表
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
     * 动态编译String形式的java源码
     * 
     * @param sources
     *            源文件列表, 不能为空
     * @param clsPathJars
     *            编译时作为classpath的jar包文件列表, 可为空
     * @return 编译结果，包括class bytes和可能的错误信息
     * @throws KanJavaException
     */
    public KanJavaCompileResult compile(List<JavaSourceFile> sources, List<DiskJarFile> clsPathJars) {
        if (sources == null || sources.isEmpty())
            throw new KanJavaException("Compiling sources must not be null or empty.");
        List<Processor> procs = new ArrayList<Processor>();
        KanJavaProcessor processor = new KanJavaProcessor(this.hooks);
        procs.add(processor);
        Set<DiskJarFile> clsPathSet = new HashSet<DiskJarFile>();
        if (clsPathJars != null)
            clsPathSet.addAll(clsPathJars);
        CompilationResult rst = DynaCompileUtil.compile(new LinkedHashSet<JavaSourceFile>(sources), clsPathSet, procs);
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
