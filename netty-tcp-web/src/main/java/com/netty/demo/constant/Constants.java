package com.netty.demo.constant;

import io.netty.util.AttributeKey;

/**
 * 
* @Project: netty-tcp-web 
* @Class Constants 
* @Description: TODO
* @author cd 14163548@qq.com
* @date 2017年12月8日 上午10:34:56 
* @version V1.0
 */
public class Constants {

	/**
	 * web
	 */
	public static interface WebSite{
		public static final int SUCCESS = 0;
		public static final int ERROR = -1;
	}

	/**
	 * 通知
	 */
	public static interface NotifyConfig{
		public static final int NOTIFY_SUCCESS = 1;
	    public static final int NOTIFY_FAILURE = 0;
	    public static final int NOTIFY_NO_SESSION = 2;
	}
	
	/**
	 * 服务器配置
	 */
	 public static interface ImserverConfig{
	    	//连接读空闲时间
	      	public static final int READ_IDLE_TIME = 120;//秒
	      	//连接写空闲时间
	      	public static final int WRITE_IDLE_TIME = 100;//秒
	        //心跳响应 超时时间
	      	public static final int PING_TIME_OUT = 30; //秒
	      	
	        // 最大协议包长度
	        public static final int MAX_FRAME_LENGTH = 1024 * 10; // 10k
	        //
	        public static final int MAX_AGGREGATED_CONTENT_LENGTH = 65536;
	        
	        public static final int WEBSOCKET = 1;//websocket标识
	        
	        public static final int SOCKET =0;//socket标识
	        
	        // [3G*3919753126*0009*LK,0,1255,99]
	        //定义接收客户端数据以什么分隔符结尾  socket tcp客户端用
	        public static final String DELIMITER = "]";
	    }
	 
	 /**
	  * session
	  */
	 public static interface SessionConfig{
    	 public static final String SESSION_KEY= "account" ;
    	 public static final String HEARTBEAT_KEY="heartbeat";
    	 public static final AttributeKey<String> SERVER_SESSION_ID = AttributeKey.valueOf(SESSION_KEY);
    	 public static final AttributeKey SERVER_SESSION_HEARBEAT = AttributeKey.valueOf(HEARTBEAT_KEY);
    }
	 
	/**
	 * 
	 */
	/*public static interface ProtobufType{
    	 byte SEND = 1; //请求
    	 byte RECEIVE = 2; //接收
    	 byte NOTIFY = 3; //通知
    	 byte REPLY = 4; //回复
	}*/
   
	/**
	 * 
	 */
    /*public static interface CmdType{
	   	 byte BIND = 1; //绑定  
	   	 byte HEARTBEAT = 2; //心跳 
	   	 byte ONLINE = 3; //上线
	   	 byte OFFLINE = 4; //下线 
	   	 byte MESSAGE = 5; //消息
	}*/
}
