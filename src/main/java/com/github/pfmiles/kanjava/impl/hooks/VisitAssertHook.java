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
package com.github.pfmiles.kanjava.impl.hooks;

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

import com.github.pfmiles.kanjava.impl.ErrMsg;
import com.github.pfmiles.kanjava.impl.GlobalContext;
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

    void beforeVisitCondition(AssertTree node, List<ErrMsg> errMsgs, GlobalContext ctx, Closure<List<Map<String, Long>>> resolveRowAndCol,
            Closure<Void> setError);

    void afterVisitConditionAndBeforeDetail(AssertTree node, List<ErrMsg> errMsgs, GlobalContext ctx,
            Closure<List<Map<String, Long>>> resolveRowAndCol, Closure<Void> setError);

    void afterVisitDetail(AssertTree node, List<ErrMsg> errMsgs, GlobalContext ctx, Closure<List<Map<String, Long>>> resolveRowAndCol,
            Closure<Void> setError);
}
