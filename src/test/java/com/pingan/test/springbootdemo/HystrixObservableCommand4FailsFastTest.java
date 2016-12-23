package com.pingan.test.springbootdemo;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;

/**
 * 当construct()发生异常超时，会跳到resumeWithFallback()，可选择再抛异常给onError或触发onNext/onComplete
 */
public class HystrixObservableCommand4FailsFastTest extends HystrixObservableCommand<String> {

    private final boolean throwException;

    public HystrixObservableCommand4FailsFastTest(boolean throwException) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.throwException = throwException;
    }
    
	@Override
    protected Observable<String> construct() {
		// 模拟超时
		try {
    		TimeUnit.MILLISECONDS.sleep(1200);
    	} catch(Exception e) {	// note：就算此处被捕获也会触发fallback
    		System.out.println("------------");
    		e.printStackTrace();
    	}
    	System.out.println("after sleep");
    	
    	// 模拟抛异常
//		if (throwException) {
//            throw new RuntimeException("failure from HystrixCommand4FailsFastTest");
//        }
		
    	System.out.println("in construct! thread:" + Thread.currentThread().getName());
        return Observable.create(new Observable.OnSubscribe<String>() {
//            @Override
            public void call(Subscriber<? super String> observer) {
                try {
                	System.out.println("in call of construct! thread:" + Thread.currentThread().getName());
                    if (!observer.isUnsubscribed()) {
                        observer.onNext("Hello1" + " thread:" + Thread.currentThread().getName());
                        observer.onNext("Hello2" + " thread:" + Thread.currentThread().getName());
                        observer.onCompleted();	// 不会往下执行observer的任何方法
                    }
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
         });
    }
	
	@Override
    protected Observable<String> resumeWithFallback() {
        if (throwException) {
    		System.out.println("here is resumeWithFallback error");
            return Observable.error(new Throwable("failure from resumeWithFallback"));	// 重新抛一个异常给onError
        } else {
    		System.out.println("here is resumeWithFallback just success");
            return Observable.just("success");	// 触发执行onNext以及onCompleted
//            return Observable.empty();	// fail silently
        }
    }

    public static class UnitTest {

//        @Test
        public void testSuccess() {
        	Observable<String> hotObservable = new HystrixObservableCommand4FailsFastTest(false).observe();
        	hotObservable.subscribe(new Observer<String>() {

    			// 先执行onNext再执行onCompleted
    			// @Override
    			public void onCompleted() {
    				System.out.println("hotObservable of ObservableCommand completed");
    			}

    			// @Override
    			public void onError(Throwable e) {
    				System.out.println("hotObservable of ObservableCommand error");
    			}

    			// @Override
    			public void onNext(String v) {
    				System.out.println("hotObservable of ObservableCommand onNext: " + v);
    			}

    		});
        }

//        @Test
        public void testFailure() {
            try {
            	Observable<String> hotObservable = new HystrixObservableCommand4FailsFastTest(true).observe();
            	hotObservable.subscribe(new Observer<String>() {

        			// 先执行onNext再执行onCompleted
        			// @Override
        			public void onCompleted() {
        				System.out.println("hotObservable of ObservableCommand completed");
        			}

        			// @Override
        			public void onError(Throwable e) {
        				System.out.println("hotObservable of ObservableCommand error");
        			}

        			// @Override
        			public void onNext(String v) {
        				System.out.println("hotObservable of ObservableCommand onNext: " + v);
        			}

        		});
            } catch (HystrixRuntimeException e) {
            	System.out.println("here is catch HystrixRuntimeException");
//            	System.out.println(e.getCause().getMessage());
//                assertEquals("failure from HystrixCommand4FailsFastTest:", e.getCause().getMessage());
//                e.printStackTrace();
            }
        }
    }
}
