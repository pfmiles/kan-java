package com.github.pfmiles.kanjava.impl;

/**
 * 嵌入ast walker各visit方法的标记接口, 用户没有理由直接实现此接口
 * 
 * To contributors: 由于实现相关，所有的hook子接口必须是本接口的直接子接口，否则不能被正确注册到hookMapping中,
 * 具体见“KanJava.resolveHookInterfaceClses”实现
 * 
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
public interface Hook {
    /**
     * 取得此hook对应的cuttable
     */
    Cuttable getCuttable();
}
