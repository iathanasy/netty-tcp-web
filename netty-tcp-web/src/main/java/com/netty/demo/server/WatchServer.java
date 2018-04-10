package com.netty.demo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netty.demo.constant.Constants;
import com.netty.demo.server.coder.WatchDecoder;
import com.netty.demo.server.coder.WatchEncode;
import com.netty.demo.server.connertor.impl.WatchConnertorImpl;
import com.netty.demo.server.exception.InitErrorException;
import com.netty.demo.server.proxy.MessageProxy;

/**
* @Project: netty-tcp-web 
* @Class WatchServer 
* @Description: 手表服务器 tcp
* @author cd 14163548@qq.com
* @date 2017年12月8日 上午10:41:22 
* @version V1.0
 */
public class WatchServer {

	private static Logger LOGGER = LoggerFactory.getLogger(WatchServer.class); 
	
	private WatchConnertorImpl connertor;
	private MessageProxy proxy;
	private int port;
	
	private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;
    
    public void init() throws Exception{
    	LOGGER.info("start watch server ...");
    	LOGGER.info("init :"+connertor +"--"+proxy+"--"+port);
    	ServerBootstrap bootstrap = new ServerBootstrap();
    	/**
    	 * 第一个经常被叫做‘boss’，用来接收进来的连接。
		 * 第二个经常被叫做‘worker’，用来处理已经被接收的连接，
    	 */
    	bootstrap.group(bossGroup, workerGroup);
    	/**
    	 * 这里告诉Channel如何获取新的连接.
    	 */
    	bootstrap.channel(NioServerSocketChannel.class);
    	bootstrap.childHandler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				//根据]结尾
                ChannelPipeline p = ch.pipeline();
                ByteBuf delimiter = Unpooled.copiedBuffer(Constants.ImserverConfig.DELIMITER.getBytes());
                //接收数据的长度
                p.addLast(new DelimiterBasedFrameDecoder(1048576,delimiter));
                //定义解码器 
                p.addLast("decoder", new WatchDecoder());
                //定义解码器
                p.addLast("encoder",new WatchEncode());
                //连接空闲时间
                p.addLast(new IdleStateHandler(Constants.ImserverConfig.READ_IDLE_TIME,Constants.ImserverConfig.WRITE_IDLE_TIME,0));
                //定义 处理消息handler
                p.addLast("handler",new WatchServerHandler(connertor, proxy));
			}
		});
    	
    	// 可选参数
    	bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        // 绑定接口，同步等待成功
        LOGGER.info("start watch server at port[" + port + "].");
        
        ChannelFuture future = bootstrap.bind(port).sync();
    	channel = future.channel();
    	//监听是否启动成功
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                	LOGGER.info("Server have success bind to " + port);
                } else {
                	LOGGER.error("Server fail bind to " + port);
                   throw new InitErrorException("Server start fail !", future.cause());
                }
            }
        });
    }
    
    /**
     * 释放资源
     */
    public void destroy() {
    	LOGGER.info("destroy watch server ...");
        // 释放线程池资源
        if (channel != null) {
			channel.close();
		}
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        LOGGER.info("destroy watch server complate.");
    }
    
	public void setPort(int port) {
		this.port = port;
	}

	public void setConnertor(WatchConnertorImpl connertor) {
		this.connertor = connertor;
	}

	public void setProxy(MessageProxy proxy) {
		this.proxy = proxy;
	}

	/*public static void main(String[] args) {
		WatchServer server = new WatchServer();
		try {
			server.setPort(8112);
			server.init();
		} catch (Exception e) {
			
			e.printStackTrace();
			server.destroy();
		}
	}*/
}
