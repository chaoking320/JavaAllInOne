package com.tsuperman.javaallinone.learn.jvm.oom;

import java.nio.ByteBuffer;

/**
 * VM Args：-XX:MaxDirectMemorySize=100m -XX:+PrintFlagsFinal
 * 限制最大直接内存大小100m
 *
 * 注：当jdk24 想要加入如下参数时，无法识别（已移除）
 * -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:-UseG1GC
 */
public class DirectOom {
    public static void main(String[] args) {
        //直接分配128M的直接内存
        ByteBuffer bb = ByteBuffer.allocateDirect(128*1024*1204);
    }
}
