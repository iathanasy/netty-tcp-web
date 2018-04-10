package com.netty.demo.server.enums;

public enum MessageEnums {

	//平台发送指令
	//关机指令
	POWEROFF,
	//重启
	RESET,
	//版本查询
	VERNO,
	//取下手环报警开关
	REMOVE,
	//计步功能开关
	PEDO,
	//设置计步功能打开的时间段范围
	WALKTIME,
	//翻转检测时间段设置
	SLEEPTIME,
	//免打扰时间段设置
	SILENCETIME,
	//找手表指令
	FIND,
	//小红花个数设置指令
	FLOWER,
	//闹钟设置指令
	REMIND,
	//情景模式 
	PROFILE,
	//短语显示设置指令
	MESSAGE,
	//低电短信报警开关
	LOWBAT,
	//SOS短信报警开关
	SOSSMS,
	//设置语言和时区
	LZ,
	//恢复出厂设置
	FACTORY,
	//IP端口设置
	IP,
	//SOS号码设置
	SOS1,
	SOS2,
	SOS3,
	//3个SOS号码同时设置
	SOS,
	//监听 终端收到该指令后会自动回拨给中心号码
	MONITOR,
	// 设置电话本
	PHB,
	PHB2,
	//带头像的电话簿
	PHBX,
	//删除电话本
	DPHBX,
	//拨打电话
	CALL,
	//控制密码设置 设置终端短信控制密码,非中心号码发送短信指令给终端需添加此密码
	PW,
	//中心号码设置
	CENTER,
	//数据上传间隔设置
	UPLOAD,
	//定位
	CR,
	//终端发送指令  平台回复的
	//链路保持
	LK,
	//报警数据上报
	AL,
	//对讲功能
	TK,
	//离线消息
	TKQ,
	//终端发送指令平台不用回复的
	//位置数据上报
	UD,
	UD2,
	//心率测试
	HRTSTART,
	//跌倒提醒开关
	FALLDOWN,
	//血压上传
	BPXY,
	
	
	/***
	 * 自定义消息类型
	 */
	/*关闭*/
	CLOSE,
	/*通知*/
	NOTIFY,
	/*广播*/
	BROADCAST,
	/*回复*/
	REPLY,
	/*发送*/
	SEND,
	/*组*/
	GROUP
	;
	
}
