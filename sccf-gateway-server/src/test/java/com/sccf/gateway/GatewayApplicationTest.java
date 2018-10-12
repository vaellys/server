package com.sccf.gateway;

import com.sccf.GatewayApplication;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author qianguobing
 * @date 2018-09-18 14:39
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GatewayApplication.class)
@Slf4j
public class GatewayApplicationTest {

    @Autowired
    private StringEncryptor stringEncryptor;

    @Test
    public void testEnvironmentProperties() {
        log.info("============================clientId=============  " + stringEncryptor.encrypt("sccf-combanc"));
        log.info("============================clientSecret=============  " + stringEncryptor.encrypt("123456"));

        log.info("============================clientId=============  " + stringEncryptor.encrypt("root"));
        log.info("============================clientSecret=============  " + stringEncryptor.encrypt("christmas"));

        Assert.assertNotNull("");
    }
}
