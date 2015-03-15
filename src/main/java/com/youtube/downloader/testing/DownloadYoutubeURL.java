package com.youtube.downloader.testing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nareshm on 15/03/2015.
 */
public class DownloadYoutubeURL {
    static public final int BUF_SIZE = 4 * 1024;
    static public final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11";
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    public static void main(String[] args) throws MalformedURLException {
        URL url = new URL("https://www.youtube.com/watch?v=OV8lsIO9qmI");
        RandomAccessFile fos = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            conn.setRequestProperty("User-Agent", USER_AGENT);
            File f = new File("C:\\Users\\nareshm\\Videos\\Naresh Downloads\\Java\\new1.webm");
            f.createNewFile();
            fos = new RandomAccessFile(f, "rw");
            BufferedInputStream binaryreader = new BufferedInputStream(conn.getInputStream());
            byte[] bytes = new byte[BUF_SIZE];
            int read = 0;
            while ((read = binaryreader.read(bytes)) > 0) {
                fos.write(bytes, 0, read);
            }
            binaryreader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null)
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

}
