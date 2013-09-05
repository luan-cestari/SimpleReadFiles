package com.ourdailycodes.simplereadfiles.main;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.io.FileInputStream;
import java.io.DataInputStream;
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

		long startTime = System.currentTimeMillis();
		for(final String s : args){
			e.submit(new Runnable() {
				@Override
				public void run() {
					try {
						long startTime = System.currentTimeMillis();
						FileInputStream f = new FileInputStream( s );
                        DataInputStream d = new DataInputStream(f);
						byte[] barray = new byte[((int)f.getChannel().size())];
                        d.read(barray);
						long checkSum = 0L;
						int nRead = barray.length;
						for ( int i=0; i<nRead; i++ )
						    checkSum += barray[i];

						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						System.out.println(s+" : "+elapsedTime+" milliseconds");
					} catch (Exception ex) {
					} finally {
					}
				}
			});
		}
		e.shutdown();
		e.awaitTermination(1, TimeUnit.MINUTES);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Program took: "+elapsedTime+" milliseconds");
		System.out.println("!!!END!!!");
	}
}
