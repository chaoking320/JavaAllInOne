package com.tsuperman.javaallinone.learn.jvm.oom;


/**
 * 栈溢出  -Xss:1m -XX:+PrintFlagsFinal
 */
public class StackOverFlow {
    static int i=0;
    long ll=9;
    //这种是死递归写法
    public void stack(){//方法不断执行-栈帧不断入栈(不出栈)
        long j=200;
        long k=900;
        long jk = j+k;
        i++;
        stack();//方法一直会执行在这一段
    }
    public static void main(String[] args)throws Throwable {
        try {
            StackOverFlow javaStack = new StackOverFlow();
            javaStack.stack();
        }catch (Throwable throwable){
            System.out.println("stack方法执行的次数："+i);
            throw  throwable;
        }

    }
}
