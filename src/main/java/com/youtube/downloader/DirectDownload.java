package com.youtube.downloader;

import com.github.axet.vget.VGet;

import java.io.File;
import java.net.URL;

/**
 * Created by nareshm on 4/11/2014.
 */
public class DirectDownload {
    public static void main(String[] args) {
        try {
            // ex: http://www.youtube.com/watch?v=Nj6PFaDmp6c
            String url = "http://www.youtube.com/watch?v=TBWX97e1E9g&list=PLE7E8B7F4856C9B19";
            // ex: "/Users/axet/Downloads"
            String path = "C:\\Users\\nareshm\\Videos\\4K Video Downloader\\JavaProgramming";
            VGet v = new VGet(new URL(url), new File(path));
            v.download();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
