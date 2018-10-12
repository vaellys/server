package com.sccf.gateway.service;

import com.netflix.zuul.context.RequestContext;

/**
 * 往消息通道发消息
 *
 */
public interface LogService {

	/**
	 * 往消息通道发消息
	 */
	void send(RequestContext requestContext);
}
