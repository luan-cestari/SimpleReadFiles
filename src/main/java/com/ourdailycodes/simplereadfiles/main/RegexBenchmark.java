package com.ourdailycodes.simplereadfiles.main;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexBenchmark {
	
	public static void main(String[] args) throws Exception{

        // The Regex to extract the data from the input. It is using named captured group from JDK7 but it can be removed (just remove the <name> in capturing group in bellow the pattern and use matcher.group(x) instead of the name of the group)
        Pattern pattern =
                Pattern.compile("--MIME_boundary-\\d*\\n(?:Content-Type: text/xml; charset=\"UTF-8\"\\nContent-Transfer-Encoding: 8bit\\nContent-Location: dataHeader\\.xml\\n<\\?xml version=\"1\\.0\" encoding=\"UTF-8\"\\?>\\n<Header>\\n <DataLen>(?<first>.*)</DataLen>|Content-Type: application/octet-stream\\nContent-Location: data\\.bin\\n(?<second>.*)\\n)");
                //Pattern.compile("(?:<DataLen>(?<first>.*)</DataLen>|Content-Location: data\\.bin\\n(?<second>.*)\\n)");

        // read the intere file named "input" into a String instance
        String input = Charset.defaultCharset().decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get("input")))).toString();

        for (int i = 0; i < 10; i++){
        long startTime = System.nanoTime();

        Matcher matcher = pattern.matcher(input);

        boolean found = false;

        while (matcher.find()) {
//            System.out.println(matcher.group("first")); //matcher.group(1)
//            System.out.println(matcher.group("second")); //if matcher.group(1) is null, matcher.group(2)
//            System.out.println("!!!!!!!!!!!!");
            found = true;
        }

        long stopTime = System.nanoTime();
        long elapsedTime = stopTime - startTime;
        System.out.println("Program took: "+((double)elapsedTime/1000000)+" milliseconds");

        if(!found){
            System.out.println("No match found");
        }
        }
    }

}
