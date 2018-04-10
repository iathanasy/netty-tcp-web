package com.netty.demo.server.connertor.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import com.netty.demo.constant.Constants;
import com.netty.demo.server.connertor.WatchConnertor;
import com.netty.demo.server.enums.MessageEnums;
import com.netty.demo.server.exception.PushException;
import com.netty.demo.server.group.WatchChannelGroup;
import com.netty.demo.server.model.MessageWrapper;
import com.netty.demo.server.model.Session;
import com.netty.demo.server.model.proto.WatchMessageBodyProto;
import com.netty.demo.server.proxy.MessageProxy;
import com.netty.demo.server.session.impl.SessionManagerImpl;
import com.netty.demo.utils.WatchMessageParsingUtils;
public class WatchConnertorImpl implements WatchConnertor {

	private final static Logger log = LoggerFactory.getLogger(WatchConnertorImpl.class);
	
	private SessionManagerImpl sessionManager;
    private MessageProxy proxy;
    
	public void setSessionManager(SessionManagerImpl sessionManager) {
		this.sessionManager = sessionManager;
	}
	public void setProxy(MessageProxy proxy) {
		this.proxy = proxy;
	}
	
	
	
	@Override
	public void close(ChannelHandlerContext hander, MessageWrapper wrapper) {
		String sessionId = getChannelSessionId(hander);
        if (StringUtils.isNotBlank(sessionId)) {
        	close(hander); 
            log.warn("connector close channel sessionId -> " + sessionId + ", ctx -> " + hander.toString());
        }
	      //信息
	      info();
	}
	@Override
	public void close(String sessionId) {
		try {
       	 Session session = sessionManager.getSession(sessionId);
     		 if (session != null) {
     			sessionManager.removeSession(sessionId); 
     			List<Channel> list = session.getSessionAll();
     			for(Channel ch:list){
     				WatchChannelGroup.remove(ch);
     			} 
     		    log.info("connector close sessionId -> " + sessionId + " success " );
     		 }
	     } catch (Exception e) {
       	log.error("connector close sessionId -->"+sessionId+"  Exception.", e);
           throw new RuntimeException(e.getCause());
	     }
		//信息
	    info();
	}
	@Override
	public void close(ChannelHandlerContext hander) {
		String sessionId = getChannelSessionId(hander);
		   try {
			    String nid = hander.channel().id().asShortText();
	        	Session session = sessionManager.getSession(sessionId);
	      		if (session != null) {
	      			sessionManager.removeSession(sessionId,nid); 
	      			WatchChannelGroup.remove(hander.channel());
	      		  log.info("connector close sessionId -> " + sessionId + " success " );
	      		}
	        } catch (Exception e) {
	        	log.error("connector close sessionId -->"+sessionId+"  Exception.", e);
	            throw new RuntimeException(e.getCause());
	        }
		 //信息
		 info();
	}

	
	public String getChannelSessionId(ChannelHandlerContext ctx) {
		return ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).get();
	}

	/**
	 * 设置用户保存
	 * @param ctx
	 * @param sessionId
	 */
	private void setChannelSessionId(ChannelHandlerContext ctx, String sessionId) {
        ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).set(sessionId);
    }
	
	@Override
	public void heartbeat(ChannelHandlerContext ctx, MessageWrapper wrapper) {
		//设置心跳响应时间
		ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(System.currentTimeMillis());
		try {
			 //获取传进来的sessionId
			 String sessionId = wrapper.getSessionId();
			 //获取当前管道的sessionId
	       	 String sessionId0 = getChannelSessionId(ctx);
	       	if (sessionId.equals(sessionId0)) {
                log.info("connector reconnect sessionId -> " + sessionId + ", ctx -> " + ctx.toString());
                //发送回复LK指令
                pushMessage(wrapper);
            } else {
                log.info("connector connect sessionId -> " + sessionId + ", sessionId0 -> " + sessionId0 + ", ctx -> " + ctx.toString());
                Session session = sessionManager.createSession(wrapper, ctx);
                //保存当前链路
                setChannelSessionId(ctx, sessionId);
                log.info("create channel attr sessionId " + sessionId + " successful, ctx -> " + ctx.toString());
                //回复LK指令
                pushMessage(wrapper);
            }
		} catch (Exception e) {
			log.error("connector connect  Exception.", e);
		}
		//信息
		info();
	}
	
	
	@Override
	public void pushMessage(MessageWrapper wrapper) throws RuntimeException {
		try {
        	//sessionManager.send(wrapper.getSessionId(), wrapper.getBody());
        	Session session = sessionManager.getSession(wrapper.getSessionId());
      		/*
      		 * 服务器集群时，可以在此
      		 * 判断当前session是否连接于本台服务器，如果是，继续往下走，如果不是，将此消息发往当前session连接的服务器并 return
      		 * if(session!=null&&!session.isLocalhost()){//判断当前session是否连接于本台服务器，如不是
      		 * //发往目标服务器处理
      		 * return; 
      		 * }
      		 */ 
      		if (session != null) {
      			
      			session.write(wrapper.getBody());
      			return;
      		}
        } catch (Exception e) {
        	log.error("connector pushMessage  Exception.", e);
            throw new RuntimeException(e.getCause());
        }
		
	}
	
	public void info(){
		log.info("----------------------------------------------");
        log.info("链接个数："+sessionManager.getSessionCount());
        for (String key : sessionManager.getSessionKeys()) {
			log.info("key:"+key);
        }
	}
	@Override
	public void pushMessage(String sessionId, MessageWrapper wrapper)
			throws RuntimeException {
		/*获取session*/
		Session session = sessionManager.getSession(sessionId);
        if (session == null) {
        	 throw new RuntimeException(String.format("session %s is not exist.", sessionId));
        } 
        
        try {
	    	///取得接收人 给接收人写入消息
	    	Session responseSession = sessionManager.getSession(wrapper.getReSessionId());
	  		if (responseSession != null ) {
	  			responseSession.write(wrapper.getBody());
	  			//保存在线消息
	  			//proxy.saveOnlineMessageToDB(wrapper);
	  			return;
	  		}else{
	  			//保存离线消息
	  			//proxy.saveOfflineMessageToDB(wrapper);
	  		}
	    } catch (PushException e) {
	    	log.error("connector send occur PushException.", e);
	       
	        throw new RuntimeException(e.getCause());
	    } catch (Exception e) {
	    	log.error("connector send occur Exception.", e);
	        throw new RuntimeException(e.getCause());
	    }  
		
	}
	@Override
	public void pushGroupMessage(MessageWrapper wrapper)
			throws RuntimeException {
		 //这里判断群组ID 是否存在 并且该用户是否在群组内
		  WatchChannelGroup.broadcast(wrapper.getBody());
		  //保存消息
		  // proxy.saveOnlineMessageToDB(wrapper);
		
	}
	
	@Override
	public void heartbeatToClient(ChannelHandlerContext ctx) {
		String sessionId = getChannelSessionId(ctx);
		WatchMessageBodyProto.Model.Builder builder = WatchMessageParsingUtils.generateTemplate(sessionId, MessageEnums.LK+"", "");
    	// 如果写超时，把消息发送回去
		ctx.channel().writeAndFlush(builder);
		
	}
}
