package com.github.pfmiles.kanjava.compile;

import java.util.Map;

/**
 * 用于加载动态编译后的java类的classLoader
 * 
 * @author pf-miles 2013-9-25 上午9:59:24
 */
public class DynaCompileClassLoader extends ClassLoader {

    private Map<String, byte[]> inMemCls;

    public DynaCompileClassLoader(ClassLoader parent, Map<String, byte[]> clses) {
        super(parent);
        this.inMemCls = clses;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] b = this.inMemCls.get(name);
        return defineClass(name, b, 0, b.length);
    }

}
