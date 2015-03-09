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

    void testCompile(){
        // test for assertion
        def kan = new KanJava(Feature.assertion)
        def srcs = []
        srcs << new JavaSourceFile("TestCompile.java", "kanjava.test", readContent("TestCompile.src"));
        def rst = kan.compile(srcs, null)

        assertTrue !rst.isSuccess()
        assertTrue rst.errMsg != null
        assertTrue rst.compiledClassFiles == null
        println rst.errMsg
    }

}
