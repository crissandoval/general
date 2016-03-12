package logservice;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import cws.demo.utils.DemoUtils;

public class LogService implements Callable<Boolean> {
	private static final int QSIZE = 10000;
	private static final int STARTING = -1;
	private static final int RUNNING = 0;
	private static final int STOPPING = 1;
//	private static final BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(QSIZE);
//	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
//	private static final LogService logService = new LogService();
	private static BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(QSIZE);
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	private static LogService logService = new LogService();
	
	private static AtomicInteger isState = new AtomicInteger(-1);
	private Future<Boolean> future;


	private LogService() {};

	public static LogService getInstance() {
		return logService;
	}

	public void log(String s) {
		if (isState.compareAndSet(STARTING, RUNNING)) {
			future = executor.submit(new LogService());
		}
		
		final String print = Thread.currentThread().getId() == 1 ? "main: " + s
				: "tid-" + Thread.currentThread().getId() + ": " + s;

		if (isState.get() == RUNNING) {
			try {
				blockingQueue.put(print);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}

	public void stop() {
		if (isState.compareAndSet(RUNNING, STOPPING)) {
//			DemoUtils.log("future.cancel()");
			future.cancel(true);
			DemoUtils.doGracefullShutdown(executor, "LogService", 30);
		}
	}

	private void write(final String s) {
		System.out.println(System.currentTimeMillis() + "  " + s);
	}

	public Boolean call() {
		try {
			while (isState.get() == RUNNING) {
				write(blockingQueue.take());

				if (Thread.currentThread().isInterrupted()) {
//					DemoUtils.log("Logger detected isInterrupted");
					return true;
				}
			}
//			DemoUtils.log("isState.get() == " + isState.get());
			return true;
		} catch (InterruptedException e) {
//			DemoUtils.log("Logger caught InterruptedException");
			return false;
		} finally {
//			DemoUtils.log("finally - Logger queue size " + blockingQueue.size());
			String s;
			while ((s = blockingQueue.poll()) != null)
				write("finally - DRAINING " + s);
		}
	}
}
