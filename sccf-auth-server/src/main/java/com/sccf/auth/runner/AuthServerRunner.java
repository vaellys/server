package com.sccf.auth.runner;

import com.sccf.auth.config.AuthServerConfiguration;
import com.sccf.core.commons.jwt.RsaKeyHelper;
import com.sccf.core.configuration.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author qianguobing
 * @date 2018-09-14 17:59
 */
@Component
public class AuthServerRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthServerRunner.class);

    @Autowired
    private AuthServerConfiguration authServerConfiguration;

    @Autowired
    private RedisUtils redisUtils;

    private static final String REDIS_AUTH_KEY = "SCCF:AUTH:SECRET";
    private static final String REDIS_CLIENT_PRI_KEY = "CLIENT-PRI";
    private static final String REDIS_CLIENT_PUB_KEY = "CLIENT-PUB";

    @PostConstruct
    public void init()  {
        log.info("初始化加载");
        Map clientKeys;
        try {
            boolean has = (this.redisUtils.hasHashKey(REDIS_AUTH_KEY, REDIS_CLIENT_PRI_KEY)) &&
                    (this.redisUtils
                            .hasHashKey(REDIS_AUTH_KEY, REDIS_CLIENT_PUB_KEY));
            if (has) {
                this.authServerConfiguration.setPrivateKey(RsaKeyHelper.toBytes(this.redisUtils.getHash(REDIS_AUTH_KEY, REDIS_CLIENT_PRI_KEY).toString()));
                this.authServerConfiguration.setPublicKey(RsaKeyHelper.toBytes(this.redisUtils.getHash(REDIS_AUTH_KEY, REDIS_CLIENT_PUB_KEY).toString()));
            } else {
                String clientSecret = this.authServerConfiguration.getClientSecret();
                clientKeys = RsaKeyHelper.generateKey(clientSecret);
                this.redisUtils.setHash(REDIS_AUTH_KEY, REDIS_CLIENT_PRI_KEY, RsaKeyHelper.toHexString((byte[]) clientKeys.get("pri")));
                this.redisUtils.setHash(REDIS_AUTH_KEY, REDIS_CLIENT_PUB_KEY, RsaKeyHelper.toHexString((byte[]) clientKeys.get("pub")));
                this.authServerConfiguration.setPrivateKey((byte[]) clientKeys.get("pri"));
                this.authServerConfiguration.setPublicKey((byte[]) clientKeys.get("pub"));
            }
        } catch (Exception e) {
            log.error("生成密钥出错", e);
            return;
        }
    }
}
