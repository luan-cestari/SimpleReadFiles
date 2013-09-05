package com.ourdailycodes.simplereadfiles.main;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;

public class SRFMain {
	public static void main(String[] args) throws Exception {
		System.out.println("!!!BEGIN!!!");
		String encoding = System.getProperty("file.encoding");
		String threadpoolmaxStr = System.getProperty("threadpoolmax");
		String queuesizeStr = System.getProperty("queuesize");
		int tpm = threadpoolmaxStr == null ? 1 : Integer.parseInt(threadpoolmaxStr);
		int queueSize = queuesizeStr == null ? 1 : Integer.parseInt(queuesizeStr);

		ThreadPoolExecutor e = new ThreadPoolExecutor(tpm, tpm, 3, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());

		for(final String s : args){
			e.submit(new Runnable() {
				@Override
				public void run() {
					try {
						long startTime = System.currentTimeMillis();
						FileInputStream f = new FileInputStream( s );
						FileChannel ch = f.getChannel( );
						byte[] barray = new byte[262144];
						ByteBuffer bb = ByteBuffer.wrap( barray );
						long checkSum = 0L;
						int nRead;
						while ( (nRead=ch.read( bb )) != -1 )
						{
							for ( int i=0; i<nRead; i++ )
								checkSum += barray[i];
							bb.clear( );
						}
						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						System.out.println(s+" : "+elapsedTime);
					} catch (Exception ex) {
					} finally {
					}
				}
			});
		}
		e.shutdown();
		e.awaitTermination(1, TimeUnit.MINUTES);
		System.out.println("!!!END!!!");
	}
}
