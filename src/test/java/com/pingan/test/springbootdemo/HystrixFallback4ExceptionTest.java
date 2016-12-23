package com.pingan.test.springbootdemo;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixRequestLog;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.exception.HystrixRuntimeException.FailureType;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import com.pingan.test.springbootdemo.hystrix.HelloWorldHystrixObservableCommand;

import rx.Observable;

/**
 * 
 * 以下四种情况将触发getFallback调用：
 * 1）run()方法抛出非HystrixBadRequestException异常
 * 2）run()方法调用超时
 * 3）熔断器开启拦截调用
 * 4）线程池/队列/信号量是否跑满
 * 
 * 实现getFallback()后，执行命令时遇到以上4种情况将被fallback接管，不会抛出异常或其他
 */
public class HystrixFallback4ExceptionTest extends HystrixCommand<String> {

    private final String name;

    public HystrixFallback4ExceptionTest(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("FallbackGroup"));
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
    	
    	/*---------------会触发fallback的case-------------------*/
    	// 无限循环，实际上属于超时
//    	int j = 0;
//    	while (true) {
//    		j++;
//    	}
    	
    	// 除零异常
//    	int i = 1/0;
    	
    	// 主动抛出异常
//        throw new HystrixTimeoutException();
//        throw new RuntimeException("this command will trigger fallback");
//        throw new Exception("this command will trigger fallback");
//    	throw new HystrixRuntimeException(FailureType.BAD_REQUEST_EXCEPTION, commandClass, message, cause, fallbackException);
        
    	/*---------------不会触发fallback的case-------------------*/
    	// 被捕获的异常不会触发fallback
//    	try {
//    		throw new RuntimeException("this command never trigger fallback");
//    	} catch(Exception e) {
//    		e.printStackTrace();
//    	}
        
    	// HystrixBadRequestException异常由非法参数或非系统错误引起，不会触发fallback，也不会被计入熔断器
        throw new HystrixBadRequestException("HystrixBadRequestException is never trigger fallback");
        
//		return name;
    }

    @Override
    protected String getFallback() {
        return "fallback: " + name;
    }

    public static class UnitTest {

//        @Test
        public void testSynchronous() throws IOException {
        	try {
	        		assertEquals("fallback: Hlx", new HystrixFallback4ExceptionTest("Hlx").execute());
	        } catch(Exception e) {
	        		System.out.println("run()抛出HystrixBadRequestException时，会被捕获到这里" + e.getCause());
	        }
//        	System.in.read();
        }
    }

}