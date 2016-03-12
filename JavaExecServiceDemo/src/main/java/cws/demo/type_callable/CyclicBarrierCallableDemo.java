package cws.demo.type_callable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.javatuples.Pair;
import static cws.demo.utils.DemoUtils.doGracefullShutdown;
import static cws.demo.utils.DemoUtils.analyizeEcecutionException;
import static cws.demo.utils.DemoUtils.log;

public class CyclicBarrierCallableDemo {
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
	
	public static void main(String[] args) throws Exception {
		log("Using " + NTHREADS + " threads");	
		
		CyclicBarrierCallableDemo barrierCallableDemo = new CyclicBarrierCallableDemo();
		barrierCallableDemo.setupExecAndRun();	
		doGracefullShutdown(executor, "Pool", 30);
	}
	
	private void setupExecAndRun() throws Exception{
		final CyclicBarrier cyclicBarrier = new CyclicBarrier(NTHREADS);
		final List<CyclicBarrierCallableDemoTask> tasks = new ArrayList<>();

		for (int i = 0; i < NTHREADS; i++) {
			CyclicBarrierCallableDemoTask task = new CyclicBarrierCallableDemoTask(cyclicBarrier);
			tasks.add(task);
		}
		final List<Future<Pair<Long, Long>>> futures = executor.invokeAll(tasks);
		
		try {
			for(final Future<Pair<Long, Long>> future : futures) {
				final Pair<Long, Long> retVal = future.get();
				log("tid-" + retVal.getValue0() + " task worked for " + retVal.getValue1() + "ms");
				future.cancel(true);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			throw analyizeEcecutionException(e.getCause());
		}		
	}
	
	private class CyclicBarrierCallableDemoTask implements Callable<Pair<Long, Long>>{
		final CyclicBarrier cyclicBarrier;
		
		public CyclicBarrierCallableDemoTask(final CyclicBarrier cyclicBarrier) {
			this.cyclicBarrier = cyclicBarrier;
		}
		
		public Pair<Long, Long> call() {
			final long sleepTime = (int)(Math.random() * 10000);
			try {
				log("Waiting for peers to synch before starting");
				// start - wait until all threads have invoked await() on this barrier
				cyclicBarrier.await();
				
				log("Starting, task will work for " + sleepTime + " ms");
				Thread.sleep(sleepTime);
				
				log("Waiting for peers to synch before ending");
				// task is done - wait until all threads have invoked await() on this barrier
				cyclicBarrier.await();

				log("Task completed");				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new Pair<Long,Long>(Thread.currentThread().getId(), sleepTime);
		}
	}
}
