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
import com.sun.source.tree.WhileLoopTree

/**
 * 禁止while循环的hook实现
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 *
 */
class CutWhileLoopFeatureVisitWhileLoopHook implements VisitWhileLoopHook {

    Cuttable getCuttable() {
        Feature.whileLoop
    }

    void beforeVisitCondition(WhileLoopTree node, List<ErrMsg> errMsgs, GlobalContext globalCtx,
            Closure<List<Map<String, Long>>> resolveRowAndCol, Closure<Void> setError) {
        setError()
        def rowAndCol = resolveRowAndCol(node)
        errMsgs.add(new ErrMsg(rowAndCol.row, rowAndCol.col, "While-loops are not allowed."))
    }

    void afterVisitConditionAndBeforeStatement(WhileLoopTree node, List<ErrMsg> errMsgs, GlobalContext globalCtx,
            Closure<List<Map<String, Long>>> resolveRowAndCol, Closure<Void> setError) {
    }

    void afterVisitStatement(WhileLoopTree node, List<ErrMsg> errMsgs, GlobalContext globalCtx,
            Closure<List<Map<String, Long>>> resolveRowAndCol, Closure<Void> setError) {
    }
}
