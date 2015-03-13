kan-java
========

'kan-java' is '砍-java', speak frankly & literally.  

这是一个java代码动态编译工具，也就是能够把String形式的java代码实时地编译为字节码的工具；  

“动态编译”工具，其实自jdk1.6发布以来，应该出现过很多，不过kan-java的特点在于 —— 就像它的名字一样 —— 可以选择性地砍掉任意语言特性；  
  
也就是说 —— 这是一个可以在动态编译java代码的同时，对java语言语法做裁剪的动态编译工具。  

通过下面这个例子可以看出“裁剪”指的是什么意思：  

    // 禁止带标签的continue语句
    void testLabeledContinue(){
        def kan = new KanJava(Feature.labeledContinue)
        def srcs = []
        srcs << new JavaSourceFile("TestLabeledContinue.java", "kanjava.test", readContent("testLabeledContinue/TestLabeledContinue.src"));
        def rst = kan.checkAndCompile(srcs)

        assertTrue !rst.isSuccess()
        assertTrue rst.errMsg != null
        assertTrue rst.classes == null
        println rst.errMsg
    }
    
上述groovy代码创建了一个`KanJava`编译工具实例, 并指明想要砍掉`labeledContinue`特性(即带标签的continue语句)  
其中`readContent`方法的返回结果如下：  

    package kanjava.test;

    public class TestLabeledContinue {
    
        public static void main(String... args) {
            for(int i=0;i<10;i++){
                if(i == 5) continue;
            }
            label: while(true){
                if(true) continue label;
            }
        }
    }
    
上述代码包含2个continue语句：第一个不带标签而第二个带标签  
最终输出结果如下：  

    Error at row: 10, col: 22, reason: Continue statements with labels are not allowed.
    
即“带标签的continue语句”已被禁止了，在编译过程中发现这种语句即会报错, 其核心功能，概念上讲就是这么简单。  

### 这能有什么用？ ###
拥有一个裁剪版本的java，这有怎样的应用场景？  

目前最直接的答案是"高性能的内部DSL"  

即当我需要一个语法上非常接近普通过程式编程语言的DSL，但却又不想或觉得没必要自己从头实现一个(外部DSL)的时候，就可以考虑以某种现成的过程式通用编程语言为蓝本，通过裁剪其语法达到目的；  
而当这种“现成的过程式通用编程语言”被选择为`java`时，kan-java出场的时刻就到了, 试想一下，下面这样“砍”会砍出来什么效果？ —— 

    private static final KanJava kanJava = new KanJava(Feature.assertion, 
                                                       Feature.doWhileLoop,
                                                       Feature.forLoop,
                                                       Feature.labeledBreak,
                                                       Feature.labeledContinue,
                                                       Feature.nestedClass,
                                                       Feature.whileLoop);

相信所有java程序员都可以猜到：你将得到一个“没有assert语句、没有do-while循环、没有for循环、没有带标签的break、没有带标签的continue、没有嵌套类、没有while循环”的 —— java.  
P.S. 如果你还坚信它是java的话 :)  

而这些"内部DSL"最终将被编译成字节码运行，因此也有了高速运行的基础；  
所以说kan-java能够成为“利用java实现高性能的内部DSL”的强大工具。  

上面示例中的这种“砍”法并不夸张，这是从现实中的使用案例中截选出来的。  

### 一些更实用 & 更高级的功能 ###
kan-java提供的api能够将“砍语法”和“编译为字节码”拆分为两个步骤；  
这使得你可以 —— 比如说 —— 在用户输入的时候禁掉'import语句'，而实际编译的时候可以正常插入import语句后再编译, 相信这种功能会很有用;  

更普遍意义地讲，kan-java实际上提供了一套"java语言语法静态处理框架", 在此框架之上，“砍”语法其实只是冰山一角 —— 因为你还可以用它来“砍用法”，比如你并不想完全禁掉`import`语句，但希望禁止import一些特定的类;  
再比如你不想完全禁止用户`new`对象，但你能够做到不让用户`new`特定的对象...  

凡是能够出现在代码当中的任意结构，都可以被控制。  
因此可以说，被发布出来的kan-java库只是一个小小的核心，其更加广阔的应用场景还有待猿们继续扩充...  

有没有更加“高级(黑)”的话题？ 当然有；因为在kan-java提供的这套框架之上不仅限于能“砍”，它还能“加”...  
不过目前这个库的主要目的还是提供一套“基于java的内部DSL构建工具”，其它的什么“用kan-java做源码增强”，什么“用kan-java把java编译到GPU上”这些黑科技就暂不展开了 :)  

### 注意事项 ###
最重要的事情总是最后才说...  

目前kan-java所支持的java基础语法是1.6的，也就是说，你使用kan-java来“砍”语法的时候，是以java 1.6为基础来砍的  
不过这并不影响kan-java库被放到更高版本的java环境中使用(above v1.6), 起码大多数情况下是ok的；不过，如果真的遇到问题，还是最好能从源码编译一份对应当前java版本环境的kan-java库(因为kan-java在实现上使用了`com.sun`包下的一些类, 这些类并不完全保证在不同版本java之间的兼容)  

同样因为kan-java使用了`com.sun`包下的类，我也只能假定kan-java只能在oracle jvm上运行  

目前开放的可被“砍”的功能，只是源于目前我个人在实际项目中的需要而已；肯定还有更多可能的“花式砍法”，如果希望有，可以提出来，有兴趣的我们可以共建  

使用kan-java时，需确保tools.jar也在classpath中    

按道理讲，除了java标准库，kan-java是不需要依赖任何第三方库的，不过项目中出现了对groovy-all的依赖，这仅仅是因为我想实践一把"java和groovy混编开发模式"的任性而已，不要太在意 :)  

