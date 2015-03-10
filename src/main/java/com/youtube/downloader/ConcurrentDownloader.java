package com.youtube.downloader;

import com.google.api.services.youtube.model.SearchResult;
import com.youtube.indianmovies.data.Search;
import com.youtube.workerpool.WorkerPool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nareshm on 8/03/2015.
 */
public class ConcurrentDownloader {
    public static void main(String[] args) {
        Search youtubeSearch = new Search();
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger videosToDownload = new AtomicInteger(10);
        String searchQuery = null;
        try {
            searchQuery = getInputQuery();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            Search.setNumberOfVideosReturned(videosToDownload.get());
            String path = "C:\\Users\\nareshm\\Videos\\Naresh Downloads\\Java";
            List<SearchResult> searchResults = youtubeSearch.find(searchQuery);
            WorkerPool.getInstance();
            searchResults.forEach(searchResult -> {
                String url = createURL(searchResult.getId().getVideoId());
                String name = searchResult.getSnippet().getTitle();
                //don't download file if its in the directory
                if (!isFileExists(name, path)) {
                    DownloadJob downloadJob = new DownloadJob("Download Job:" + url);
                    downloadJob.setFileDownloadPath(path);
                    downloadJob.setUrlToDownload(url);
                    downloadJob.setTitle(name);
                    count.incrementAndGet();
                    WorkerPool.deployJob(downloadJob);
                }
            });
            if (count.get() == 0) {
                videosToDownload.addAndGet(10);
            }
            if (count.get() >= 1) {
                break;
            }
        }
        if (0 == count.get()) {
            System.out.println("=============No Videos Downloaded==========");
        }
        try {
            WorkerPool.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private static boolean isFileExists(String name, String path) {
        String replacedName = name.trim().replaceAll("[<>:\"/\\\\|?*\\x00-\\x1F]", "");
        String fileName = path.concat("\\").concat(replacedName + ".webm");
        File folderFiles = new File(fileName);
        if (folderFiles.exists()) {
            return true;
        }
        File[] listFiles = new File(path).listFiles();
        Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        for (File file : listFiles)
            if (file.isFile()) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.lookingAt()) {
                    return true;
                }
            }
        return false;
    }

    private static String createURL(String videoId) {
        return "http://www.youtube.com/watch?v=".concat(videoId);
    }

    private static String getInputQuery() throws IOException {

        String inputQuery;

        System.out.print("Please enter a search term: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();

        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
    }
}
