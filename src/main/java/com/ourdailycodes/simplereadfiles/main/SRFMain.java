package com.ourdailycodes.simplereadfiles.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SRFMain {
	private static final class NamedThreadFactory implements ThreadFactory {
		public static final String PREFIX = "IO_WORKER#";
		private AtomicInteger counter = new AtomicInteger();
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, PREFIX+counter.incrementAndGet());
			return t;
		}
	}

	private static final class IOThread implements Runnable {
		private static final boolean DEBUG = true;
		private final String encoding;
		private final int bbSize;
		private final String filePath;
		private final Pattern pattern;
		
		private IOThread(String encoding, int bbSize, String filePath, Pattern pattern) {
			this.encoding = encoding;
			this.bbSize = bbSize;
			this.filePath = filePath;
			this.pattern = pattern;
		}

		@Override
		public void run() {
			long startTime;
			if(DEBUG) startTime = System.nanoTime();
			
			FileInputStream f = null;
			try {
				//Read the file
				f = new FileInputStream( filePath );
				FileChannel ch = f.getChannel( );
				byte[] barray = new byte[bbSize];
				ByteBuffer bb = ByteBuffer.wrap( barray );
		        Charset charset = Charset.forName(encoding);
		        StringBuilder sb = new StringBuilder();
				while ( ch.read( bb ) != -1 )
				{
					String s2 = new String( barray, charset );
		            sb.append(s2);
					bb.clear( );
				}
				String input = sb.toString();
				
				//Extract the data
				Matcher matcher = pattern.matcher(input);
				while (matcher.find()) {
					//This will always generate two groups for each match. It means even if the file have only one entry, it will produce these two groups representing the two different pattern that it have to extract the data (so always one of them is null)
		            //System.out.println(matcher.group("first")); //matcher.group(1) -> from <DataLen>
		            //System.out.println(matcher.group("second")); //if matcher.group(1) is null, matcher.group(2) -> from Content-Location: data.bin
		            //System.out.println("!!!!!!!!!!!!");
				}
				// if in debug mode, print the time to process this file
				if(DEBUG){
					long stopTime = System.nanoTime();
					long elapsedTime = stopTime - startTime;
					System.out.println(Thread.currentThread().getName()+" processed in "+((double)elapsedTime/1000000)+" milliseconds");
				}
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				if(f != null) {
					try {
						f.close();
					} catch (IOException e) {
						System.out.println(e);
					}
				}
			}
		}
	}

	public static void main(String[] paths) throws Exception {
		
		String encoding = System.getProperty("file.encoding");
		String threadpoolmaxStr = System.getProperty("threadpoolmax");
		String queuesizeStr = System.getProperty("queuesize");
		String bbsizeStr = System.getProperty("bbsize");
		
		int tpm = threadpoolmaxStr == null ? 1 : Integer.parseInt(threadpoolmaxStr);
		int queueSize = queuesizeStr == null ? 1 : Integer.parseInt(queuesizeStr);
		int bbSize = bbsizeStr == null ? 1 : Integer.parseInt(bbsizeStr);
		// this pattern follows the sample in http://pastebin.com/8zB2MECj
		Pattern pattern = Pattern.compile("--MIME_boundary-\\d*\\n(?:Content-Type: text/xml; charset=\"UTF-8\"\\nContent-Transfer-Encoding: 8bit\\nContent-Location: dataHeader\\.xml\\n<\\?xml version=\"1\\.0\" encoding=\"UTF-8\"\\?>\\n<Header>\\n <DataLen>(?<first>.*)</DataLen>|Content-Type: application/octet-stream\\nContent-Location: data\\.bin\\n(?<second>.*)\\n)");
		ThreadPoolExecutor e = new ThreadPoolExecutor(tpm, tpm, 3, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(queueSize, true), new NamedThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy() );

		for(final String filePath : paths){
			e.submit(new IOThread(encoding, bbSize, filePath, pattern));
		}
		
		// Make the pool not accept new requests for process and force the termination in 5 minutes 
		e.shutdown();
		e.awaitTermination(5, TimeUnit.MINUTES);
	}
}
