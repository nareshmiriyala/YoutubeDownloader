package com.youtube.downloader;

import com.google.api.services.youtube.model.SearchResult;
import com.youtube.indianmovies.data.Search;
import com.youtube.workerpool.WorkerPool;

import java.io.File;
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
        Search.setNumberOfVideosReturned(10);
        String path = "C:\\Users\\nareshm\\Videos\\Naresh Downloads";
        List<SearchResult> searchResults = youtubeSearch.find();
        WorkerPool workerPool = WorkerPool.getInstance();
        AtomicInteger count = new AtomicInteger(0);
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
        File folderFiles = new File(path);
        File[] listFiles = folderFiles.listFiles();
        Pattern pattern = Pattern.compile(name.substring(0, (name.length() / 2)), Pattern.CASE_INSENSITIVE);
        for (File file : listFiles) {
            if (file.isFile()) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.lookingAt()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String createURL(String videoId) {
        return "http://www.youtube.com/watch?v=".concat(videoId);
    }
}
