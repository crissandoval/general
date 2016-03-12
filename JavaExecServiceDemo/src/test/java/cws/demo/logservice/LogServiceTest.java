package cws.demo.logservice;

import org.junit.Test;

//import org.junit.Assert;
import junit.framework.TestCase;
import logservice.LogService;

public class LogServiceTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	@Test
	public void testUniqueInstance(){
		LogService logger1 = LogService.getInstance();
		LogService logger2 = LogService.getInstance();
		
		assertEquals(true, logger1 == logger2);
	}

}
