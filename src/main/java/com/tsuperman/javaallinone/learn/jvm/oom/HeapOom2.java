package com.tsuperman.javaallinone.learn.jvm.oom;

import java.util.LinkedList;
import java.util.List;


/**
 * VM Args：-Xms30m -Xmx30m -XX:+PrintFlagsFinal    堆的大小30M
 * 造成一个堆内存溢出(分析下JVM的分代收集)
 * GC调优---生产服务器推荐开启(默认是关闭的)
 * -XX:+HeapDumpOnOutOfMemoryError
 */
public class HeapOom2 {
   public static void main(String[] args) throws Exception {
       List<Object> list = new LinkedList<>();
       int i =0;
       while(true){
           i++;
           if(i%1000==0) Thread.sleep(10);
           list.add(new Object());// 不能回收
       }

   }
}
