package com.tsuperman.javaallinone.learn.redis.redismq;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestPSVer {

    @Autowired
    private PubSubVer psVer;

    @Test
    void testSub(){
        psVer.sub(PubSubVer.KEY+"psmq", PubSubVer.KEY+"psmq2");
    }

    @Test
    void testPub(){
        psVer.pub("psmq","msgtest");
        psVer.pub("psmq2","msgtest2");
    }

}
