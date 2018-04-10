package com.netty.demo.server.session.impl;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netty.demo.constant.Constants;
import com.netty.demo.server.group.WatchChannelGroup;
import com.netty.demo.server.model.MessageWrapper;
import com.netty.demo.server.model.Session;
import com.netty.demo.server.model.proto.WatchMessageBodyProto;
import com.netty.demo.server.model.proto.WatchMessageProto;
import com.netty.demo.server.proxy.MessageProxy;
import com.netty.demo.server.session.SessionManager;
import com.netty.demo.utils.ImUtils;

public class SessionManagerImpl implements SessionManager {

	private final static Logger log = LoggerFactory.getLogger(SessionManagerImpl.class);
	
	private MessageProxy proxy;
    
    public void setProxy(MessageProxy proxy) {
		this.proxy = proxy;
	}
	
    /**
     * The set of currently active Sessions for this Manager, keyed by session
     * identifier.
     */
    protected Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();
    
	public synchronized void addSession(Session session) {
		if (null == session) {
            return;
        } 
        sessions.put(session.getAccount(), session);
        WatchChannelGroup.add(session.getSession());
        //全员发送上线消息
        //WatchChannelGroup.broadcast(proxy.getOnLineStateMsg(session.getAccount()));
        log.debug("put a session " + session.getAccount() + " to sessions!");
	}

	public synchronized void updateSession(Session session) {
		 session.setUpdateTime(System.currentTimeMillis());
	     sessions.put(session.getAccount(), session);
	}

	/**
     * Remove this Session from the active Sessions for this Manager.
     */
    public synchronized void removeSession(String sessionId) {
    	try{
    		Session session = getSession(sessionId);
    		if(session!=null){
    			session.closeAll();
				sessions.remove(sessionId);
				if(session.getSource() == Constants.ImserverConfig.WEBSOCKET){
					
				}
				//广播下线消息
				//WatchChannelGroup.broadcast(proxy.getOffLineStateMsg(sessionId));
    		}  
    	}catch(Exception e){
    		
    	} 
    	log.info("system remove the session " + sessionId + " from sessions!");
    } 

    public synchronized void removeSession(String sessionId,String nid) {
    	try{
    		Session session = getSession(sessionId);
    		if(session!=null){
    			if(session.getSource()==Constants.ImserverConfig.WEBSOCKET){
    				session.close(nid);
    				//判断没有其它session后 从SessionManager里面移除
    				if(session.otherSessionSize()<1){
    					sessions.remove(sessionId);
    					//广播下线消息
    					WatchChannelGroup.broadcast(proxy.getOffLineStateMsg(sessionId));
    				} 
    			}else{
    				session.close();
    				sessions.remove(sessionId);
    				//广播下线消息
    				WatchChannelGroup.broadcast(proxy.getOffLineStateMsg(sessionId));
    			}
    		}  
    	}catch(Exception e){
    		
    	}finally{
    		
    		
    	}
        log.info("remove the session " + sessionId + " from sessions!");
    } 

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public Session[] getSessions() {
        return sessions.values().toArray(new Session[0]);
    }

    public Set<String> getSessionKeys() {
        return sessions.keySet();
    }

    public int getSessionCount() {
        return sessions.size();
    }


    public Session createSession(MessageWrapper wrapper, ChannelHandlerContext ctx) {
    	
    	String sessionId = wrapper.getSessionId();
        Session session = sessions.get(sessionId);
        if (session != null) {
        	log.info("session " + sessionId + " exist!");
        	//当链接来源不是同一来源或者 是socket链接，踢掉已经登录的session 
        	if(session.getSource()!=wrapper.getSource() || session.getSource()==Constants.ImserverConfig.SOCKET){
        		// 如果session已经存在则销毁session
                //从广播组清除
        		log.info("sessionId" + session.getNid() +"------------------"+ ctx.channel().id().asShortText()+ "      !");
                WatchChannelGroup.remove(session.getSession());
                //session.close();
                sessions.remove(session.getAccount());
                log.info("session " + sessionId + " have been closed!");
        	}else if(session.getSource()==Constants.ImserverConfig.WEBSOCKET){
        		//用于解决websocket多开页面session被踢下线的问题
        		Session  newsession = setSessionContent(ctx,wrapper,sessionId);
        		session.addSessions(newsession);
        		updateSession(session);
        		WatchChannelGroup.add(newsession.getSession());
                log.info("session " + sessionId + " update!");
        		return newsession;
        	}  
        }
       
        session = setSessionContent(ctx,wrapper,sessionId);
        addSession(session);
      
        return session;
    }
    
    /**
     * 设置session内容
     * @param ctx
     * @param wrapper
     * @param sessionId
     * @return
     */
    private Session setSessionContent(ChannelHandlerContext ctx,MessageWrapper wrapper,String sessionId){
    	 log.info("create new session " + sessionId + ", ctx -> " + ctx.toString());
    	  WatchMessageBodyProto.Model.Builder model = (WatchMessageBodyProto.Model.Builder)wrapper.getBody();
    	  Session session = new Session(ctx.channel());
          session.setAccount(sessionId); //session绑定的账号
          session.setSource(wrapper.getSource());//来源
          session.setAppKey(ImUtils.getRemoteAddress(ctx));//客户端ip地址
          session.setDeviceId(model.getDeviceId());//设备Id
          String plat = "websocket";//网页
          if(wrapper.getSource() == Constants.ImserverConfig.SOCKET){
        	  plat = "watch"; //手表
          }
          session.setPlatform(plat); //终端类型
          session.setPlatformVersion("1.0");
          session.setBindTime(System.currentTimeMillis());
          session.setUpdateTime(session.getBindTime());
          log.info("create new session " + sessionId + " successful!");
          return session;
    }
    
	public boolean exist(String sessionId) {
        Session session = getSession(sessionId);
        return session != null ? true : false;
	}

}
