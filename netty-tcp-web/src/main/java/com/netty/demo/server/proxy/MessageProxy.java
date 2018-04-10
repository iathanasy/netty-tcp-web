package com.netty.demo.server.proxy;

import com.netty.demo.server.model.MessageWrapper;
import com.netty.demo.server.model.proto.WatchMessageBodyProto;
/**
* @Project: netty-tcp-web 
* @Class MessageProxy 
* @Description: 消息代理
* @author cd 14163548@qq.com
* @date 2017年12月11日 下午3:57:56 
* @version V1.0
 */
public interface MessageProxy {

	/**
	 * 消息代理
	 * @param sessionId
	 * @param message
	 * @return
	 */
	MessageWrapper  convertToMessageWrapper(String sessionId,WatchMessageBodyProto.Model.Builder message);
	
	 /**
     * 获取上线状态消息
     * @param sessionId
     * @return
     */
	WatchMessageBodyProto.Model.Builder getOnLineStateMsg(String sessionId);
	
	/**
     * 获取下线状态消息
     * @param sessionId
     * @return
     */
	WatchMessageBodyProto.Model.Builder getOffLineStateMsg(String sessionId);
}
