package cws.demo.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class DemoUtils {
	
	public static void log(final String s) {
		final String print = Thread.currentThread().getId() == 1 ? "main: " + s :
			"tid-"+ Thread.currentThread().getId() + ": " + s;
		System.out.println(System.currentTimeMillis() + "  " + print);
	}
	
    public static RuntimeException analyizeEcecutionException(final Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new IllegalStateException("Not unchecked", t);
    }
    
	public static void doGracefullShutdown(final ExecutorService executor, String source, final long waitInSeconds) {
		if (waitInSeconds < 0) {
			log(source + " Shutdown not executed");
			return;
		}

		try {
			log(source + " starting shutdown with timeout: " + waitInSeconds + "s");
			executor.shutdown();

			if (executor.awaitTermination(waitInSeconds, TimeUnit.SECONDS)) {
				log(source + " executor terminated, program ending");
			} else {
				log(source + " executor NOT terminated, wait timed out after: " + waitInSeconds + "s");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
