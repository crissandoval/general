package cws.demo.tasks;

import static cws.demo.utils.DemoUtils.log;

import java.math.BigInteger;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

import org.javatuples.Pair;

import cws.demo.type_callable.CyclicBarrierCallableDemo;
import cws.demo.utils.MyDemoException;

public class CyclicBarrierTimeIntervalsTask implements Callable<Pair<Long, Long>> {
	private static final BigInteger WORK_TIME_FACTOR = new BigInteger("1000");
	private boolean sleepOnly = true;
	private boolean throwException = false;
	private boolean doCancel = false;
	private CyclicBarrier cyclicBarrier;
	/**
	 * Constructs a task that generates prime number for a calculated, constant
	 * time
	 * 
	 * @param throwException
	 *            - flag indicating whether the task should throw the
	 *            MyDemoException this factor is used
	 * @param workTimeFactorMs
	 *            factor used to calculate time to for generating primes
	 */
	public CyclicBarrierTimeIntervalsTask(final Boolean throwException, 
			                               final CyclicBarrier cyclicBarrier) {
		this.cyclicBarrier = cyclicBarrier;
		if (throwException != null) {
			this.sleepOnly = false;
			this.throwException = throwException;
		}
	}

	/**
	 * Task - implementation of of call() method with return type call() has 2
	 * modes: simple sleep or prime number generation
	 * 
	 * @throws InterruptedException
	 * @throws MyDemoException
	 */
	@Override
	public Pair<Long, Long> call() throws InterruptedException, MyDemoException, BrokenBarrierException {
//		final long workTime = Thread.currentThread().getId() * WORK_TIME_FACTOR.longValue();
//		final long workTime = (int)(Math.random() * 10000);
		final long workTime = 10000;
		// test is task is 'sleep only'
		if (sleepOnly)
			return sleepOnlyMode(workTime);
		else
			return primeGenerationMode(workTime);
	}

	/**
	 * Implements the sleep only task mode
	 * 
	 * @param workTime
	 * @return
	 * @throws InterruptedException
	 */
	private Pair<Long, Long> sleepOnlyMode(final long workTime) throws InterruptedException {
		log("Will sleep for " + workTime + "ms");

		try {
			Thread.sleep(workTime);
			// task is done - wait until all threads have invoked await() on
			// this barrier
//			checkForSynchOnCompletion();
		} catch (InterruptedException e) {
			log("InterruptedException has been raised by future.cancel()");
		} catch (Exception e) {
			log("Exception has been raised by future.cancel()");
		}
		return new Pair<Long, Long>(Thread.currentThread().getId(), workTime);
	}

	/**
	 * Implements the Prime Generation Mode
	 * @param workTime
	 * @return
	 * @throws InterruptedException
	 * @throws MyDemoException
	 */
	private Pair<Long, Long> primeGenerationMode(final long workTime) 
			throws InterruptedException, MyDemoException, BrokenBarrierException {
		log("Will generate primes for " + workTime + "ms");

		// task will generate primes
		final long endTime = System.currentTimeMillis() + workTime;
		BigInteger prime = BigInteger.ONE.add(BigInteger.ONE);

		while (true) {
			prime = prime.nextProbablePrime();

			if (throwException && prime.compareTo(WORK_TIME_FACTOR) > 0) {
				log("Thowing MyDemoException");
				throw new MyDemoException();
			}

			if (Thread.currentThread().isInterrupted()) {
				log("Has been Interupted by future.cancel()");
				break;
			}

			if (doCancel) {
				log("Has been cancelled by requestTaskCancelation()");
				break;
			}

			if (System.currentTimeMillis() >= endTime) {
				log("Task completed within alloted time: " + workTime + "ms");
				break;
			}
		}
		checkForSynchOnCompletion();
		return new Pair<Long, Long>(Thread.currentThread().getId(), prime.longValueExact());
	}
	
	private void checkForSynchOnCompletion() throws InterruptedException, BrokenBarrierException{
		log("Waiting for synch on task completion");
		cyclicBarrier.await();
	}

	/**
	 * Handles client request to cancel task
	 * 
	 */
	public void requestTaskCancelation() {
		doCancel = true;
	}
}
