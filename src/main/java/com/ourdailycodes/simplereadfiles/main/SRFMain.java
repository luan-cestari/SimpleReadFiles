package com.ourdailycodes.simplereadfiles.main;

public class SRFMain {
	public static void main(String[] args) throws Exception {
		System.out.println("!!!BEGIN!!!");
		String encoding = System.getProperty("file.encoding");
		String threadpoolmax = System.getProperty("threadpoolmax");

		// TODO Create the THreadPool , reading all the files (and waiting for available thread), put all files read into the memory and process them with the business rules
		for(String s : args){
			System.out.println(s);
		}
		System.out.println("!!!END!!!");
	}
	/*
	{
		FileInputStream f = new FileInputStream( name );
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
	}
	*/
}
