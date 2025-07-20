package com.tsuperman.javaallinone.learn.jvm.oom;


/**
 * VM Args：-Xms30m -Xmx30m -XX:+PrintGCDetails -XX:+PrintFlagsFinal
 * 堆内存溢出（直接溢出）
 */
public class HeapOom {
   public static void main(String[] args)
   {
       String[] strings = new String[35*1024*1024];  //35m的数组（堆）
   }
}
