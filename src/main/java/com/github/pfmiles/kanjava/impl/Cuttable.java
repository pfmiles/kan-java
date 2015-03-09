package com.github.pfmiles.kanjava.impl;

import java.util.List;

/**
 * 可被“砍”的接口，可选择预定义的Feature拿来砍或自定义实现拿来砍
 * 
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
public interface Cuttable {

    List<Hook> getHooks();

}
