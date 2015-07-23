package test;


import org.apache.log4j.Logger;

public class XmlFileProcessor {
	final static Logger logger = Logger.getLogger(XmlFileProcessor.class);
	public String process(String payload) throws InterruptedException{
		int counter = 5;
		while(counter  > 0){
			Thread.sleep(1000);
			logger.info("XmlFileProcessor THREAD "+ Thread.currentThread().getName());
			counter--;
			/*if(counter == 3)
				throw new RuntimeException("XmlFileProcessor");*/
		}
		//Thread.sleep(5000);
		return "XML finished processing";
	}
}
