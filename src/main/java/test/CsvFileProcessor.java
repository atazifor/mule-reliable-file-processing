package test;


import org.apache.log4j.Logger;

public class CsvFileProcessor {
	final static Logger logger = Logger.getLogger(CsvFileProcessor.class);
	public String process(String payload) throws InterruptedException{
		int counter = 5;
		while(counter  > 0){
			Thread.sleep(500);
			logger.info("CsvFileProcessor THREAD "+ Thread.currentThread().getName());
			counter--;
		}
		//Thread.sleep(5000);
		return "CSV Finished processing";
	}
}
