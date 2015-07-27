package flows.test;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mule.api.context.notification.EndpointMessageNotificationListener;
import org.mule.api.context.notification.ServerNotification;
import org.mule.tck.junit4.FunctionalTestCase;

public class TestFileAggregation extends FunctionalTestCase {
	@Rule
    public Timeout globalTimeout = new Timeout(120000);//
	
	CountDownLatch dispatcLatch;
	CountDownLatch receiveLatch;
	CountDownLatch dlqReceiveLatch;
	
	@Override
	protected String getConfigResources() {
		return "reliable-file-processing.xml";
	}
	
	public void doSetUp() throws Exception{
		super.doSetUp();
		File dataDirectory = new File("./data");
        if (dataDirectory.exists()) {
            FileUtils.deleteDirectory(dataDirectory);
        }
        dataDirectory.mkdirs();
        new File("./data/in").mkdirs();
        new File("./data/zip").mkdirs();
        new File("./data/zip/in").mkdirs();
        new File("./data/out/csv").mkdirs();
        //using VM 
        new File("./data/out/xml").mkdirs();
        
        muleContext.registerListener(new EndpointMessageNotificationListener() {
            public void onNotification(final ServerNotification notification) {
            	logger.warn("ResourceIdentifier#: " + notification.getResourceIdentifier() + ": " +notification.getActionName());
                if ("aggregate-flow".equals(notification.getResourceIdentifier())){
                	if("receive".equals(notification.getActionName()))
                		receiveLatch.countDown();
                	if("end dispatch".equals(notification.getActionName()))
                		dispatcLatch.countDown();
                }
                if ("dead_queue-letter-flow".equals(notification.getResourceIdentifier())){
                	if("receive".equals(notification.getActionName()))
                		dlqReceiveLatch.countDown();
                }
            }
        });
        
	}
	/**
	 * Expected pairs of files are put in inbound end point
	 * @throws Exception
	 */
	@Test
	public void testConsistentFiles() throws Exception{
		int counter = 5; //total of 10 files; 5 csv, 5 xml
		receiveLatch = new CountDownLatch(counter*2);// 10 files should be processed in groups of 2
        dispatcLatch = new CountDownLatch(counter);//5 groups of files (after aggregation)
	        assertThat(0, is(FileUtils.listFiles(new File("./data/out"), new String[]{"xml", "csv"}, false).size()));
	        File csvFile;
	        File xmlFile;
	        for (int i = 0; i < 5; i++) {
				csvFile = new File("./data/zip/1"+i+"-first"+i+".csv");
				xmlFile = new File("./data/zip/1"+i+"-second"+i+".xml");
				FileUtils.writeStringToFile(csvFile, "csv is okay");
				FileUtils.writeStringToFile(xmlFile, "xml is better");
			}
			createZipFile("./data/zip", "./data/zip/in");
			//zip("./data/zip", "./data/zip/in/archive.zip");
			assertTrue(receiveLatch.await(getTestTimeoutSecs(), TimeUnit.SECONDS));
			assertTrue(dispatcLatch.await(getTestTimeoutSecs(), TimeUnit.SECONDS));
	}
	
	/*
	 * 
	 */
	@Test
	public void testInconsistentFiles() throws Exception{
		int counter = 5; //total of 10 files; 5 csv, 5 xml
		receiveLatch = new CountDownLatch(counter*2 + 1);// 10 files should be processed in groups of 2 [+1 for extra file]
        dispatcLatch = new CountDownLatch(counter);//5 groups of files (after aggregation)
        dlqReceiveLatch = new CountDownLatch(1); //1 incomplete group
	        assertThat(0, is(FileUtils.listFiles(new File("./data/out"), new String[]{"xml", "csv"}, false).size()));
	        File csvFile;
	        File xmlFile;
	        for (int i = 0; i < 5; i++) {
				csvFile = new File("./data/zip/1"+i+"-first"+i+".csv");
				xmlFile = new File("./data/zip/1"+i+"-second"+i+".xml");
				FileUtils.writeStringToFile(csvFile, "csv is okay "+ i);
				FileUtils.writeStringToFile(xmlFile, "xml is better " + i);
			}
	        xmlFile = new File("./data/zip/2"+2+"-bad"+2+".xml");
			FileUtils.writeStringToFile(xmlFile, "BAD XML is NOT okay");
			
			createZipFile("./data/zip", "./data/zip/in");
			//zip("./data/zip", "./data/zip/in/archive.zip");
			assertTrue(receiveLatch.await(getTestTimeoutSecs(), TimeUnit.SECONDS));
			assertTrue(dispatcLatch.await(getTestTimeoutSecs(), TimeUnit.SECONDS));
			assertTrue(dlqReceiveLatch.await(getTestTimeoutSecs(), TimeUnit.SECONDS));
	}
	
	private void createZipFile(String srcDir, String zipFile) throws Exception{
		byte[] buffer = new byte[1024];

		File dir = new File(srcDir);

		File[] files = dir.listFiles();

		for (int i = 0; i < files.length; i++) {
			String filename = files[i].getName();
			System.out.println("Adding file: " + filename);
			if (files[i].isFile()) {
				FileOutputStream fos = new FileOutputStream(zipFile + "/"+filename.substring(0, filename.indexOf("."))+".zip");

				ZipOutputStream zos = new ZipOutputStream(fos);
				
				FileInputStream fis = new FileInputStream(files[i]);
				// begin writing a new ZIP entry, positions the stream to the start of the entry data
				zos.putNextEntry(new ZipEntry(files[i].getName()));
				int length;
				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
				// close the InputStream
				fis.close();
				
				// close the ZipOutputStream
				zos.close();
				
				fos.close();
			}
		}

	}
	
}
