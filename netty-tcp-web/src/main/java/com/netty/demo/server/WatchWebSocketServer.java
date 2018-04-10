package com.netty.demo.server;

import static io.netty.buffer.Unpooled.wrappedBuffer;

import java.util.List;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import com.netty.demo.constant.Constants;
import com.netty.demo.server.connertor.impl.WatchConnertorImpl;
import com.netty.demo.server.model.proto.WatchMessageBodyProto;
import com.netty.demo.server.proxy.MessageProxy;

/**
* @Project: netty-tcp-web 
* @Class WatchWebSocketServer 
* @Description: websocket服务器
* @author cd 14163548@qq.com
* @date 2017年12月12日 上午9:31:10 
* @version V1.0
 */
public class WatchWebSocketServer {

	private static Logger LOGGER = LoggerFactory.getLogger(WatchWebSocketServer.class); 
	
	/**
	 * 解码器
	 */
	private ProtobufDecoder decoder = new ProtobufDecoder(WatchMessageBodyProto.Model.getDefaultInstance());

	private WatchConnertorImpl connertor;
	private MessageProxy proxy;
	private int port;
	
	private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;
    
    /**
     * 启动websocket server
     * @throws Exception
     */
    public void init() throws Exception{
    	LOGGER.info("start watch websocket server ...");
    	LOGGER.info("init :"+connertor +"--"+proxy+"--"+port);
    	ServerBootstrap bootstrap = new ServerBootstrap();
    	bootstrap.group(bossGroup, workerGroup);
    	bootstrap.channel(NioServerSocketChannel.class);
    	bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				 // HTTP请求的解码和编码
	            pipeline.addLast(new HttpServerCodec());
	            // 把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse，
	            // 原因是HTTP解码器会在每个HTTP消息中生成多个消息对象HttpRequest/HttpResponse,HttpContent,LastHttpContent
	            pipeline.addLast(new HttpObjectAggregator(Constants.ImserverConfig.MAX_AGGREGATED_CONTENT_LENGTH));
	            // 主要用于处理大数据流，比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的; 增加之后就不用考虑这个问题了
	            pipeline.addLast(new ChunkedWriteHandler());
	            // WebSocket数据压缩
	            pipeline.addLast(new WebSocketServerCompressionHandler());
	            // 协议包长度限制
	            pipeline.addLast(new WebSocketServerProtocolHandler("/websocket", null, true, Constants.ImserverConfig.MAX_FRAME_LENGTH));
	            // 协议包解码
	            pipeline.addLast(new MessageToMessageDecoder<WebSocketFrame>() {
	                @Override
	                protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> objs) throws Exception {
	                	ByteBuf buf = ((BinaryWebSocketFrame) frame).content();
	                    objs.add(buf);
	                    buf.retain();
	                }
	            });
	            // 协议包编码
	            pipeline.addLast(new MessageToMessageEncoder<MessageLiteOrBuilder>() {
	                @Override
	                protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) throws Exception {
	                    ByteBuf result = null;
	                    if (msg instanceof MessageLite) {
	                    	LOGGER.info("MessageLite---------->"+msg);
	                        result = wrappedBuffer(((MessageLite) msg).toByteArray());
	                    }
	                    if (msg instanceof MessageLite.Builder) {
	                    	LOGGER.info("MessageLite.Builder---------->"+msg);
	                        result = wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray());
	                    }
	                    // 然后下面再转成websocket二进制流，因为客户端不能直接解析protobuf编码生成的
	                    WebSocketFrame frame = new BinaryWebSocketFrame(result);
	                    out.add(frame);
	                }
	            });
	            // 协议包解码时指定Protobuf字节数实例化为CommonProtocol类型
	            pipeline.addLast(decoder);
	            pipeline.addLast(new IdleStateHandler(Constants.ImserverConfig.READ_IDLE_TIME,Constants.ImserverConfig.WRITE_IDLE_TIME,0));
	            // 业务处理器
	            pipeline.addLast(new WatchWebSocketServerHandler(connertor, proxy));
			}
		});
    	
    	// 可选参数
    	bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        // 绑定接口，同步等待成功
        LOGGER.info("start watch websocketserver at port[" + port + "].");
        ChannelFuture future = bootstrap.bind(port).sync();
    	channel = future.channel();
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                	LOGGER.info("websocketserver have success bind to " + port);
                } else {
                	LOGGER.error("websocketserver fail bind to " + port);
                }
            }
        });
    }
    
    /**
     * 释放资源
     */
    public void destroy() {
    	LOGGER.info("destroy watch websocketserver ...");
        // 释放线程池资源
        if (channel != null) {
			channel.close();
		}
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        LOGGER.info("destroy watch webscoketserver complate.");
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
}
