package com.github.pfmiles.kanjava.impl.hooks

import com.github.pfmiles.kanjava.Feature
import com.github.pfmiles.kanjava.impl.Cuttable
import com.github.pfmiles.kanjava.impl.ErrMsg;
import com.github.pfmiles.kanjava.impl.hooks.VisitAssertHook;
import com.sun.source.tree.AssertTree

/**
 * 
 * 禁止assert语句的VisitAssertHook实现
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 *
 */
class CutAssertFeatureVisitAssertHook implements VisitAssertHook{

    Cuttable getCuttable() {
        Feature.assertion;
    }

    void beforeVisitAssert(AssertTree node, List<ErrMsg> errMsgs, Map<String, Object> ctx, Closure<List<Map<String, Long>>> resolveRowAndCol,
            Closure<Void> setError) {
        setError()
        def rowAndCol = resolveRowAndCol(node)
        errMsgs.add(new ErrMsg(rowAndCol.row, rowAndCol.col, "Assertions are not allowed."))
    }

    void afterVisitAssert(AssertTree node, List<ErrMsg> errMsgs, Map<String, Object> ctx, Closure<List<Map<String, Long>>> resolveRowAndCol,
            Closure<Void> setError) {
    }
}
