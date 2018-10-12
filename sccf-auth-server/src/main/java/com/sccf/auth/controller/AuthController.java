package com.sccf.auth.controller;

import com.sccf.core.commons.base.RestBaseController;
import com.sccf.core.commons.base.RestResponse;
import com.sccf.core.commons.constants.SecurityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController extends RestBaseController {

	@Autowired
	@Qualifier("consumerTokenServices")
	private ConsumerTokenServices	consumerTokenServices;

	private AuthorizationServerTokenServices authorizationServerTokenServices;

	@DeleteMapping("/removeToken")
	@CacheEvict(value = SecurityConstant.TOKEN_USER_DETAIL, key = "#accessToken")
	public RestResponse<Boolean> removeToken(String accessToken) {
		boolean isRemoved = consumerTokenServices.revokeToken(accessToken);
		return wrap(isRemoved);
	}

}
