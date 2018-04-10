package com.netty.demo.server.model;

import java.io.Serializable;

import com.netty.demo.server.enums.MessageEnums;
public class MessageWrapper implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3036799586264586903L;
	private MessageEnums protocol;//消息枚举
    private String sessionId;//请求人 
    private String reSessionId;//接收人
    private int source;//来源 用于区分是websocket还是socekt
    private Object body;

    private MessageWrapper() {
    }

    /**
     * 初始化消息
     * @param protocol 枚举类型
     * @param sessionId 请求人
     * @param reSessionId 接收人
     * @param body 消息内容
     */
    public MessageWrapper(MessageEnums protocol,String sessionId,String reSessionId, Object body) {
        this.protocol = protocol;
    	this.sessionId = sessionId;
        this.reSessionId = reSessionId;
        this.body = body;
    }


    
	public void setProtocol(MessageEnums protocol) {
		this.protocol = protocol;
	}

	public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

	public String getReSessionId() {
		return reSessionId;
	}

	public void setReSessionId(String reSessionId) {
		this.reSessionId = reSessionId;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}
 
	/**
	 * 是否是心跳
	 * @return
	 */
	public boolean isHeartbeat(){
		return MessageEnums.LK.equals(this.protocol);
	}
	
	/**
	 * 是否是位置数据
	 * @return
	 */
	public boolean isPosition(){
		return MessageEnums.UD.equals(this.protocol)
				|| MessageEnums.UD2.equals(this.protocol);
	}
	
	/**
	 * 是否关闭
	 * @return
	 */
	public boolean isClose(){
		return MessageEnums.CLOSE.equals(this.protocol);
	}
	
	/**
	 * 发送
	 * @return
	 */
	public boolean isSend() {
	   return MessageEnums.SEND.equals(this.protocol);
	}

	/**
	 * 通知
	 * @return
	 */
    public boolean isNotify() {
        return MessageEnums.NOTIFY.equals(this.protocol);
    }
    
    /**
     * 组
     * @return
     */
    public boolean isGroup() {
        return MessageEnums.GROUP.equals(this.protocol);
    }
    
    /**
     * 回复
     * @return
     */
    public boolean isReply() {
        return MessageEnums.REPLY.equals(this.protocol);
    }
}
