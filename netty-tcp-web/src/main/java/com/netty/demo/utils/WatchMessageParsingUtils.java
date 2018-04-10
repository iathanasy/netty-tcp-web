package com.netty.demo.utils;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import com.netty.demo.server.model.proto.WatchMessageBodyProto;
import com.netty.demo.server.model.proto.WatchMessageBodyProto.Model;

/**
* @Project: netty-tcp-web 
* @Class WatchMessageParsingUtils 
* @Description: 手表消息解析工具类
* @author cd 14163548@qq.com
* @date 2017年12月8日 下午3:26:27 
* @version V1.0
 */
public class WatchMessageParsingUtils {

	/**
	 * 拆分字符
	 * @param str 要拆分的字符串
	 * @param symbol 根据什么拆分
	 * @return 拆分后的数组
	 */
	public static String[] Split(String str,String symbol){
		if(symbol.equals("+") || // +、*、|、/等符号在正则表达示中有相应的不同意义。
		   symbol.equals("*") ||
		   symbol.equals("|") ||
		   symbol.equals("/")){
			symbol = "["+symbol+"]";
		}
		return str.split(symbol);
	}
	
	/**
	 * 
	 * @param str
	 * @param symbol
	 * @return
	 */
	public static String firstSplit(String str,String symbol){
		int len = str.indexOf(symbol);
		if(len == -1){
			return "";
		}
		return str.substring(len+1);
	}
	
	/**
	 * 截取字符串 去掉前一个和后一个字符 []
	 * @param str 要截取的字符串
	 * @return 截取后的字符
	 */
	public static String Intercept(String str) {
		String s = str.substring(1, str.length()-1);
		return s;
	}
	
	
	/**
	 * 内容长度转换
	 * @param n
	 * @return 返回转换后的内容长度
	 */
	//10进制转16进制  
    public static String IntToHex(int n){
    	//转换为 16进制
    	String str = Integer.toHexString(n);
    	
    	while(str.length() < 4){
    		str = "0"+str;
    	}
    	
    	return str.toString().toUpperCase();
    }  

    
    /**
     * 解析数据 到实体类 接受数据
     * @param data  [3G*3919753126*0009*LK,0,1255,99]
     * @return 
     */
	public static WatchMessageBodyProto.Model.Builder receiveAnalyticData(String data){
		WatchMessageBodyProto.Model.Builder msg = WatchMessageBodyProto.Model.newBuilder();
		//先去掉[]
		String str = Intercept(data);
		//拆分字符串 根据, 拆分 [3G*3919753126*0009*LK,0,1255,99]
		String[] len = Split(str, ",");
		//拆分  根据* 拆分 3G*3918172553*0009*LK
		String[] info = Split(len[0], "*");
		msg.setFactory(info[0]);//厂商
		msg.setDeviceId(info[1]);//设备id
		msg.setLength(info[2]);//长度
		msg.setType(info[3]);//类型
		msg.setBody(firstSplit(str, ","));//内容
		return msg;
	}
	
    
	/**
     * 解析数据 到实体类 发送数据
     * @param data  [3G*3919753126*0009*LK,0,1255,99]
     * @return 
     */
	public static WatchMessageBodyProto.Model.Builder sendAnalyticData(String data){
		WatchMessageBodyProto.Model.Builder msg = WatchMessageBodyProto.Model.newBuilder();
		//先去掉[]
		String str = Intercept(data);
		//拆分字符串 根据, 拆分 [3G*3919753126*0009*LK,0,1255,99]
		String[] len = Split(str, ",");
		//拆分  根据* 拆分 3G*3918172553*0009*LK
		String[] info = Split(len[0], "*");
		msg.setFactory(info[0]);//厂商
		msg.setDeviceId(info[1]);//设备id
		msg.setType(info[3]);//类型
		msg.setBody(firstSplit(str, ","));//内容
		String length = "";
		if(StringUtils.isNotBlank(msg.getBody())){
			//为空
			length = msg.getType() + "," +msg.getBody();
		}
		length = msg.getType();
		msg.setLength(IntToHex(length.length()));//长度计算
		return msg;
	}
	
	/**
	 * 针对所有发送给手表客户端的数据
	 * 解析实体类的数据 返回手表需要的协议
	 * @param message 实体类数据
	 * @return 手表数据  [3G*3919753126*0009*LK,0,1255,99]
	 */
	public static String sendWatchData(WatchMessageBodyProto.Model.Builder message){
		if(!StringUtils.isNotBlank(message.getDeviceId())){//为空就返回
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		//拆分字符串 根据, 拆分 [3G*3919753126*0009*LK,0,1255,99]
		buffer.append("[");
		buffer.append((!StringUtils.isNotBlank(message.getFactory())) ? "3G*" : message.getFactory() +"*");//厂商
		buffer.append(message.getDeviceId()+"*");//设备id
		String length = "";
		length = message.getType();
		message.setLength(IntToHex(length.length()));//长度计算
		buffer.append(message.getLength()+"*");
		buffer.append(message.getType());//类型
		if(StringUtils.isNotBlank(message.getBody())){//判断是否为空
			buffer.append(","+message.getBody());//内容
		}
		buffer.append("]");
		return buffer.toString();
		
	}
	
	/**
     * 自动生成实体类模板
     * @param body 0,1255,99
     * @return  [3G*3919753126*0009*LK,0,1255,99]
     */
	public static WatchMessageBodyProto.Model.Builder generateTemplate(String deviceId,String type,String body){
		WatchMessageBodyProto.Model.Builder msg = WatchMessageBodyProto.Model.newBuilder();
		msg.setFactory("3G");//厂商
		msg.setDeviceId(deviceId);//设备id
		msg.setType(type);//类型
		msg.setBody(firstSplit(body, ","));//内容
		String length = "";
		if(StringUtils.isNotBlank(msg.getBody())){
			//为空
			length = msg.getType() + "," +msg.getBody();
		}
		length = msg.getType();
		msg.setLength(IntToHex(length.length()));//长度计算
		msg.setTimeStamp(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
		return msg;
	}
	
	/**
	 * 过滤数据
	 * @param data  dasd [3G*3919753126*0009*LK,0,1255,99]eqweq
	 * @return [3G*3919753126*0009*LK,0,1255,99]
	 */
	public static String filterData(String data){
		int first = data.indexOf("[");
		int last =  data.indexOf("]");
		if(first == -1 || last == -1){
			return "";
		}
		String str = data.substring(first,(last+1));
		return str;
	}
	
	
	public static void main(String[] args) {
		String data = "[3G*3919753126*0009*LK,0,1255,99]";
		System.out.println(receiveAnalyticData(data.trim()).toString());
		
		String data1 = "[3G*3919753126*zz*LK]";
		System.out.println(sendAnalyticData(data1.trim()).toString());
	}
}
