package com.netty.demo.server.coder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.netty.demo.constant.Constants;
import com.netty.demo.server.WatchServerHandler;
import com.netty.demo.server.model.proto.WatchMessageBodyProto;
import com.netty.demo.server.model.proto.WatchMessageBodyProto.Model.Builder;
import com.netty.demo.server.model.proto.WatchMessageProto;
import com.netty.demo.utils.WatchMessageParsingUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
* @Project: netty-tcp-web 
* @Class WatchDecoder 
* @Description: 手表解码器 把收到手表的数据解码封装Class
* @author cd 14163548@qq.com
* @date 2017年12月8日 下午12:11:34 
* @version V1.0
 */
public class WatchDecoder extends ByteToMessageDecoder{
	 private final static Logger log = LoggerFactory.getLogger(WatchDecoder.class);
	/**
	 * 把消息解码封装Class
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		//没有数据
		if (in.readableBytes() < 4) {
            return;
        }
		//转换为String
		byte[] byteArray = new byte[in.capacity()];
		in.readBytes(byteArray);
		String message = new String(byteArray);
		//补全分割的数据
    	String data = (String)message+Constants.ImserverConfig.DELIMITER;
        //过滤垃圾数据
        data = WatchMessageParsingUtils.filterData(data.trim());
        log.info("message:"+data);
        //解析消息数据为实体
        WatchMessageBodyProto.Model.Builder receiveAnalyticData = WatchMessageParsingUtils.receiveAnalyticData(data);
        //发送给下一个handler
        ctx.fireChannelRead(receiveAnalyticData);
	}

}
