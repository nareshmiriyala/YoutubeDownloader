package com.youtube.downloader.util;

import info.debatty.java.stringsimilarity.Damerau;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.LongestCommonSubsequence;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

/**
 * Created by nareshm on 12/07/2015.
 */
public class TestStringCompare {
    public static void main(String[] args) {
        String name1="Asian Babysitting Nightmare Prank - Ownage Pranks";
        String name2="Asian Babysitting Nightmare Prank - Ownage Pranks";
        JaroWinkler jaroWinkler=new JaroWinkler();
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
        NormalizedLevenshtein l = new NormalizedLevenshtein();
        System.out.println(l.distance(name1,name2));
        double similarity = jaroWinkler.similarity(name1, name2);
        Damerau d = new Damerau();
        System.out.println(d.distance(name1,name2));

        System.out.println(lcs.distance(name1,name2));
        System.out.println(similarity);

    }
}
