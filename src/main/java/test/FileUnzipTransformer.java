package test;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.config.i18n.MessageFactory;
import org.mule.transformer.compression.AbstractCompressionTransformer;
import org.mule.util.IOUtils;

public class FileUnzipTransformer extends AbstractCompressionTransformer {
	public static final int BUFFER = 2048;
	private String extractToDir;
	private MuleEvent muleEvent;
	
	
	protected synchronized  Object doTransform(Object byteArray, String encoding)	throws TransformerException {
		System.out.println("Inside the unzip transform");
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream((byte[]) byteArray));
		ZipEntry entry;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		
		try {
			System.out.println("Right before while statement -------- " + zis.available());
			while ((entry = zis.getNextEntry()) != null) {
				System.out.println("zipping -------- " + zis.available());
				System.out.println("entry -------- " + entry.getName());
//				String filePath = extractToDir + File.separator	+ entry.getName();
//				System.out.println("__________________________ filePath: " + filePath);
//				FileOutputStream fos = new FileOutputStream(filePath);
				BufferedOutputStream dest = new BufferedOutputStream(bos,BUFFER);
//				System.out.println("__________________________ dest: " + dest.toString());
				IOUtils.copy(zis, dest);

				dest.flush();
				dest.close();
				
				muleEvent.getMessage().setProperty("filename", entry.getName(), PropertyScope.INBOUND);
			}
			zis.close();
//			System.out.println(bos.toString());
			return bos.toByteArray();
			
		} catch (IOException ioException) {
			throw new TransformerException(
					MessageFactory.createStaticMessage("Failed to uncompress file."),this, ioException);
		} finally {
//			IOUtils.closeQuietly(zis);
		}

	}
	
	@Override
	public synchronized MuleEvent process(MuleEvent event) throws MuleException {
		muleEvent = event;
		return super.process(event);
		
	}
	public String getExtractToDir() {
		return extractToDir;
	}

	public void setExtractToDir(String extractToDir) {
		this.extractToDir = extractToDir;
	}

}
