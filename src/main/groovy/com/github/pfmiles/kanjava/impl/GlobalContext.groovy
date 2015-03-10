package com.github.pfmiles.kanjava.impl

/**
 * ast walk过程中维护的全局context, 结构为：cuttable -> map, 即每一个cuttable实例都对应一个独立的map上下文
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 *
 */
class GlobalContext {
    def ctx = [:]

    /**
     * 根据指定cuttable实例获取对应的map形式的context
     */
    def getCtx(Cuttable cuttable){
        if(cuttable in ctx){
            ctx[cuttable]
        }else{
            def ret = [:]
            ctx[cuttable] = ret
            ret
        }
    }
}
