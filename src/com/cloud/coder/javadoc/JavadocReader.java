package com.cloud.coder.javadoc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;

import java.util.List;

public class JavadocReader {
    private static RootDoc root = null;
    public static  class ApiDoclet extends Doclet {
        public static boolean start(RootDoc root) {
            JavadocReader.root = root;
            return true;
        }
        public ApiDoclet() {
            super();
        }
    }
    /**
     * 解析指定的java源文件返回javadoc对象 {@link RootDoc}<br>
     * 参见 <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/javadoc/standard-doclet.html#runningprogrammatically">Running the Standard Doclet Programmatically</a>
     * @param source a java source file or package name
     * @param classpath value for  '-classpath',{@code source}的class位置,可为{@code null},如果不提供,无法获取到完整的注释信息(比如annotation)
     * @param sourcepath value for '-sourcepath'
     * @return
     */
    public synchronized static RootDoc readDocs(String source, String classpath,String sourcepath, String subpackages) {
        if(Strings.isNullOrEmpty(source)){
            return null;
        }
        Preconditions.checkArgument(!Strings.isNullOrEmpty(source), "source is null");
        List<String> args = Lists.newArrayList("-doclet", ApiDoclet.class.getName(), "-quiet","-Xmaxerrs","1","-Xmaxwarns","1","-encoding","utf-8","-subpackages", subpackages);
        if(!Strings.isNullOrEmpty(classpath)){
            args.add("-classpath");
            args.add(classpath);
        }
        if(!Strings.isNullOrEmpty(sourcepath)){
            args.add("-sourcepath");
            args.add(sourcepath);
        }
        args.add(source);
        int returnCode = com.sun.tools.javadoc.Main.execute(JavadocReader.class.getClassLoader(),args.toArray(new String[args.size()]));
        if(0 != returnCode){
            throw new IllegalStateException();
        }
        return root;
    }
}
