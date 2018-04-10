package com.netty.demo.server.coder;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netty.demo.server.model.proto.WatchMessageBodyProto;
import com.netty.demo.utils.WatchMessageParsingUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
* @Project: netty-tcp-web 
* @Class WatchEncode 
* @Description: 手表编码器 把要发送给手表的数据Class 
* 	解析为 [3G*3919753126*0009*LK,0,1255,99] 这种格式的协议 发送给手表
* @author cd 14163548@qq.com
* @date 2017年12月8日 下午12:13:41 
* @version V1.0
 */
public class WatchEncode extends MessageToByteEncoder<Object>{

	 private final static Logger log = LoggerFactory.getLogger(WatchEncode.class);
	/**
	 * 把要发送的数据Class 解析为 手表客户端协议的数据 
	 * 列： [3G*3919753126*0009*LK,0,1255,99] 
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
			throws Exception {
		
		//解析消息数据发送给客户端
		if (null == msg) {
            //throw new Exception("WatchEncode encode message is null.");
			log.warn("WatchEncode encode message is null.");
        }else{
        	if (msg instanceof WatchMessageBodyProto.Model.Builder) {
            	WatchMessageBodyProto.Model.Builder message = (WatchMessageBodyProto.Model.Builder)msg;
            	log.info("message:"+message.toString());
            	if(StringUtils.isNotEmpty(message.getReceiver())){//判断是否有回复消息
            		message.setDeviceId(message.getReceiver());//回复人的设备ID
            	}
            	//封装数据格式为手表需要的协议格式
            	String data = WatchMessageParsingUtils.sendWatchData(message);
            	ByteBuf resp = Unpooled.copiedBuffer(data.getBytes());
            	ctx.writeAndFlush(resp);
            } else {
                log.warn("WatchEncode encode message is not proto.");
            }
        }
		
	}

}
