package com.github.pfmiles.kanjava.impl.hooks;

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

import com.github.pfmiles.kanjava.impl.ErrMsg;
import com.github.pfmiles.kanjava.impl.Hook;
import com.sun.source.tree.AssertTree;

/**
 * 
 * assert语句的hook接口
 * 
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 * 
 */
public interface VisitAssertHook extends Hook {
    /**
     * 在visitAssert之前调用
     * 
     * @param node
     *            assert节点
     * @param errMsgs
     *            全局错误列表
     * @param ctx
     *            属于本hook关联的cuttable的全局上下文
     * @param resolveRowAndCol
     *            传入节点，获得该节点的行号、列号
     * @param setError
     *            调用后将本次ast walk设置为失败
     */
    void beforeVisitAssert(AssertTree node, List<ErrMsg> errMsgs, Map<String, Object> ctx, Closure<List<Map<String, Long>>> resolveRowAndCol,
            Closure<Void> setError);

    /**
     * 在visitAssert之后调用
     * 
     * @param node
     *            assert节点
     * @param errMsgs
     *            全局错误列表
     * @param ctx
     *            属于本hook关联的cuttable的全局上下文
     * @param resolveRowAndCol
     *            传入节点，获得该节点的行号、列号
     * @param setError
     *            调用后将本次ast walk设置为失败
     */
    void afterVisitAssert(AssertTree node, List<ErrMsg> errMsgs, Map<String, Object> ctx, Closure<List<Map<String, Long>>> resolveRowAndCol,
            Closure<Void> setError);
}
