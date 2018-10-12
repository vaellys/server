package com.sccf.auth.service;

import com.sccf.api.feign.IUserFeignService;
import com.sccf.api.feign.dto.AuthUserVo;
import com.sccf.core.commons.base.RestResponse;
import com.sccf.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service("userDetailService")
public class UserDetailServiceImpl implements UserDetailsService, Serializable {

    private static final long serialVersionUID = 5181442448895412779L;

    @Autowired
    private IUserFeignService userFeignApi;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (StringHelper.isBlank(username))
            throw new UsernameNotFoundException("用户不存在:" + username);

        RestResponse<AuthUserVo> responseEntity = userFeignApi.findUserByUsername(username);
        AuthUserVo authUserVo = responseEntity.getResult();
        if (null == authUserVo) {
            throw new UsernameNotFoundException("用户不存在:" + username);
        }
        return new UserDetailsImpl(responseEntity.getResult());
    }

}
