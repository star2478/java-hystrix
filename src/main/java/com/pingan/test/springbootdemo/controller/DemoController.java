package com.pingan.test.springbootdemo.controller;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import com.pingan.test.springbootdemo.hystrix.HelloWorldHystrixCommand;
import com.pingan.test.springbootdemo.hystrix.HelloWorldHystrixObservableCommand;

import rx.Observable;
import rx.Observer;

@Controller
@EnableAutoConfiguration
public class DemoController implements EmbeddedServletContainerCustomizer {

//	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {  
//        return builder.sources(MainController.class);  
//    }

    public void customize(ConfigurableEmbeddedServletContainer container) {  
        container.setPort(8888);
    }  

    @RequestMapping("/demo")
    @ResponseBody
    public String demo() {
    	try {
    		HelloWorldHystrixObservableCommand helloWorldHystrixCommand = new HelloWorldHystrixObservableCommand("Hlx");
    		Observable<String> coldObservable = helloWorldHystrixCommand.observe();
    		Observable<String> coldObservable1 = helloWorldHystrixCommand.observe();

    		// single()是堵塞的
    		System.out.println("coldObservable single结果：" + coldObservable.toBlocking().single());
    		System.out.println("coldObservable single结果：" + coldObservable1.toBlocking().single());
    		// 注册观察者事件
    		// subscribe()是非堵塞的
    		// - this is a verbose anonymous inner-class approach and doesn't do assertions
//    		coldObservable.subscribe(new Observer<String>() {
//
//    			// 先执行onNext再执行onCompleted
//    			// @Override
//    			public void onCompleted() {
//    				System.out.println("coldObservable completed");
//    			}
//
//    			// @Override
//    			public void onError(Throwable e) {
//    				System.out.println("coldObservable error");
//    				e.printStackTrace();
//    			}
//
//    			// @Override
//    			public void onNext(String v) {
//    				System.out.println("coldObservable onNext: " + v);
//    			}
//
//    		});
//    		
//    		TimeUnit.MILLISECONDS.sleep(2000);
//    		System.out.println("3333333");
    	} catch(Exception e) {
    		System.out.println("here is exception");
    		e.printStackTrace();
    	}
        return "i am a demo";
    }
}