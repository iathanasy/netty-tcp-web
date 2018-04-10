package com.netty.demo.server.session;

import io.netty.channel.ChannelHandlerContext;

import java.util.Set;

import com.netty.demo.server.model.MessageWrapper;
import com.netty.demo.server.model.Session;

public interface SessionManager {

	/**
     * 添加指定session
     *
     * @param session
     */
    void addSession(Session session);

    void updateSession(Session session);
 

    /**
     * 删除指定session
     *
     * @param sessionId
     */
     void removeSession(String sessionId);

    /**
     * 删除指定session
     * 
     *
     * @param sessionId
     * @param nid  is socketid 
     */
     void removeSession(String sessionId,String nid);

    /**
     * 根据指定sessionId获取session
     *
     * @param sessionId
     * @return
     */
    Session getSession(String sessionId);

    /**
     * 获取所有的session
     *
     * @return
     */
    Session[] getSessions();

    /**
     * 获取所有的session的id集合
     *
     * @return
     */
    Set<String> getSessionKeys();

    /**
     * 获取所有的session数目
     *
     * @return
     */
    int getSessionCount();
 
    /**
     * 创建session
     * @param wrapper
     * @param ctx
     * @return
     */
    Session createSession(MessageWrapper wrapper, ChannelHandlerContext ctx);
    
    boolean exist(String sessionId) ;
}
