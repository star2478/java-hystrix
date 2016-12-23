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
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixRequestLog;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.exception.HystrixRuntimeException.FailureType;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import com.pingan.test.springbootdemo.hystrix.HelloWorldHystrixObservableCommand;

import rx.Observable;

/**
 * 
 * 设置线程池里的线程数＝13，然后循环>13次和<13次，最后查看当前所有线程名称
 * 
 */
public class HystrixCommand4ThreadPoolTest extends HystrixCommand<String> {

    private final String name;

    public HystrixCommand4ThreadPoolTest(String name) {
//        super(HystrixCommandGroupKey.Factory.asKey("ThreadPoolTestGroup"));
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ThreadPoolTestGroup"))  
                .andCommandKey(HystrixCommandKey.Factory.asKey("testCommandKey"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("ThreadPoolTest"))
                .andThreadPoolPropertiesDefaults(
                		HystrixThreadPoolProperties.Setter()
                		.withCoreSize(13)	// 配置线程池里的线程数
                )
        );
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
    	/*---------------会触发fallback的case-------------------*/
//    	int j = 0;
//    	while (true) {
//    		j++;
////    		return "a";
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
//        throw new HystrixBadRequestException("HystrixBadRequestException is never trigger fallback");
        
		return name;
    }

    @Override
    protected String getFallback() {
        return "fallback: " + name;
    }

    public static class UnitTest {

//        @Test
        public void testSynchronous() throws IOException {
        	for(int i = 0; i < 50; i++) {
	        	try {
//	        		assertEquals("fallback: Hlx", new HystrixCommand4ThreadPoolTest("Hlx").execute());
	        		System.out.println("===========" + new HystrixCommand4ThreadPoolTest("Hlx").execute());
//	        		Future<String> future = new HystrixCommand4ThreadPoolTest("Hlx"+i).queue();
//	        		System.out.println("===========" + future);
	        	} catch(Exception e) {
	        		System.out.println("run()抛出HystrixBadRequestException时，被捕获到这里" + e.getCause());
	        	}
        	}
        	for(int i = 222; i < 272; i++) {
	        	try {
	//        		assertEquals("fallback: Hlx", new HystrixCommand4ThreadPoolTest("Hlx").execute());
	        		System.out.println("1===========" + new HystrixCommand4ThreadPoolTest("Hlx").execute());
//	        		Future<String> future = new HystrixCommand4ThreadPoolTest("Hlx1"+i).queue();
//	        		System.out.println("===========" + future);
	        	} catch(Exception e) {
	        		System.out.println("run()抛出HystrixBadRequestException时，被捕获到这里" + e.getCause());
	        	}
        	}
        	try {
        		TimeUnit.MILLISECONDS.sleep(2000);
        	}catch(Exception e) {}
        	System.out.println("------开始打印现有线程---------");
        	Map<Thread, StackTraceElement[]> map=Thread.getAllStackTraces();
        	for (Thread thread : map.keySet()) {
				System.out.println(thread.getName());
			}
        	System.out.println(map);
        	System.out.println("thread num: " + map.size());
//        	int numExecuted = HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().size();
//            System.out.println("num executed: " + numExecuted);
        	System.in.read();
        }
    }

}