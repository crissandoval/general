package logservice;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.javatuples.Pair;
import cws.demo.utils.MyDemoException;

public class LogSrvConstantTimeIntervalsTask implements Callable<Pair<Long, Long>> {
	public static LogService logger = LogService.getInstance();

	private static final BigInteger WORK_TIME_FACTOR = new BigInteger("1000");
	private boolean sleepOnly = true;
	private boolean throwException = false;
	private boolean doCancel = false;

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
	public LogSrvConstantTimeIntervalsTask(final Boolean throwException) {
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
	public Pair<Long, Long> call() throws InterruptedException, MyDemoException {
		final long workTime = (Thread.currentThread().getId()-1) * WORK_TIME_FACTOR.longValue();

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
		logger.log("Will sleep for " + workTime + "ms");

		try {
			Thread.sleep(workTime);
		} catch (InterruptedException e) {
			logger.log("InterruptedException has been raised by future.cancel()");
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
	private Pair<Long, Long> primeGenerationMode(final long workTime) throws InterruptedException, MyDemoException {
//		logger.start();
		logger.log("Will generate primes for " + workTime + "ms");

		// task will generate primes
		final long endTime = System.currentTimeMillis() + workTime;
		BigInteger prime = BigInteger.ONE.add(BigInteger.ONE);

		while (true) {
			prime = prime.nextProbablePrime();
			logger.log("generated prime " + prime);
			if (throwException && prime.compareTo(WORK_TIME_FACTOR) > 0) {
				logger.log("Thowing MyDemoException");
				throw new MyDemoException();
			}

			if (Thread.currentThread().isInterrupted()) {
				logger.log("Has been Interupted by future.cancel()");
				break;
			}

			if (doCancel) {
				logger.log("Has been cancelled by requestTaskCancelation()");
				break;
			}

			if (System.currentTimeMillis() >= endTime) {
				logger.log("Task completed within alloted time: " + workTime + "ms");
				break;
			}
		}
		return new Pair<Long, Long>(Thread.currentThread().getId(), prime.longValueExact());
	}

	/**
	 * Handles client request to cancel task
	 * 
	 */
	public void requestTaskCancelation() {
		doCancel = true;
	}
}
