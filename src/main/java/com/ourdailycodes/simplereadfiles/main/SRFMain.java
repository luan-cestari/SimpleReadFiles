package com.ourdailycodes.simplereadfiles.main;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class SRFMain {
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

		long startTime = System.currentTimeMillis();
		for(final String s : args){
			e.submit(new Runnable() {
				@Override
				public void run() {
					try {
						long startTime = System.currentTimeMillis();
						FileInputStream f = new FileInputStream( s );
						FileChannel ch = f.getChannel( );
						byte[] barray = new byte[bbSize];
						ByteBuffer bb = ByteBuffer.wrap( barray );
                        Charset charset = Charset.forName(encoding);
                        CharsetDecoder decoder = charset.newDecoder();
                        StringBuilder sb = new StringBuilder();
						int nRead;
						while ( (nRead=ch.read( bb )) != -1 )
						{
                            CharBuffer cb = decoder.decode( bb );
                            String s2 = cb.toString();
                            sb.append(s2);
							bb.clear( );
						}
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
