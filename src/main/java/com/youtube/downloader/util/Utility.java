package com.youtube.downloader.util;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.youtube.downloader.biz.ConcurrentDownloader;
import com.youtube.downloader.biz.DownloadJob;
import com.youtube.indianmovies.data.Search;
import com.youtube.workerpool.WorkerPool;
import org.apache.commons.io.FileUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by nareshm on 11/07/2015.
 */
public class Utility {
    private static final Logger logger = LoggerFactory.getLogger(Utility.class.getName());
    private static String path = Utility.getPropertyValue("download.directory");
    private static String inputVideoLength="any";
    private static int inputNoOfDaysToSearch;


    public static void addSearchFilters(Search youtubeSearch, int noOfDaysToSearch, String videoLength) throws IOException {
        try {
            YouTube.Search.List searchObject = youtubeSearch.createSearchObject();
            // Format for input
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            String dateTime = dtf.print(dtf.parseDateTime(getCurrentTimeStamp()).minusDays(noOfDaysToSearch));
            searchObject.setPublishedAfter(new com.google.api.client.util.DateTime(dtf.parseDateTime(dateTime).toDate()));
            inputVideoLength=videoLength;
            inputNoOfDaysToSearch=noOfDaysToSearch;
            searchObject.setVideoDuration(videoLength);//Allowed values: [any, long, medium, short]

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static String getInputString() throws IOException {
        String inputQuery;
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();
        return inputQuery;
    }

    public static String createURL(String videoId) {
        return "http://www.youtube.com/watch?v=".concat(videoId);
    }

    public static String getPropertyValue(String property) {
        try {
            return ConfigReader.getInstance().getPropertyValue(property);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isFileAlreadyDownloaded(String videoId) {
        String fileName = null;
        try {
            fileName = getPropertyValue("download.directory") + "\\" + ConfigReader.getInstance().getPropertyValue("config.filename");
            return FileUtils.readFileToString(new File(fileName)).contains(videoId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void findAndFilterVideos(List<SearchResult> finalSearchResultList, Search ySearch, String searchQuery, int videosToDownload) throws IOException {

        logger.info("Called findAndFilterVideos");

        List<SearchResult> searchResults = ySearch.find(searchQuery);
        for (SearchResult searchResult : searchResults) {
            if (!isFileAlreadyDownloaded(searchResult.getId().getVideoId()) && !findIfAddedToList(searchResult.getId().getVideoId(), finalSearchResultList)) {
                finalSearchResultList.add(searchResult);
            }
        }
        if (finalSearchResultList.size() >= videosToDownload) {

            logger.debug("Size of finalSearchResultList equals videoToDownload");
            return;
        }
        //increment search days by 10
        inputNoOfDaysToSearch=inputNoOfDaysToSearch+10;
        addSearchFilters(ySearch,inputNoOfDaysToSearch,inputVideoLength);
        logger.info("Size of finalSearchResultList {} ", finalSearchResultList.size());
        findAndFilterVideos(finalSearchResultList, ySearch, searchQuery, videosToDownload);
    }

    private static boolean findIfAddedToList(String videoId, List<SearchResult> finalSearchResultList) {
        for (SearchResult searchResult : finalSearchResultList) {
            if (searchResult.getId().getVideoId().equals(videoId)) {
                return true;
            }
        }
        return false;
    }

    public static void downloadVideo(String videoId, List<SearchResult> finalSearchResults) {
        for (SearchResult searchResult : finalSearchResults) {
            if (videoId.equals(searchResult.getId().getVideoId())) {
                downloadVideo(searchResult);
                break;
            }
        }
    }

    public static void downloadVideo(SearchResult searchResult) {
        WorkerPool.getInstance();
        String videoId = searchResult.getId().getVideoId();
        String url = createURL(videoId);
        String name = searchResult.getSnippet().getTitle();
        //don't download file if its in the directory
        final DownloadJob downloadJob = new DownloadJob("Download Job:" + url);
        downloadJob.setFileDownloadPath(path);
        downloadJob.setUrlToDownload(url);
        downloadJob.setTitle(name);
        downloadJob.setVideoId(videoId);
        logger.info("Download progress {}", downloadJob.getDownloadProgress());
        WorkerPool.deployJob(downloadJob);
    }

    public static void displaySearchResults(List<SearchResult> finalSearchResultList) {
        for (SearchResult searchResult : finalSearchResultList) {
            logger.info("VideoId: {}, Title: {}", searchResult.getId().getVideoId(), searchResult.getSnippet().getTitle());
        }
    }

    public static void shutDownPool() {
        try {
            WorkerPool.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
