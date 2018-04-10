package com.netty.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StartServer {
	public static void main(String[] args) {
		 String [] str = {"spring-config.xml"};
		 ApplicationContext context = new  ClassPathXmlApplicationContext(str);
		
	}
}
