package com.sccf.auth.config;

import com.sccf.auth.handler.CustomOauthException;
import com.sccf.core.commons.constants.SecurityConstant;
import com.sccf.core.commons.jwt.RsaKeyHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务器逻辑实现
 */
@Configuration
@EnableAuthorizationServer
@Order(Integer.MIN_VALUE)
public class AuthorizationConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthServerConfiguration authServerConfiguration;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;


    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient(authServerConfiguration.getClientId())
                .secret(authServerConfiguration.getClientSecret())
                .authorizedGrantTypes(SecurityConstant.REFRESH_TOKEN, SecurityConstant.PASSWORD, SecurityConstant.AUTHORIZATION_CODE, SecurityConstant.CLIENT_CREDENTIALS)
                .scopes(authServerConfiguration.getScope())
                .accessTokenValiditySeconds(60 * 60)
                .refreshTokenValiditySeconds(24 * 60 * 60 * 7)
                // true 直接跳转到客户端页面，false 跳转到用户确认授权页面
                .autoApprove(true);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter()));
        endpoints.tokenStore(redisTokenStore())
                .tokenEnhancer(tokenEnhancerChain)
                .authenticationManager(authenticationManager)
                .reuseRefreshTokens(false)
                .userDetailsService(userDetailsService)
                .exceptionTranslator(webResponseExceptionTranslator());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients()
                // 获取JWt加密key: /oauth/token_key 采用RSA非对称加密时候使用。对称加密禁止访问
                 .tokenKeyAccess("isAuthenticated()")
                .checkTokenAccess("permitAll()");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @DependsOn("authServerRunner")
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        // 采用RSA非对称加密
         JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
         jwtAccessTokenConverter.setSigningKey(RsaKeyHelper.toHexString(authServerConfiguration.getPrivateKey()));
         jwtAccessTokenConverter.setVerifierKey(RsaKeyHelper.toHexString(authServerConfiguration.getPublicKey()));
         jwtAccessTokenConverter.setSigner(new RsaSigner((RSAPrivateKey)RsaKeyHelper.getPrivateKey(authServerConfiguration.getPrivateKey())));
         jwtAccessTokenConverter.setVerifier(new RsaVerifier((RSAPublicKey) RsaKeyHelper.getPublicKey(authServerConfiguration.getPublicKey())));
        return jwtAccessTokenConverter;
    }

    /**
     * tokenstore 定制化处理 1. 如果使用的 redis-cluster 模式请使用 FwRedisTokenStore FwRedisTokenStore tokenStore = new
     * FwRedisTokenStore();
     * tokenStore.setRedisTemplate(redisTemplate);
     */
    @Bean
    public TokenStore redisTokenStore() {
        RedisTokenStore tokenStore = new RedisTokenStore(redisConnectionFactory);
        tokenStore.setPrefix(SecurityConstant.PREFIX);
        return tokenStore;
    }

    /**
     * jwt 生成token 定制化处理
     *
     * @return TokenEnhancer
     */
    @Bean
    public TokenEnhancer tokenEnhancer() {
        return (accessToken, authentication) -> {
            final Map<String, Object> additionalInfo = new HashMap<>(1);
            additionalInfo.put("license", SecurityConstant.LICENSE);
            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
            return accessToken;
        };
    }

    @Bean
    public WebResponseExceptionTranslator webResponseExceptionTranslator() {
        return e -> {
            if(e instanceof OAuth2Exception) {
                OAuth2Exception oAuth2Exception = (OAuth2Exception) e;
                return ResponseEntity
                        .status(oAuth2Exception.getHttpErrorCode())
                        .body(new CustomOauthException(oAuth2Exception.getMessage()));
            }
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomOauthException(e.getMessage()));
        };
    }


}
