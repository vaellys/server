package com.sccf.auth.component.ajax;

import com.sccf.core.commons.constants.CommonConstant;
import com.sccf.core.commons.constants.SecurityConstant;
import com.sccf.core.util.StringHelper;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AjaxAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private boolean	postOnly	= true;

	public AjaxAuthenticationFilter() {
		super(new AntPathRequestMatcher(SecurityConstant.MOBILE_TOKEN_URL, "POST"));
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		if (postOnly && !request.getMethod().equals(HttpMethod.POST.name()))
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());

		String mobile = obtainMobile(request);
		if (StringHelper.isBlank(mobile))
			mobile = "";

		AjaxAuthenticationToken ajaxAuthenticationToken = new AjaxAuthenticationToken(mobile.trim());

		setDetails(request, ajaxAuthenticationToken);

		return this.getAuthenticationManager()
				.authenticate(ajaxAuthenticationToken);
	}

	private String obtainMobile(HttpServletRequest request) {
		return request.getParameter(CommonConstant.SPRING_SECURITY_FORM_MOBILE_KEY);
	}

	private void setDetails(HttpServletRequest request, AjaxAuthenticationToken authRequest) {
		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
	}

	public void setPostOnly(boolean postOnly) {
		this.postOnly = postOnly;
	}

	public boolean isPostOnly() {
		return postOnly;
	}
}
