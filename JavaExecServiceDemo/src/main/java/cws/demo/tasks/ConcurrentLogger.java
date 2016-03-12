package cws.demo.tasks;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConcurrentLogger implements Runnable {
	private static final int QSIZE = 100;
	private static final BlockingQueue<String> queue = new LinkedBlockingQueue<String>(QSIZE);
	
	public static void log(String s) {
		queue.add(s);
	}
	
	public void run() {
		try{
		while(true) {
			cws.demo.utils.DemoUtils.log(queue.take());
		}
		}catch(InterruptedException e) {
			cws.demo.utils.DemoUtils.log("Concurrent caught InterruptedException");
		}
	}
}
