package com.sccf.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sccf.core.commons.base.RestResponse;
import com.sccf.core.commons.constants.CommonConstant;
import com.sccf.core.commons.constants.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * 授权拒绝处理器，覆盖默认的OAuth2AccessDeniedHandler 包装失败信息到DeniedException
 *
 */
@Slf4j
@Component
public class AccessDeniedHandler extends OAuth2AccessDeniedHandler {

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException authException) throws IOException, ServletException {
		log.info("授权失败，禁止访问 {}", request.getRequestURI());
		response.setCharacterEncoding(CommonConstant.UTF8);
		response.setContentType(CommonConstant.CONTENT_TYPE);
		RestResponse restResponse = new RestResponse();
		restResponse.setStatus(HttpStatus.SC_FORBIDDEN);
		restResponse.setMessage(MessageConstant.COMMONS_AUTH_NOTSUPPORT);
		PrintWriter printWriter = response.getWriter();
		printWriter.append(objectMapper.writeValueAsString(restResponse));
	}
}
