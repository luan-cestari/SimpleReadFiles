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
		
		private IOThread(String encoding, int bbSize, String filePath) {
			this.encoding = encoding;
			this.bbSize = bbSize;
			this.filePath = filePath;
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
				long checkSum = 0L;
				int nRead;
				while ( (nRead=ch.read( bb )) != -1 )
				{
					for ( int i=0; i<nRead; i++ )checkSum += barray[i];
					bb.clear( );
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
		Thread.sleep(10000);
		
		String encoding = System.getProperty("file.encoding");
		String threadpoolmaxStr = System.getProperty("threadpoolmax");
		String queuesizeStr = System.getProperty("queuesize");
		String bbsizeStr = System.getProperty("bbsize");
		
		int tpm = threadpoolmaxStr == null ? 1 : Integer.parseInt(threadpoolmaxStr);
		int queueSize = queuesizeStr == null ? 1 : Integer.parseInt(queuesizeStr);
		int bbSize = bbsizeStr == null ? 1 : Integer.parseInt(bbsizeStr);
		ThreadPoolExecutor e = new ThreadPoolExecutor(tpm, tpm, 3, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(queueSize, true), new NamedThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy() );

		for(final String filePath : paths){
			e.submit(new IOThread(encoding, bbSize, filePath));
		}
		
		// Make the pool not accept new requests for process and force the termination in 5 minutes 
		e.shutdown();
		e.awaitTermination(5, TimeUnit.MINUTES);
	}
}
