package com.github.pfmiles.kanjava.impl.hooks;

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

import com.github.pfmiles.kanjava.impl.ErrMsg;
import com.github.pfmiles.kanjava.impl.GlobalContext;
import com.github.pfmiles.kanjava.impl.Hook;
import com.sun.source.tree.ContinueTree;

/**
 * ast walker visit continue语句的hook接口
 * 
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
public interface VisitContinueHook extends Hook {
    void visit(ContinueTree node, List<ErrMsg> errMsgs, GlobalContext globalCtx, Closure<List<Map<String, Long>>> resolveRowAndCol,
            Closure<Void> setError);
}
