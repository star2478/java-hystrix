package com.pingan.test.springbootdemo;

import static org.junit.Assert.*;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.pingan.test.springbootdemo.hystrix.HelloWorldHystrixObservableCommand;

import rx.Observable;

/**
 * 
 * 以下四种情况将触发getFallback调用：
 * 1）run()方法抛出非HystrixBadRequestException异常
 * 2）run()方法调用超时
 * 3）熔断器开启拦截调用
 * 4）线程池/队列/信号量是否跑满
 */
public class HystrixFallback4TimeoutTest extends HystrixCommand<String> {

    private final String name;

    public HystrixFallback4TimeoutTest(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.name = name;
    }

    // 采用无限循环或sleep来模拟超时
    @Override
    protected String run() throws Exception {

//    	int j = 0;
//    	while (true) {
//    		j++;
//    	}
    	// 超时异常，就算使用try-catch捕获也会触发fallback
    	try {
    		TimeUnit.MILLISECONDS.sleep(1200);
    	} catch(Exception e) {	// note：就算此处被捕获也会触发fallback
    		e.printStackTrace();
    	}
    	System.out.println("after sleep");
		return name;
    }

    @Override
    protected String getFallback() {
        return "fallback: " + name;
    }

    public static class UnitTest {

//        @Test
        public void testSynchronous() {
        	try {
        		assertEquals("fallback: Hlx", new HystrixFallback4ExceptionTest("Hlx").execute());
        	} catch(Exception e) {
        		System.out.println("run()抛出HystrixBadRequestException时，会被捕获到这里");
        	}
        }
    }

}