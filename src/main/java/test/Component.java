package test;


import org.apache.log4j.Logger;


public class Component {
	final static Logger logger = Logger.getLogger(Component.class);
	public String process(String str) {
		logger.info(">>> Thread in java component: "+Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());
        return str;
    }
}
