package com.netty.demo.server.proxy.impl;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netty.demo.server.enums.MessageEnums;
import com.netty.demo.server.model.MessageWrapper;
import com.netty.demo.server.model.proto.WatchMessageBodyProto;
import com.netty.demo.server.model.proto.WatchMessageBodyProto.Model.Builder;
import com.netty.demo.server.proxy.MessageProxy;
import com.netty.demo.utils.WatchMessageParsingUtils;

public class MessageProxyImpl implements MessageProxy {
	private final static Logger log = LoggerFactory.getLogger(MessageProxyImpl.class);
	
	public MessageWrapper convertToMessageWrapper(String sessionId,
			Builder message) {
		switch (message.getType()) {
		case "LK"://链接
			
			try {
				//存数据库操作
				message.setBody("");
				//返回消息封装
				return new MessageWrapper(MessageEnums.LK,message.getDeviceId(), null, message);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "UD"://位置上报
			try {
				//存数据库操作
				message.setBody("");
				//返回消息封装
				return new MessageWrapper(MessageEnums.UD,message.getDeviceId(), null, message);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		
		default:
			//其他数据
			message.setTimeStamp(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
			message.setSender(sessionId);//存入发送人sessionId
			try{
				  //判断消息是否有接收人
				  if(StringUtils.isNotEmpty(message.getReceiver())){//回复
					  return new MessageWrapper(MessageEnums.REPLY,message.getDeviceId(), message.getReceiver(), message);
				  }else if(StringUtils.isNotEmpty(message.getGroupId())){//组
					  return new MessageWrapper(MessageEnums.GROUP,message.getDeviceId(),null, message);
				  }else{
					  return new MessageWrapper(MessageEnums.SEND,message.getDeviceId(),null, message);
				  }
			 } catch (Exception e) {
	             e.printStackTrace();
	         }
			break;
		}
		
		return null;
	}

	@Override
	public Builder getOnLineStateMsg(String sessionId) {
		
		return WatchMessageParsingUtils.generateTemplate(sessionId, "ONLINE", "0");
	}

	@Override
	public Builder getOffLineStateMsg(String sessionId) {
		return WatchMessageParsingUtils.generateTemplate(sessionId, "OFFLINE", "1");
	}

}
