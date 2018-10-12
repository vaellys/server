package com.sccf.gateway.service;

import com.sccf.api.feign.IPermissionFeignService;
import com.sccf.core.commons.constants.SecurityConstant;
import com.sccf.core.commons.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 权限接口实现
 */
@Slf4j
@Service("permissionService")
public class PermissionServiceImpl implements PermissionService {

    /**
     * 具体Admin工程实现
     */
    @Autowired
    private IPermissionFeignService permissionFeignApi;

    /**
     * redis 工厂类
     */
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasPermission(HttpServletRequest request, Authentication authentication) {

        Object principal = authentication.getPrincipal();
        List<SimpleGrantedAuthority> grantedAuthorityList = (List<SimpleGrantedAuthority>) authentication
                .getAuthorities();
        boolean hasPermission = false;

        if (null == principal) return hasPermission;
        if (CollectionUtils.isEmpty(grantedAuthorityList)) return hasPermission;

        String token = JwtUtil.getToken(request);
        if (null == token) {
            log.warn("==> gateway|permissionService 未获取到Header Authorization");
            return hasPermission;
        }

//        if (!"anonymousUser".equals(principal.toString())) {
        RedisTokenStore tokenStore = new RedisTokenStore(redisConnectionFactory);
        tokenStore.setPrefix(SecurityConstant.PREFIX);
        OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);
        if (null == accessToken || accessToken.isExpired()) {
            log.warn("==> gateway|permissionService token 过期或者不存在");
            return hasPermission;
        } else {
            return true;
        }
    }
}
