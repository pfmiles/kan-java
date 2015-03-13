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
package com.github.pfmiles.kanjava.impl.hooks

import com.github.pfmiles.kanjava.Feature
import com.github.pfmiles.kanjava.impl.Cuttable
import com.github.pfmiles.kanjava.impl.ErrMsg
import com.github.pfmiles.kanjava.impl.GlobalContext
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

    void beforeVisitCondition(AssertTree node, List<ErrMsg> errMsgs, GlobalContext ctx, Closure<List<Map<String, Long>>> resolveRowAndCol,
            Closure<Void> setError) {
        setError()
        def rowAndCol = resolveRowAndCol(node)
        errMsgs.add(new ErrMsg(rowAndCol.row, rowAndCol.col, "Assertions are not allowed."))
    }

    void afterVisitConditionAndBeforeDetail(AssertTree node, List<ErrMsg> errMsgs, GlobalContext ctx,
            Closure<List<Map<String, Long>>> resolveRowAndCol, Closure<Void> setError) {
    }

    void afterVisitDetail(AssertTree node, List<ErrMsg> errMsgs, GlobalContext ctx, Closure<List<Map<String, Long>>> resolveRowAndCol,
            Closure<Void> setError) {
    }
}
