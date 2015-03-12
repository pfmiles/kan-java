package com.github.pfmiles.kanjava;

import java.util.Arrays;
import java.util.List;

import com.github.pfmiles.kanjava.impl.Cuttable;
import com.github.pfmiles.kanjava.impl.Hook;
import com.github.pfmiles.kanjava.impl.hooks.CutAssertFeatureVisitAssertHook;
import com.github.pfmiles.kanjava.impl.hooks.CutDoWhileLoopFeatureVisitDoWhileLoopHook;
import com.github.pfmiles.kanjava.impl.hooks.CutEnhancedForLoopFeatureVisitEnhancedForLoopHook;
import com.github.pfmiles.kanjava.impl.hooks.CutForLoopFeatureVisitForLoopHook;
import com.github.pfmiles.kanjava.impl.hooks.CutNestedClassFeatureVisitClassHook;
import com.github.pfmiles.kanjava.impl.hooks.CutWhileLoopFeatureVisitWhileLoopHook;

/**
 * 预置的各种java语言特性，可选择性地"砍"
 * 
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
public enum Feature implements Cuttable {
    /**
     * 嵌套类
     */
    nestedClass(new CutNestedClassFeatureVisitClassHook()),
    /**
     * for循环
     */
    forLoop(new CutForLoopFeatureVisitForLoopHook()),
    /**
     * while循环
     */
    whileLoop(new CutWhileLoopFeatureVisitWhileLoopHook()),
    /**
     * do-while循环
     */
    doWhileLoop(new CutDoWhileLoopFeatureVisitDoWhileLoopHook()),
    /**
     * 增强版for循环(for-each)
     */
    enhancedForLoop(new CutEnhancedForLoopFeatureVisitEnhancedForLoopHook()),
    /**
     * assert语句
     */
    assertion(new CutAssertFeatureVisitAssertHook());
    // /**
    // * break语句
    // */
    // breakStmt,
    // /**
    // * 带标签的break语句, 如：
    // *
    // * <pre>
    // * break label1;
    // * </pre>
    // */
    // LabeledBreak,
    // /**
    // * continue语句
    // */
    // continueStmt,
    // /**
    // * 带标签的continue语句，如:
    // *
    // * <pre>
    // * continue label1;
    // * </pre>
    // */
    // labeledContinue;

    // 本feature关联的ast walker hook
    private List<Hook> hooks;

    private Feature(Hook... hooks) {
        this.hooks = Arrays.asList(hooks);
    }

    public List<Hook> getHooks() {
        return this.hooks;
    }

    // to be continued...
}
