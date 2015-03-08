package com.youtube.downloader;

import com.google.api.services.youtube.model.SearchResult;
import com.youtube.indianmovies.data.Search;
import com.youtube.workerpool.WorkerPool;

import java.util.List;

/**
 * Created by nareshm on 8/03/2015.
 */
public class ConcurrentDownloader {
    public static void main(String[] args) {
        WorkerPool workerPool = WorkerPool.getInstance();
        Search youtubeSearch = new Search();
        String path = "C:\\Users\\nareshm\\Videos\\Naresh Downloads";
        List<SearchResult> searchResults = youtubeSearch.find();
        searchResults.forEach(searchResult -> {
            String url = createURL(searchResult.getId().getVideoId());
            DownloadJob downloadJob = new DownloadJob("Download Job:"+url);
            downloadJob.setFileDownloadPath(path);
            downloadJob.setUrlToDownload(url);
            workerPool.deployJob(downloadJob);
        });

    }

    private static String createURL(String videoId) {
        return "http://www.youtube.com/watch?v=".concat(videoId);
    }
}
