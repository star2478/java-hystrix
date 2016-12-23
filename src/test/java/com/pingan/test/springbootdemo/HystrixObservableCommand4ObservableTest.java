package com.pingan.test.springbootdemo;

import static org.junit.Assert.*;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.pingan.test.springbootdemo.hystrix.HelloWorldHystrixCommand;
import com.pingan.test.springbootdemo.hystrix.HelloWorldHystrixObservableCommand;


/**
 * 
 * HystrixObservableCommand vs HystrixCommand：
 * 1）前者的命令封装在contruct()，后者在run()；前者的fallback处理封装在resumeWithFallback()，后者在getFallBack()
 * 2）前者用主线程执行contruct()，后者另起线程来执行run()
 * 3）前者可以在contruct()中顺序定义多个onNext，当调用subscribe()注册成功后将依次执行这些onNext，后者只能在run()中返回一个值（即一个onNext）
 * 
 * HystrixObservableCommand的observe()与toObservable()的区别：
 * 1）observe()会立即执行HelloWorldHystrixObservableCommand.construct()；toObservable()要在toBlocking().single()或subscribe()时才执行HelloWorldHystrixObservableCommand.construct()
 * 2）observe()中，toBlocking().single()和subscribe()可以共存；在toObservable()中不行，因为两者都会触发执行HelloWorldHystrixObservableCommand.construct()，这违反了同一个HelloWorldHystrixObservableCommand对象只能执行construct()一次原则
 * @throws Exception
 */
public class HystrixObservableCommand4ObservableTest {
	
//	@Test
	public void testObservable() throws Exception {

		// observe()是异步非堵塞性执行
//		System.out.println("11111111");
		Observable<String> hotObservable = new HelloWorldHystrixObservableCommand("Hlx").observe();
//		System.out.println("1.555555");
//		TimeUnit.MILLISECONDS.sleep(2000);
//		System.out.println("22222222");
//		String string = hotObservable.toBlocking().single();
//		// single()是堵塞的
//		System.out.println(string);

		// 注册观察者事件
		// subscribe()是非堵塞的
		hotObservable.subscribe(new Observer<String>() {

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
			public void onNext(String v) {
				System.out.println("hotObservable of ObservableCommand onNext: " + v);
			}

		});
		
		// 非堵塞
		// - also verbose anonymous inner-class
		// - ignore errors and onCompleted signal
//		hotObservable.subscribe(new Action1<String>() {
//
//			// 相当于上面的onNext()
//			// @Override
//			public void call(String v) {
//				System.out.println("hotObservable call: " + v);
//			}
//
//		});

		// 主线程不直接退出，在此一直等待其他线程执行
		System.in.read();

	}
	
//	@Test
	public void testToObservable() throws Exception {

		// toObservable()是异步非堵塞性执行
//		System.out.println("11111111");
		Observable<String> coldObservable = new HelloWorldHystrixObservableCommand("Hlx").toObservable();
//		TimeUnit.MILLISECONDS.sleep(2000);
//		System.out.println("22222222");

		
		// 注册观察者事件
		// subscribe()是非堵塞的
		// - this is a verbose anonymous inner-class approach and doesn't do assertions
		coldObservable.subscribe(new Observer<String>() {

			// 先执行onNext再执行onCompleted
			// @Override
			public void onCompleted() {
				System.out.println("coldObservable of ObservableCommand completed");
			}

			// @Override
			public void onError(Throwable e) {
				System.out.println("coldObservable of ObservableCommand error");
				e.printStackTrace();
			}

			// @Override
			public void onNext(String v) {
				System.out.println("coldObservable of ObservableCommand onNext: " + v);
			}

		});

		// 主线程不直接退出，在此一直等待其他线程执行
		System.in.read();

	}

}
