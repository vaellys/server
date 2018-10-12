package com.sccf.auth.handler;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * @author qianguobing
 * @date 2018-09-17 17:07
 */
@JsonSerialize(using = CustomOauthExceptionSerializer.class)
public class CustomOauthException  extends OAuth2Exception {
    public CustomOauthException(String msg) {
        super(msg);
    }
}
