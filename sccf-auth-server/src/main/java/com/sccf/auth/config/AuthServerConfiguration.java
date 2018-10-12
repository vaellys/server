package com.sccf.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "sccf.auth")
public class AuthServerConfiguration {

    /**
     * 客户端id
     */
    private String clientId;

    /**
     * 客户端密码
     */
    private String clientSecret;

    /**
     * scope
     */
    private String scope;

    /**
     * RSA private 密匙
     */
    private byte[] privateKey;

    /**
     * RSA public 公匙
     */
    private byte[] publicKey;

}
