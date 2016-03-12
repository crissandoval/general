package cws.demo.type_callable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.Runtime;

import org.javatuples.Pair;

import cws.demo.tasks.ConstantTimeIntervalsTask;

import static cws.demo.utils.DemoUtils.log;
import static cws.demo.utils.DemoUtils.doGracefullShutdown;;

public class ExecutorCompletionDemo {
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
	private static long shutdownTiemoutSecs = 30;

	private Boolean throwException = null;
	private long getTimeoutMs = 20000;
	private String retValFormat = "tid-%d slept for: %dms";

	public static void main(String[] args) {
		log("Using " + NTHREADS + " threads");

		ExecutorCompletionDemo executorCompletionDemo = new ExecutorCompletionDemo();
		executorCompletionDemo.setupExecAndRun(args);
		doGracefullShutdown(executor, "Pool", ExecutorCompletionDemo.shutdownTiemoutSecs);
	}

	/**
	 * Sets up and execute tasks, use CompletionService to block until all tasks
	 * have completed
	 */
	private void setupExecAndRun(String[] args) {
		getCommandLineParams(args);

		CompletionService<Pair<Long, Long>> completionService = new ExecutorCompletionService<Pair<Long, Long>>(
				executor);
		
		final List<ConstantTimeIntervalsTask> tasks = new ArrayList<>();
		for (int thread = 1; thread <= NTHREADS; thread++) {
			final ConstantTimeIntervalsTask task = new ConstantTimeIntervalsTask(throwException);
			completionService.submit(task);
			tasks.add(task);
		}

		try {
			int numTaskWaitFailures = 0;
			for (int i = 0; i < NTHREADS; i++) {
				final Future<Pair<Long, Long>> future = completionService.poll(getTimeoutMs, TimeUnit.MILLISECONDS);
				if (future == null) {
					numTaskWaitFailures++;
				}else{
					final Pair<Long, Long> retVal = future.get();
					log(String.format(retValFormat, retVal.getValue0(), retVal.getValue1()));
				}
			}
			if(numTaskWaitFailures > 0) {
				log("Task-wait timed out on " + numTaskWaitFailures + " tasks, requesting task cancelation");
				for(ConstantTimeIntervalsTask task : tasks) 
					task.requestTaskCancelation();
			}
				
		} catch (InterruptedException e) {
			log("Caught InterruptedException on completionService.poll()");
		} catch (ExecutionException e) {
			log("Caught ExecutionException exception caused by " + e.getCause());
		}
	}

	private void getCommandLineParams(String[] args) {

		for (int i = 0; i < args.length; i++) {
			if (i == 0)
				getTimeoutMs = Long.decode(args[0]);
			if (i == 1) {
				throwException = Boolean.valueOf(args[1]);
				retValFormat = "tid-%d generated largest prime: %d";
			}
			if (i == 2) {
				shutdownTiemoutSecs = Long.decode(args[2]);
			}
		}
		log("param getTimeout= " + getTimeoutMs + "ms");
		log("param throwException= " + throwException);
		log("param shutdown timeout " + shutdownTiemoutSecs + "s");
		if (throwException == null)
			log("Running Sleep Only task");
		else
			log("Using Prime Generation task");

	}
}
