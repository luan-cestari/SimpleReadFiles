package com.ourdailycodes.simplereadfiles.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class IOBenchmark {
	public static void main(String[] args) throws Exception {
		System.out.println("!!!BEGIN!!!");
		final String encoding = System.getProperty("file.encoding");
		String threadpoolmaxStr = System.getProperty("threadpoolmax");
		String queuesizeStr = System.getProperty("queuesize");
		String bbsizeStr = System.getProperty("bbsize");
		int tpm = threadpoolmaxStr == null ? 1 : Integer.parseInt(threadpoolmaxStr);
		int queueSize = queuesizeStr == null ? 1 : Integer.parseInt(queuesizeStr);
		final int bbSize = bbsizeStr == null ? 1 : Integer.parseInt(bbsizeStr);

		ThreadPoolExecutor e = new ThreadPoolExecutor(tpm, tpm, 3, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());

		long startTime = System.nanoTime();
		for(final String s : args){
			e.submit(new Runnable() {
				@Override
				public void run() {
					FileInputStream f = null;
					try {
						long startTime = System.nanoTime();
						f = new FileInputStream( s );
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
						long stopTime = System.nanoTime();
						long elapsedTime = stopTime - startTime;
						System.out.println(s+" : "+((double)elapsedTime/1000000)+" milliseconds");
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
			});
		}
		e.shutdown();
		e.awaitTermination(1, TimeUnit.MINUTES);
		long stopTime = System.nanoTime();
		long elapsedTime = stopTime - startTime;
		System.out.println("Program took: "+((double)elapsedTime/1000000)+" milliseconds");
		System.out.println("!!!END!!!");
	}
}
