package com.netty.demo.server.connertor;

import com.netty.demo.server.model.MessageWrapper;

import io.netty.channel.ChannelHandlerContext;

/**
* @Project: netty-tcp-web 
* @Class WatchConnertor 
* @Description: 连接器
* @author cd 14163548@qq.com
* @date 2017年12月11日 下午3:57:30 
* @version V1.0
 */
public interface WatchConnertor {

	/**
	 * 发送心跳包给客户端
	 * @param sessionId
	 */
	void heartbeatToClient(ChannelHandlerContext ctx);

	/**
	 * 根据消息封装关闭
	 * @param hander
	 * @param wrapper
	 */
	 void close(ChannelHandlerContext hander,MessageWrapper wrapper);
	 
	 /**
	  * 根据sessionId关闭
	  * @param sessionId
	  */
	 void close(String sessionId);
	 
	 /**
	  * 根据hander关闭
	  * @param hander
	  */
	 void close(ChannelHandlerContext hander);
	 /**
	  * 获取用户唯一标识符
	  * @param ctx
	  * @return
	  */
	 String getChannelSessionId(ChannelHandlerContext ctx); 
	 
	 /**
	  * 回复心跳链接指令 并保存当前管道链路
	  * @param hander
	  * @param wrapper
	  */
	 void  heartbeat(ChannelHandlerContext ctx,MessageWrapper wrapper);
	 
	 /**
	  * 发送消息
	  * @param wrapper
	  * @throws RuntimeException
	  */
	 void pushMessage(MessageWrapper wrapper) throws RuntimeException;
	 
	 /**
	  * 发送消息指定消息
	  * @param sessionId  发送人
	  * @param wrapper   发送内容
	  * @throws RuntimeException
	  */
	 void pushMessage(String sessionId,MessageWrapper wrapper) throws RuntimeException;
	 
	 /**
	  * 发送组消息
	  * @param wrapper
	  * @throws RuntimeException
	  */
	 void pushGroupMessage(MessageWrapper wrapper) throws RuntimeException;
	 
}
