package com.github.pfmiles.kanjava.impl

import javax.annotation.processing.Messager

import com.github.pfmiles.kanjava.impl.hooks.VisitAssertHook
import com.github.pfmiles.kanjava.impl.hooks.VisitClassHook
import com.github.pfmiles.kanjava.impl.hooks.VisitDoWhileLoopHook
import com.github.pfmiles.kanjava.impl.hooks.VisitEnhancedForLoopHook
import com.github.pfmiles.kanjava.impl.hooks.VisitForLoopHook
import com.github.pfmiles.kanjava.impl.hooks.VisitWhileLoopHook
import com.sun.source.tree.AnnotationTree
import com.sun.source.tree.ArrayAccessTree
import com.sun.source.tree.ArrayTypeTree
import com.sun.source.tree.AssertTree
import com.sun.source.tree.AssignmentTree
import com.sun.source.tree.BinaryTree
import com.sun.source.tree.BlockTree
import com.sun.source.tree.BreakTree
import com.sun.source.tree.CaseTree
import com.sun.source.tree.CatchTree
import com.sun.source.tree.ClassTree
import com.sun.source.tree.CompilationUnitTree
import com.sun.source.tree.CompoundAssignmentTree
import com.sun.source.tree.ConditionalExpressionTree
import com.sun.source.tree.ContinueTree
import com.sun.source.tree.DoWhileLoopTree
import com.sun.source.tree.EmptyStatementTree
import com.sun.source.tree.EnhancedForLoopTree
import com.sun.source.tree.ErroneousTree
import com.sun.source.tree.ExpressionStatementTree
import com.sun.source.tree.ForLoopTree
import com.sun.source.tree.IdentifierTree
import com.sun.source.tree.IfTree
import com.sun.source.tree.ImportTree
import com.sun.source.tree.InstanceOfTree
import com.sun.source.tree.LabeledStatementTree
import com.sun.source.tree.LineMap
import com.sun.source.tree.LiteralTree
import com.sun.source.tree.MemberSelectTree
import com.sun.source.tree.MethodInvocationTree
import com.sun.source.tree.MethodTree
import com.sun.source.tree.ModifiersTree
import com.sun.source.tree.NewArrayTree
import com.sun.source.tree.NewClassTree
import com.sun.source.tree.ParameterizedTypeTree
import com.sun.source.tree.ParenthesizedTree
import com.sun.source.tree.PrimitiveTypeTree
import com.sun.source.tree.ReturnTree
import com.sun.source.tree.SwitchTree
import com.sun.source.tree.SynchronizedTree
import com.sun.source.tree.ThrowTree
import com.sun.source.tree.Tree
import com.sun.source.tree.TryTree
import com.sun.source.tree.TypeCastTree
import com.sun.source.tree.TypeParameterTree
import com.sun.source.tree.UnaryTree
import com.sun.source.tree.VariableTree
import com.sun.source.tree.WhileLoopTree
import com.sun.source.tree.WildcardTree
import com.sun.source.util.TreePathScanner
import com.sun.source.util.Trees

/**
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 *
 */
class KanJavaAstWalker extends TreePathScanner<Void, Void> {
    // 当前被扫描代码对应的节点转换工具类, 运行时由Processor负责置入
    Trees    trees;
    // 错误信息打印、处理流程控制工具, 运行时由Processor负责置入
    Messager messager;

    // 检查是否成功
    boolean success = true;
    // 错误信息
    List<ErrMsg> errMsgs = [];

    // 检查执行过程中的上下文, cuttable -> 隶属特定cuttable的全局map
    GlobalContext ctx = new GlobalContext();

    // hook点接口类 -> hooks实例映射
    Map<Class<? extends Hook>, List<Hook>> hooks;

    /**
     * 获取指定语法节点缩在源文件中的行号和列号信息, 用于错误信息输出: {row, col}
     */
    def resolveRowAndCol = { node ->
        CompilationUnitTree unit = this.getCurrentPath().getCompilationUnit();
        long pos = this.trees.getSourcePositions().getStartPosition(unit, node);
        LineMap m = unit.getLineMap();
        [row: m.getLineNumber(pos) , col: m.getColumnNumber(pos)]
    }

    /**
     * 将当前检查状态置为不成功
     */
    def setError = {-> this.success = false }

    Void visitAssert(AssertTree node, Void arg1) {
        // 取得assert hooks
        def ahooks = this.hooks[VisitAssertHook.class]
        ahooks.each {it.beforeVisitCondition(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan((Tree)node.getCondition(), arg1);
        ahooks.each {it.afterVisitConditionAndBeforeDetail(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan((Tree)node.getDetail(), arg1);
        ahooks.each {it.afterVisitDetail(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        return null;
    }

    Void visitForLoop(ForLoopTree node, Void arg1) {
        // 取得for-loop hooks
        def fhooks = this.hooks[VisitForLoopHook.class]
        // 访问初始条件
        fhooks.each {it.beforeVisitInitializer(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getInitializer(), arg1);
        fhooks.each {it.afterVisitInitializerAndBeforeCondition(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        // 访问循环条件
        scan(node.getCondition(), arg1);
        fhooks.each {it.afterVisitConditionAndBeforeUpdate(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        // 访问递进条件
        scan(node.getUpdate(), arg1);
        fhooks.each {it.afterVisitUpdateAndBeforeStatement(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        // 访问循环体
        scan(node.getStatement(), arg1);
        fhooks.each {it.afterVisitStatement(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        return null;
    }

    Void visitWhileLoop(WhileLoopTree node, Void arg1) {
        def whooks = this.hooks[VisitWhileLoopHook.class]
        whooks.each {it.beforeVisitCondition(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getCondition(), arg1);
        whooks.each {it.afterVisitConditionAndBeforeStatement(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getStatement(), arg1);
        whooks.each {it.afterVisitStatement(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        return null;
    }

    Void visitClass(ClassTree node, Void arg1) {
        def chooks = this.hooks[VisitClassHook.class]
        chooks.each {it.beforeVisitModifiers(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan((Tree)node.getModifiers(), arg1);
        chooks.each {it.afterVisitModifiersAndBeforeTypeParameters(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getTypeParameters(), arg1);
        chooks.each {it.afterVisitTypeParametersAndBeforeExtendsClause(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan((Tree)node.getExtendsClause(), arg1);
        chooks.each {it.afterVisitExtendsClauseAndBeforeImplementsClause(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getImplementsClause(), arg1);
        chooks.each {it.afterVisitImplementsClauseAndBeforeMembers(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getMembers(), arg1);
        chooks.each {it.afterVisitMembers(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        return null;
    }

    Void visitDoWhileLoop(DoWhileLoopTree node, Void arg1) {
        def dhooks = this.hooks[VisitDoWhileLoopHook.class]
        dhooks.each {it.beforeVisitStatement(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getStatement(), arg1);
        dhooks.each {it.afterVisitStatementAndBeforeCondition(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getCondition(), arg1);
        dhooks.each {it.afterVisitCondition(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        return null;
    }

    Void visitEnhancedForLoop(EnhancedForLoopTree node, Void arg1) {
        def ehooks = this.hooks[VisitEnhancedForLoopHook.class]
        ehooks.each {it.beforeVisitVariable(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getVariable(), arg1);
        ehooks.each {it.afterVisitVariableAndBeforeExpression(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getExpression(), arg1);
        ehooks.each {it.afterVisitExpressionAndBeforeStatement(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        scan(node.getStatement(), arg1);
        ehooks.each {it.afterVisitStatement(node, errMsgs, this.ctx, resolveRowAndCol, setError)}
        return null;
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitAnnotation(com.sun.source.tree.AnnotationTree, java.lang.Object)
     */
    @Override
    Void visitAnnotation(AnnotationTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitAnnotation(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitArrayAccess(com.sun.source.tree.ArrayAccessTree, java.lang.Object)
     */
    @Override
    Void visitArrayAccess(ArrayAccessTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitArrayAccess(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitArrayType(com.sun.source.tree.ArrayTypeTree, java.lang.Object)
     */
    @Override
    Void visitArrayType(ArrayTypeTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitArrayType(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitAssignment(com.sun.source.tree.AssignmentTree, java.lang.Object)
     */
    @Override
    Void visitAssignment(AssignmentTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitAssignment(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitBinary(com.sun.source.tree.BinaryTree, java.lang.Object)
     */
    @Override
    Void visitBinary(BinaryTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitBinary(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitBlock(com.sun.source.tree.BlockTree, java.lang.Object)
     */
    @Override
    Void visitBlock(BlockTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitBlock(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitBreak(com.sun.source.tree.BreakTree, java.lang.Object)
     */
    @Override
    Void visitBreak(BreakTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitBreak(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitCase(com.sun.source.tree.CaseTree, java.lang.Object)
     */
    @Override
    Void visitCase(CaseTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitCase(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitCatch(com.sun.source.tree.CatchTree, java.lang.Object)
     */
    @Override
    Void visitCatch(CatchTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitCatch(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitCompilationUnit(com.sun.source.tree.CompilationUnitTree, java.lang.Object)
     */
    @Override
    Void visitCompilationUnit(CompilationUnitTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitCompilationUnit(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitCompoundAssignment(com.sun.source.tree.CompoundAssignmentTree, java.lang.Object)
     */
    @Override
    Void visitCompoundAssignment(CompoundAssignmentTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitCompoundAssignment(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitConditionalExpression(com.sun.source.tree.ConditionalExpressionTree, java.lang.Object)
     */
    @Override
    Void visitConditionalExpression(ConditionalExpressionTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitConditionalExpression(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitContinue(com.sun.source.tree.ContinueTree, java.lang.Object)
     */
    @Override
    Void visitContinue(ContinueTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitContinue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitEmptyStatement(com.sun.source.tree.EmptyStatementTree, java.lang.Object)
     */
    @Override
    Void visitEmptyStatement(EmptyStatementTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitEmptyStatement(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitErroneous(com.sun.source.tree.ErroneousTree, java.lang.Object)
     */
    @Override
    Void visitErroneous(ErroneousTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitErroneous(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitExpressionStatement(com.sun.source.tree.ExpressionStatementTree, java.lang.Object)
     */
    @Override
    Void visitExpressionStatement(ExpressionStatementTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitExpressionStatement(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitIdentifier(com.sun.source.tree.IdentifierTree, java.lang.Object)
     */
    @Override
    Void visitIdentifier(IdentifierTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitIdentifier(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitIf(com.sun.source.tree.IfTree, java.lang.Object)
     */
    @Override
    Void visitIf(IfTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitIf(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitImport(com.sun.source.tree.ImportTree, java.lang.Object)
     */
    @Override
    Void visitImport(ImportTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitImport(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitInstanceOf(com.sun.source.tree.InstanceOfTree, java.lang.Object)
     */
    @Override
    Void visitInstanceOf(InstanceOfTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitInstanceOf(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitLabeledStatement(com.sun.source.tree.LabeledStatementTree, java.lang.Object)
     */
    @Override
    Void visitLabeledStatement(LabeledStatementTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitLabeledStatement(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitLiteral(com.sun.source.tree.LiteralTree, java.lang.Object)
     */
    @Override
    Void visitLiteral(LiteralTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitLiteral(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitMemberSelect(com.sun.source.tree.MemberSelectTree, java.lang.Object)
     */
    @Override
    Void visitMemberSelect(MemberSelectTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitMemberSelect(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitMethod(com.sun.source.tree.MethodTree, java.lang.Object)
     */
    @Override
    Void visitMethod(MethodTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitMethod(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitMethodInvocation(com.sun.source.tree.MethodInvocationTree, java.lang.Object)
     */
    @Override
    Void visitMethodInvocation(MethodInvocationTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitMethodInvocation(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitModifiers(com.sun.source.tree.ModifiersTree, java.lang.Object)
     */
    @Override
    Void visitModifiers(ModifiersTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitModifiers(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitNewArray(com.sun.source.tree.NewArrayTree, java.lang.Object)
     */
    @Override
    Void visitNewArray(NewArrayTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitNewArray(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitNewClass(com.sun.source.tree.NewClassTree, java.lang.Object)
     */
    @Override
    Void visitNewClass(NewClassTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitNewClass(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitParameterizedType(com.sun.source.tree.ParameterizedTypeTree, java.lang.Object)
     */
    @Override
    Void visitParameterizedType(ParameterizedTypeTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitParameterizedType(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitParenthesized(com.sun.source.tree.ParenthesizedTree, java.lang.Object)
     */
    @Override
    Void visitParenthesized(ParenthesizedTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitParenthesized(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitPrimitiveType(com.sun.source.tree.PrimitiveTypeTree, java.lang.Object)
     */
    @Override
    Void visitPrimitiveType(PrimitiveTypeTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitPrimitiveType(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitReturn(com.sun.source.tree.ReturnTree, java.lang.Object)
     */
    @Override
    Void visitReturn(ReturnTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitSwitch(com.sun.source.tree.SwitchTree, java.lang.Object)
     */
    @Override
    Void visitSwitch(SwitchTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitSwitch(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitSynchronized(com.sun.source.tree.SynchronizedTree, java.lang.Object)
     */
    @Override
    Void visitSynchronized(SynchronizedTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitSynchronized(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitThrow(com.sun.source.tree.ThrowTree, java.lang.Object)
     */
    @Override
    Void visitThrow(ThrowTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitTry(com.sun.source.tree.TryTree, java.lang.Object)
     */
    @Override
    Void visitTry(TryTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitTry(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitTypeCast(com.sun.source.tree.TypeCastTree, java.lang.Object)
     */
    @Override
    Void visitTypeCast(TypeCastTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitTypeCast(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitTypeParameter(com.sun.source.tree.TypeParameterTree, java.lang.Object)
     */
    @Override
    Void visitTypeParameter(TypeParameterTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitTypeParameter(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitUnary(com.sun.source.tree.UnaryTree, java.lang.Object)
     */
    @Override
    Void visitUnary(UnaryTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitUnary(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitVariable(com.sun.source.tree.VariableTree, java.lang.Object)
     */
    @Override
    Void visitVariable(VariableTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitVariable(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.sun.source.util.TreeScanner#visitWildcard(com.sun.source.tree.WildcardTree, java.lang.Object)
     */
    @Override
    Void visitWildcard(WildcardTree arg0, Void arg1) {
        // TODO Auto-generated method stub
        return super.visitWildcard(arg0, arg1);
    }


}
