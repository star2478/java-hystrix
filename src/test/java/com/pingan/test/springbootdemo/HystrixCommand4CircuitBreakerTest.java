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
 * CircuitBreakerRequestVolumeThreshold设置为3，意味着10s内请求超过3次就触发熔断器
 * run()中无限循环使命令超时进入fallback，执行3次run后，将被熔断，进入降级，即不进入run()而直接进入fallback
 * 如果未熔断，但是threadpool被打满，仍然会降级，即不进入run()而直接进入fallback
 */
public class HystrixCommand4CircuitBreakerTest extends HystrixCommand<String> {

    private final String name;

    public HystrixCommand4CircuitBreakerTest(String name) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("CircuitBreakerTestGroup"))  
                .andCommandKey(HystrixCommandKey.Factory.asKey("CircuitBreakerTestKey"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("CircuitBreakerTest"))
                .andThreadPoolPropertiesDefaults(	// 配置线程池
                		HystrixThreadPoolProperties.Setter()
                		.withCoreSize(200)	// 配置线程池里的线程数，设置足够多线程，以防未熔断却打满threadpool
                )
                .andCommandPropertiesDefaults(	// 配置熔断器
                		HystrixCommandProperties.Setter()
                		.withCircuitBreakerEnabled(true)
                		.withCircuitBreakerRequestVolumeThreshold(3)
                		.withCircuitBreakerErrorThresholdPercentage(80)
//                		.withCircuitBreakerForceOpen(true)	// 置为true时，所有请求都将被拒绝，直接到fallback
//                		.withCircuitBreakerForceClosed(true)	// 置为true时，将忽略错误
//                		.withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)	// 信号量隔离
//                		.withExecutionTimeoutInMilliseconds(5000)
                )
        );
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
    	System.out.println("running run():" + name);
    	int num = Integer.valueOf(name);
    	if(num % 2 == 0 && num < 10) {	// 直接返回
    		return name;
    	} else {	// 无限循环模拟超时
    		int j = 0;
        	while (true) {
        		j++;
        	}
    	}
//		return name;
    }

    @Override
    protected String getFallback() {
        return "CircuitBreaker fallback: " + name;
    }

    public static class UnitTest {

//        @Test
        public void testSynchronous() throws IOException {
        	for(int i = 0; i < 50; i++) {
	        	try {
	        		System.out.println("===========" + new HystrixCommand4CircuitBreakerTest(String.valueOf(i)).execute());
//	        		try {
//	            		TimeUnit.MILLISECONDS.sleep(1000);
//	            	}catch(Exception e) {}
//	        		Future<String> future = new HystrixCommand4CircuitBreakerTest("Hlx"+i).queue();
//	        		System.out.println("===========" + future);
	        	} catch(Exception e) {
	        		System.out.println("run()抛出HystrixBadRequestException时，被捕获到这里" + e.getCause());
	        	}
        	}

        	System.out.println("------开始打印现有线程---------");
        	Map<Thread, StackTraceElement[]> map=Thread.getAllStackTraces();
        	for (Thread thread : map.keySet()) {
				System.out.println(thread.getName());
			}
        	System.out.println("thread num: " + map.size());
        	
        	System.in.read();
        }
    }

}