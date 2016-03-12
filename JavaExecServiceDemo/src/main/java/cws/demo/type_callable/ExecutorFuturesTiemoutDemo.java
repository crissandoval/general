package cws.demo.type_callable;

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

import cws.demo.tasks.ConstantTimeIntervalsTask;
import cws.demo.utils.DemoUtils;

import static cws.demo.utils.DemoUtils.log;
import static cws.demo.utils.DemoUtils.doGracefullShutdown;

public class ExecutorFuturesTiemoutDemo {
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
	private static long shutdownTiemoutSecs = 30;
	
	private Boolean throwException = null;
	private long getTimeout = 20000;
	private String retValFormat = "tid-%d slept for: %dms";

	public static void main(String[] args) {
		final long start = System.currentTimeMillis();
		log("Using " + NTHREADS + " threads");

		ExecutorFuturesTiemoutDemo executorFuturesTiemoutDemo = new ExecutorFuturesTiemoutDemo();
		executorFuturesTiemoutDemo.setupExecAndRun(args);
		
		DemoUtils.log("Total time: " + (System.currentTimeMillis() - start) + "ms");
		
		doGracefullShutdown(executor, "Pool", shutdownTiemoutSecs);
		log("Exiting");
	}

	/**
	 * Sets up and execute tasks, use CompletionService to block until all tasks
	 * have completed
	 */
	private void setupExecAndRun(String[] args) {
		getCommandLineParams(args);

		final List<Future<Pair<Long, Long>>> futures = new ArrayList<>();
		for (int thread = 0; thread < NTHREADS; thread++) {
			futures.add(executor.submit(new ConstantTimeIntervalsTask(throwException)));
		}

		for (Future<Pair<Long, Long>> future : futures) {
			handleThreadInterupts(future);
			log("processing future");
		}
	}

	private void handleThreadInterupts(Future<Pair<Long, Long>> future) {
		try {
			final Pair<Long, Long> retVal = future.get(getTimeout, TimeUnit.MILLISECONDS);
			log(String.format(retValFormat, retVal.getValue0(), retVal.getValue1()));

		} catch (TimeoutException e) {
			log("Caught TimeoutException on future.get()");
		} catch (InterruptedException e) {
			log("Caught InterruptedException on future.get()");
		} catch (ExecutionException e) {
			log("Caught ExecutionException exception caused by " + e.getCause() + " - take appropiate action");
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
		log("param getTimeout= " + getTimeout + "ms");
		log("param throwException= " + throwException);
		if(shutdownTiemoutSecs < 0)
			log("Task shutdown will not be invoked");
		else
			log("param shutdown timeout " + shutdownTiemoutSecs + "s");
		
		if(throwException == null)
			log("Running Sleep Task");
		else
			log("Using Prime Generation task");
	}
}
