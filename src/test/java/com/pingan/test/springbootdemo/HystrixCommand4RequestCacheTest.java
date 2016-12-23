package com.pingan.test.springbootdemo;

import static org.junit.Assert.*;

import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;
import org.junit.Test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

/**
 * cache只有同在一个context中才生效
 * 通过HystrixRequestContext.initializeContext()初始化context，通过shutdown()关闭context
 */
public class HystrixCommand4RequestCacheTest extends HystrixCommand<Boolean> {

    private final int value;
    private final String value1;

    protected HystrixCommand4RequestCacheTest(int value, String value1) {
        super(HystrixCommandGroupKey.Factory.asKey("RequestCacheCommandGroup"));
        this.value = value;
        this.value1 = value1;
    }

    // 返回结果是cache的value
    @Override
    protected Boolean run() {
        return value == 0 || value % 2 == 0;
    }

    // 构建cache的key
    @Override
    protected String getCacheKey() {
        return String.valueOf(value) + value1;
    }

    public static class UnitTest {

//        @Test
        public void testWithoutCacheHits() {
            HystrixRequestContext context = HystrixRequestContext.initializeContext();
            try {
                assertTrue(new HystrixCommand4RequestCacheTest(2,"HLX").execute());
                assertFalse(new HystrixCommand4RequestCacheTest(1,"HLX").execute());
                assertTrue(new HystrixCommand4RequestCacheTest(0,"HLX").execute());
                assertTrue(new HystrixCommand4RequestCacheTest(58672,"HLX").execute());
            } finally {
                context.shutdown();
            }
        }

//        @Test
        public void testWithCacheHits() {
            HystrixRequestContext context = HystrixRequestContext.initializeContext();
            try {
                HystrixCommand4RequestCacheTest command2a = new HystrixCommand4RequestCacheTest(2,"HLX");
                HystrixCommand4RequestCacheTest command2b = new HystrixCommand4RequestCacheTest(2,"HLX");
                HystrixCommand4RequestCacheTest command2c = new HystrixCommand4RequestCacheTest(2,"HLX1");

                assertTrue(command2a.execute());
                // this is the first time we've executed this command with the value of "2" so it should not be from cache
                assertFalse(command2a.isResponseFromCache());

                assertTrue(command2b.execute());
                // this is the second time we've executed this command with the same value so it should return from cache
                assertTrue(command2b.isResponseFromCache());
                
                assertTrue(command2c.execute());
                assertFalse(command2c.isResponseFromCache());
            } finally {
                context.shutdown();
            }

            // start a new request context
            context = HystrixRequestContext.initializeContext();
            try {
                HystrixCommand4RequestCacheTest command3a = new HystrixCommand4RequestCacheTest(2,"HLX");
                HystrixCommand4RequestCacheTest command3b = new HystrixCommand4RequestCacheTest(2,"HLX");
                assertTrue(command3a.execute());
                // this is a new request context so this should not come from cache
                assertFalse(command3a.isResponseFromCache());

                // 没有command3b.execute()，command3b.isResponseFromCache()就一直为false
                assertFalse(command3b.isResponseFromCache());
            } finally {
                context.shutdown();
            }
        }
    }

}
