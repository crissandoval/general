package cws.demo.type_runnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cws.demo.utils.DemoUtils.doGracefullShutdown;
import static cws.demo.utils.DemoUtils.log;

public class CyclicBarrierDemo {
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
	private static final ConcurrentHashMap<Long, Integer> results = new ConcurrentHashMap<>();
	
	public static void main(String[] args) throws Exception {
		log("Using " + NTHREADS + " threads");

		final CyclicBarrier cyclicBarrier = new CyclicBarrier(NTHREADS+1);

		for (int thread = 0; thread < NTHREADS; thread++) {
			executor.execute(new Runnable() {
				public void run() {
					try {
						log("Waiting for peers to synch before starting");
						// start - wait until all threads have invoked await() on this barrier
						cyclicBarrier.await();
						
						final int sleepTime = (int)(Math.random() * 10000);
						log("Starting, task will work for " + sleepTime + " ms");
						Thread.sleep(sleepTime);
						CyclicBarrierDemo.results.put(Thread.currentThread().getId(), sleepTime);

						log("Waiting for peers to synch before ending");
						// task is done - wait until all threads have invoked await() on this barrier
						cyclicBarrier.await();

						log("Task completed");
					
					} catch (Exception e) {
						log(e.toString());
					}
				}
			});
			Thread.sleep(100);
		}	
		log("Waiting for peers to synch before starting");
		cyclicBarrier.await();
		log("Waiting for peers to synch before ending");
		cyclicBarrier.await();
		
		if(CyclicBarrierDemo.results.isEmpty())
			log("hash empty");
		
		for(Long key : CyclicBarrierDemo.results.keySet()) {
			log("result (tid, return value) " + key + ", " + CyclicBarrierDemo.results.get(key));	
		}
		
		doGracefullShutdown(executor, null, 30);

	}
}
