package logservice;

import cws.demo.utils.DemoUtils;

public class LogServiceTest {
	public static LogService logger = LogService.getInstance();
	
	public static void main(String[] args) {
		final long start = System.currentTimeMillis();
		
		for(int i=0; i<999; i++){
			if(i==500)
				logger.stop();
			logger.log("sss "+ i); 
		}
		
		System.out.println("Starting sleep");
		try{
			Thread.sleep(30*1000);
		}catch(Exception e){
			
		}
		System.out.println("Ending sleep");

		for(int i=1000; i<1999; i++)
			logger.log("sss "+ i);
		
		DemoUtils.log("Total time: " + (System.currentTimeMillis() - start) + "ms");
	}
}
