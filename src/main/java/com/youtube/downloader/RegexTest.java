package com.youtube.downloader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nareshm on 9/03/2015.
 */
public class RegexTest {
    public static void main(String[] args) {
        String fileName = "Goat Sex in India Prank (";
        System.out.println(fileName.replaceAll("\\(", "").replaceAll("\\)", ""));
        String name = "Racist Congressman's Son Prank (animated) - Ownage Pranks";
        System.out.println(name.substring(0, (name.length() / 2)));
        Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fileName.trim());
        System.out.println("lookingAt = " + matcher.lookingAt());
        System.out.println("matches   = " + matcher.matches());
        if (matcher.find()) {
            System.out.println("True");
        }
    }
}
