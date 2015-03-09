package com.github.pfmiles.kanjava.impl;

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

import com.sun.source.util.TreePath
import com.sun.source.util.Trees
import com.sun.tools.javac.model.JavacElements
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.util.Context

/**
 * 
 * Annotation processing接入点，调用ast walker完成主要功能
 * @author pf-miles
 * 
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
class KanJavaProcessor extends AbstractProcessor {

    // 工具实例类，用于将CompilerAPI, CompilerTreeAPI和AnnotationProcessing框架粘合起来
    Trees trees;
    // 分析过程中可用的日志、信息打印工具
    Messager messager;
    // 构造java parse tree的工具类
    TreeMaker maker;
    // 用于获取java的symbol table
    JavacElements elements;

    // ast walker
    KanJavaAstWalker astWalker = new KanJavaAstWalker();

    // 代码检查是否成功
    boolean success = true;

    List<ErrMsg> errMsgs = [];

    // hook接口类型 -> hook列表
    Map<Class<? extends Hook>, List<Hook>> hooks;


    KanJavaProcessor(Map<Class<? extends Hook>, List<Hook>> hooks) {
        this.hooks = hooks;
    }

    synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.trees = Trees.instance(processingEnv);
        this.messager = processingEnv.getMessager();
        // 这个强制转换是个trick, 使得processor能对java的parse tree做更改
        Context ctx = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.maker = TreeMaker.instance(ctx);
        this.elements = JavacElements.instance(ctx);
        // 为astWalker置入工具实例
        astWalker.trees = trees;
        astWalker.messager = messager;
        astWalker.hooks = this.hooks;
    }

    boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (!env.processingOver())
            env.getRootElements().each {
                TreePath path = this.trees.getPath(it);
                this.astWalker.scan(path, null)
                this.success = this.astWalker.success;
                this.errMsgs.addAll(this.astWalker.errMsgs);
            }
        /*
         * 这里若return true将阻止任何后续可能存在的Processor的运行，因此这里可以固定返回false
         */
        return false;
    }

}
