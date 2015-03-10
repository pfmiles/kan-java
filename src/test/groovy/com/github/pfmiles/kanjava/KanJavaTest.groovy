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

}
