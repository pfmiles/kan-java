package com.github.pfmiles.kanjava.impl.hooks

import com.github.pfmiles.kanjava.Feature
import com.github.pfmiles.kanjava.impl.Cuttable
import com.github.pfmiles.kanjava.impl.ErrMsg
import com.github.pfmiles.kanjava.impl.GlobalContext
import com.sun.source.tree.ClassTree

/**
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 *
 */
class CutNestedClassFeatureVisitClassHook implements VisitClassHook {

    Cuttable getCuttable() {
        Feature.nestedClass;
    }

    void beforeVisitModifiers(ClassTree node, List<ErrMsg> errMsgs, GlobalContext globalCtx,
            Closure<List<Map<String, Long>>> resolveRowAndCol, Closure<Void> setError) {
        def ctx = globalCtx.getCtx(this.getCuttable())
        if(ctx.inCls){
            setError()
            def rowAndCol = resolveRowAndCol(node)
            errMsgs.add(new ErrMsg(rowAndCol.row, rowAndCol.col, "Nested classes are not allowed."))
        }else{
            ctx.inCls = true
        }
    }

    void afterVisitModifiersAndBeforeTypeParameters(ClassTree node, List<ErrMsg> errMsgs, GlobalContext globalCtx,
            Closure<List<Map<String, Long>>> resolveRowAndCol, Closure<Void> setError) {
    }

    void afterVisitTypeParametersAndBeforeExtendsClause(ClassTree node, List<ErrMsg> errMsgs, GlobalContext globalCtx,
            Closure<List<Map<String, Long>>> resolveRowAndCol, Closure<Void> setError) {
    }

    void afterVisitExtendsClauseAndBeforeImplementsClause(ClassTree node, List<ErrMsg> errMsgs, GlobalContext globalCtx,
            Closure<List<Map<String, Long>>> resolveRowAndCol, Closure<Void> setError) {
    }

    void afterVisitImplementsClauseAndBeforeMembers(ClassTree node, List<ErrMsg> errMsgs, GlobalContext globalCtx,
            Closure<List<Map<String, Long>>> resolveRowAndCol, Closure<Void> setError) {
    }

    void afterVisitMembers(ClassTree node, List<ErrMsg> errMsgs, GlobalContext globalCtx, Closure<List<Map<String, Long>>> resolveRowAndCol,
            Closure<Void> setError) {
    }
}
