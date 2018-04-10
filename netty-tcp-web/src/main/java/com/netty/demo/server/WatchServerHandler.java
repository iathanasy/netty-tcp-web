package com.netty.demo.server;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netty.demo.constant.Constants;
import com.netty.demo.server.connertor.impl.WatchConnertorImpl;
import com.netty.demo.server.enums.MessageEnums;
import com.netty.demo.server.model.MessageWrapper;
import com.netty.demo.server.model.proto.WatchMessageBodyProto;
import com.netty.demo.server.proxy.MessageProxy;
import com.netty.demo.server.session.impl.SessionManagerImpl;
import com.netty.demo.utils.ImUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

@Sharable
public class WatchServerHandler extends ChannelInboundHandlerAdapter{

	 private final static Logger log = LoggerFactory.getLogger(WatchServerHandler.class);
	 
	 private WatchConnertorImpl connertor = null;
	 private MessageProxy proxy = null;
	 
	public WatchServerHandler(){}
	public WatchServerHandler(WatchConnertorImpl connertor,MessageProxy proxy) {
		 this.connertor = connertor;
	     this.proxy = proxy;
	}
	 
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object o) throws Exception {
    	//发送心跳包
    	if (o instanceof IdleStateEvent && ((IdleStateEvent) o).state().equals(IdleState.WRITER_IDLE)) {
    		 //保存发送的时间
		     ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(System.currentTimeMillis());
		     String sessionId = connertor.getChannelSessionId(ctx);
		     //如果sessionId不等于null
			  if(StringUtils.isNotEmpty(sessionId)){
		    	// 如果写超时，把消息发送回去
				  connertor.heartbeatToClient(ctx);
			  } 
 			 log.debug(IdleState.WRITER_IDLE +"... from "+sessionId+"-->"+ctx.channel().remoteAddress()+" nid:" +ctx.channel().id().asShortText());
    		
 	    } 
	        
	    //如果心跳请求发出30秒内没收到响应，则关闭连接
	    if ( o instanceof IdleStateEvent && ((IdleStateEvent) o).state().equals(IdleState.READER_IDLE)){
	    	//读超时
			log.debug(IdleState.READER_IDLE +"... from "+ctx.channel().remoteAddress()+" nid:" +ctx.channel().id().asShortText());
	    	Long lastTime = (Long) ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).get();
	    	
	     	if(lastTime != null && System.currentTimeMillis() - lastTime >= Constants.ImserverConfig.PING_TIME_OUT)
	     	{
	     		//关闭当前连接管道
	     		connertor.close(ctx);
	     		//ctx.channel().close();
	     	}
	     	ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(null);
	    }
	}
	 
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
        try {
            if (o instanceof WatchMessageBodyProto.Model.Builder) {
            	WatchMessageBodyProto.Model.Builder message = (WatchMessageBodyProto.Model.Builder)o;
            	log.info("message:"+message.toString());
            	//获取sessionId
            	String sessionId = connertor.getChannelSessionId(ctx);
            	//将消息交给代理去完成
            	MessageWrapper wrapper = proxy.convertToMessageWrapper(sessionId, message);
            	if(wrapper != null)
            		receiveMessages(ctx, wrapper);
            } else {
                log.warn("WatchServerHandler channelRead message is not proto.");
            }
        } catch (Exception e) {
            log.error("WatchServerHandler channerRead error.", e);
            throw e;
        }
        
    }

	/*管道注册*/
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    	log.info("WatchServerHandler  join from "+ImUtils.getRemoteAddress(ctx)+" nid:" + ctx.channel().id().asShortText());
    }

    /*管道未注册*/
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("WatchServerHandler Disconnected from {" +ctx.channel().remoteAddress()+"--->"+ ctx.channel().localAddress() + "}");
    }

    /*管道连接成功*/
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.debug("WatchServerHandler channelActive from (" + ImUtils.getRemoteAddress(ctx) + ")");
    }

    /*管道连接断开*/
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.debug("WatchServerHandler channelInactive from (" + ImUtils.getRemoteAddress(ctx) + ")");
        String sessionId = connertor.getChannelSessionId(ctx);
        //关闭连接
        receiveMessages(ctx,new MessageWrapper(MessageEnums.CLOSE, sessionId,null, null));  
    }

    /*异常错误*/
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("WatchServerHandler (" + ImUtils.getRemoteAddress(ctx) + ") -> Unexpected exception from downstream." + cause);
    }
    
    /**
     * to send  message
     *
     * @param hander
     * @param wrapper
     */
    private void receiveMessages(ChannelHandlerContext hander, MessageWrapper wrapper) {
    	//设置消息来源为websocket
    	wrapper.setSource(Constants.ImserverConfig.SOCKET);
        if(wrapper.isHeartbeat()){
        	//心跳
        	connertor.heartbeat(hander, wrapper);
        }else if(wrapper.isClose()){
        	//关闭连接
        	connertor.close(hander, wrapper);
        }else if(wrapper.isGroup()){
        	//组消息
        	connertor.pushGroupMessage(wrapper);
        }else if(wrapper.isSend()){
        	//发送
        	connertor.pushMessage(wrapper);
        }else if(wrapper.isReply()){
        	//回复
        	connertor.pushMessage(wrapper.getSessionId(), wrapper);
        }else{
        	//其他数据
        }
    	
    }
}
