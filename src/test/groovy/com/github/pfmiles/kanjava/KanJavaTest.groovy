package com.github.pfmiles.kanjava;


/**
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 *
 */
class KanJavaTest extends GroovyTestCase {

    // 读取同package下的文件内容
    static def readContent(file){
        def writer = new StringWriter()
        def reader = new BufferedReader(new InputStreamReader(KanJavaTest.class.getResourceAsStream(file), "UTF-8"))
        writer << reader
        writer.toString()
    }

    // 测试编译成功情形：互相依赖的2个class
    void testCompileSuccess(){
        def kan = new KanJava()
        def srcs = []
        srcs << new JavaSourceFile("Foo.java", "kanjava.test", readContent("testSuccess/Foo.src"))
        srcs << new JavaSourceFile("Bar.java", "kanjava.test", readContent("testSuccess/Bar.src"))

        def rst = kan.compile(srcs, null)
        assertTrue rst.isSuccess()
        assertTrue rst.errMsg == null
        assertTrue(rst.classes !=null && rst.classes.size == 2)
        println "Static cp: " + rst.classes

        // dynamic classpath
        rst = kan.compile(srcs)
        assertTrue rst.isSuccess()
        assertTrue rst.errMsg == null
        assertTrue(rst.classes !=null && rst.classes.size == 2)
        println "Dynamic cp: " + rst.classes
    }

    // 测试基本情形，禁止assert语句
    void testAssert(){
        // test for assertion
        def kan = new KanJava(Feature.assertion)
        def srcs = []
        srcs << new JavaSourceFile("TestAssert.java", "kanjava.test", readContent("testAssert/TestAssert.src"));
        def rst = kan.compile(srcs, null)

        assertTrue !rst.isSuccess()
        assertTrue rst.errMsg != null
        assertTrue rst.classes == null
        println rst.errMsg
    }

    // 禁止普通for循环
    void testForLoop(){
        def kan = new KanJava(Feature.forLoop)
        def srcs = []
        srcs << new JavaSourceFile("TestForLoop.java", "kanjava.test", readContent("testForLoop/TestForLoop.src"));
        def rst = kan.compile(srcs)

        assertTrue !rst.isSuccess()
        assertTrue rst.errMsg != null
        assertTrue rst.classes == null
        println rst.errMsg
    }

    // 禁止while循环
    void testWhileLoop(){
        def kan = new KanJava(Feature.whileLoop)
        def srcs = []
        srcs << new JavaSourceFile("TestWhileLoop.java", "kanjava.test", readContent("testWhileLoop/TestWhileLoop.src"));
        def rst = kan.compile(srcs)

        assertTrue !rst.isSuccess()
        assertTrue rst.errMsg != null
        assertTrue rst.classes == null
        println rst.errMsg
    }

    // 禁止嵌套类
    void testNestedClass(){
        def kan = new KanJava(Feature.nestedClass)
        def srcs = []
        srcs << new JavaSourceFile("TestNestedClass.java", "kanjava.test", readContent("testNestedClass/TestNestedClass.src"));
        def rst = kan.compile(srcs)

        assertTrue !rst.isSuccess()
        assertTrue rst.errMsg != null
        assertTrue rst.classes == null
        println rst.errMsg
    }

    // 禁止do-while循环
    void testDoWhileLoop(){
        def kan = new KanJava(Feature.doWhileLoop)
        def srcs = []
        srcs << new JavaSourceFile("TestDoWhileLoop.java", "kanjava.test", readContent("testDoWhileLoop/TestDoWhileLoop.src"));
        def rst = kan.compile(srcs)

        assertTrue !rst.isSuccess()
        assertTrue rst.errMsg != null
        assertTrue rst.classes == null
        println rst.errMsg
    }

    // 禁止for-each循环
    void testEnhancedForLoop(){
        def kan = new KanJava(Feature.enhancedForLoop)
        def srcs = []
        srcs << new JavaSourceFile("TestEnhancedForLoop.java", "kanjava.test", readContent("testEnhancedForLoop/TestEnhancedForLoop.src"));
        def rst = kan.compile(srcs)

        assertTrue !rst.isSuccess()
        assertTrue rst.errMsg != null
        assertTrue rst.classes == null
        println rst.errMsg
    }

    // 禁止break语句
    void testBreakStatement(){
        def kan = new KanJava(Feature.breakStmt)
        def srcs = []
        srcs << new JavaSourceFile("TestBreakStmt.java", "kanjava.test", readContent("testBreakStmt/TestBreakStmt.src"));
        def rst = kan.compile(srcs)

        assertTrue !rst.isSuccess()
        assertTrue rst.errMsg != null
        assertTrue rst.classes == null
        println rst.errMsg
    }

    // 禁止带标签的break语句
    void testLabeledBreak(){
        def kan = new KanJava(Feature.labeledBreak)
        def srcs = []
        srcs << new JavaSourceFile("TestLabeledBreak.java", "kanjava.test", readContent("testLabeledBreak/TestLabeledBreak.src"));
        def rst = kan.compile(srcs)

        assertTrue !rst.isSuccess()
        assertTrue rst.errMsg != null
        assertTrue rst.classes == null
        println rst.errMsg
    }
}
