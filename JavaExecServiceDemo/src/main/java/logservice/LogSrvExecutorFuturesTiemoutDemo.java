package logservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.Runtime;
import org.javatuples.Pair;

import cws.demo.utils.DemoUtils;

public class LogSrvExecutorFuturesTiemoutDemo {
	private static final LogService logger = LogService.getInstance();
	
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
	private static long shutdownTiemoutSecs = 30;
	
	private Boolean throwException = null;
	private long getTimeout = 20000;
	private String retValFormat = "tid-%d slept for: %dms";

	public static void main(String[] args) {
		final long start = System.currentTimeMillis();

		logger.log("Using " + NTHREADS + " threads");

		LogSrvExecutorFuturesTiemoutDemo executorFuturesTiemoutDemo = new LogSrvExecutorFuturesTiemoutDemo();
		executorFuturesTiemoutDemo.setupExecAndRun(args);
		DemoUtils.log("Total time: " + (System.currentTimeMillis() - start) + "ms");
		
		DemoUtils.doGracefullShutdown(executor, "Pool", shutdownTiemoutSecs);
		
		logger.stop();
		DemoUtils.log("Exiting");
	}

	/**
	 * Sets up and execute tasks, use CompletionService to block until all tasks
	 * have completed
	 */
	private void setupExecAndRun(String[] args) {
		getCommandLineParams(args);

		final List<Future<Pair<Long, Long>>> futures = new ArrayList<>();
		for (int thread = 0; thread < NTHREADS; thread++) {
			futures.add(executor.submit(new LogSrvConstantTimeIntervalsTask(throwException)));
		}

		for (Future<Pair<Long, Long>> future : futures) {
			handleThreadInterupts(future);
			logger.log("processing future");
		}
	}

	private void handleThreadInterupts(Future<Pair<Long, Long>> future) {
		try {
			final Pair<Long, Long> retVal = future.get(getTimeout, TimeUnit.MILLISECONDS);
			logger.log(String.format(retValFormat, retVal.getValue0(), retVal.getValue1()));

		} catch (TimeoutException e) {
			logger.log("Caught TimeoutException on future.get()");
		} catch (InterruptedException e) {
			logger.log("Caught InterruptedException on future.get()");
		} catch (ExecutionException e) {
			logger.log("Caught ExecutionException exception caused by " + e.getCause() + " - take appropiate action");
		} finally {
			future.cancel(true);
		}
	}

	private void getCommandLineParams(String[] args) {
		for(int i=0; i < args.length; i++) {
			if(i==0)
				getTimeout = Long.decode(args[0]);
			if(i==1) {
				throwException = Boolean.valueOf(args[1]);
				retValFormat = "tid-%d generated largest prime: %d";		
			}
			if(i==2) {
				shutdownTiemoutSecs = Long.decode(args[2]);
			}
		}
		
		logger.log("param getTimeout= " + getTimeout + "ms");
		logger.log("param throwException= " + throwException);
		if(shutdownTiemoutSecs < 0)
			logger.log("Task shutdown will not be invoked");
		else
			logger.log("param shutdown timeout " + shutdownTiemoutSecs + "s");
		
		if(throwException == null)
			logger.log("Running Sleep Task");
		else
			logger.log("Using Prime Generation task");
	}
}
