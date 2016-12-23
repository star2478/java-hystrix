package com.pingan.test.springbootdemo;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * construct()里发射1、2、3后，抛出异常跳到resumeWithFallback()，resumeWithFallback从4开始再多发射8个数字到onNext
 */
public class HystrixObservableCommand4FailsStubbedTest extends HystrixObservableCommand<Integer> {

    private int lastSeen;

    public HystrixObservableCommand4FailsStubbedTest(int lastSeen) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.lastSeen = lastSeen;
    }
    
    @Override
    protected Observable<Integer> construct() {
        return Observable.just(1, 2, 3)	//发射1、2、3
                .concatWith(Observable.<Integer> error(new RuntimeException("forced error")))	// 抛出异常
                .doOnNext(new Action1<Integer>() {
//                    @Override
                    public void call(Integer t1) {
                    	System.out.println("in doOnNext, t1="+t1);
                        lastSeen = t1;
                    }

                })
                .subscribeOn(Schedulers.computation());
    }
	
    @Override
    protected Observable<Integer> resumeWithFallback() {
    	System.out.println("lastSeen=" + lastSeen);
        if (lastSeen < 4) {
            return Observable.range(lastSeen + 1, 8);//4 - lastSeen);
        } else {
            return Observable.empty();
        }
    }

    public static class UnitTest {

//        @Test
        public void testSuccess() throws IOException {
        	Observable<Integer> hotObservable = new HystrixObservableCommand4FailsStubbedTest(0).observe();
        	try {
        		TimeUnit.MILLISECONDS.sleep(800);
        	} catch(Exception e) {
        		
        	}
        	System.out.println("-------------");
        	hotObservable.subscribe(new Observer<Integer>() {

    			// 先执行onNext再执行onCompleted
    			// @Override
    			public void onCompleted() {
    				System.out.println("hotObservable of ObservableCommand completed");
    			}

    			// @Override
    			public void onError(Throwable e) {
    				System.out.println("hotObservable of ObservableCommand error");
    				e.printStackTrace();
    			}

    			// @Override
    			public void onNext(Integer v) {
    				System.out.println("hotObservable of ObservableCommand onNext: " + v);
    			}

    		});
//    		hotObservable.subscribe(new Action1<Integer>() {
//        	
//				// 相当于上面的onNext()
//				// @Override
//				public void call(Integer v) {
//					System.out.println("hotObservable of ObservableCommand call: " + v);
//				}
//        	
//    		});
        	System.in.read();
        }

    }
}
