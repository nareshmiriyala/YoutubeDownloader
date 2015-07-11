package com.youtube.downloader.biz;

import com.google.api.services.youtube.model.SearchResult;
import com.youtube.downloader.util.Utility;
import com.youtube.indianmovies.data.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nareshm on 8/03/2015.
 */
public class ConcurrentDownloader {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentDownloader.class);
    public static final int NUMBER_OF_VIDEOS_RETURNED = 50;
    private static String searchQuery = null;
    private static int noOfDaysToSearch;
    private static int noOfVideosToDownload;

    private static String videoLength;

    public static void main(String[] args) throws IOException {
        Search youtubeSearch = new Search();
        getInputData();

        Utility.addSearchFilters(youtubeSearch, noOfDaysToSearch, videoLength);
        List<SearchResult> finalSearchResultList = new ArrayList<>();
        Utility.findAndFilterVideos(finalSearchResultList, youtubeSearch, searchQuery, noOfVideosToDownload);
        logger.info("Final Videos being Downloaded size {}", finalSearchResultList.size());
        Utility.displaySearchResults(finalSearchResultList);
        DOWNLOAD_METHOD download_method = getDownloadMethod();
        processDownload(finalSearchResultList, download_method);
        System.exit(0);

    }

    private static void getInputData() {
        try {
            searchQuery = getInputSearchString();
            noOfVideosToDownload = getInputVideosToDownload();
            noOfDaysToSearch = getInputDaysToSearch();
            videoLength = getVideoLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processDownload(List<SearchResult> finalSearchResultList, DOWNLOAD_METHOD download_method) throws IOException {
        switch (download_method) {
            case ONE_WITH_VIDEOID:
                String videoId = getVideoId();
                if (isDownloadApproved()) {
                    Utility.downloadVideo(videoId, finalSearchResultList);
                }
                break;
            case ALL:
                if (isDownloadApproved()) {
                    for (SearchResult searchResult : finalSearchResultList) {
                        Utility.downloadVideo(searchResult);
                    }
                }
                break;
        }

        Utility.shutDownPool();
    }

    private static String getVideoLength() throws IOException {
        logger.info("Enter the length of video ,values can be any/long/medium/short");
        return Utility.getInputString();
    }

    private static String getVideoId() throws IOException {
        logger.info("\nEnter VideoId to download:");
        return Utility.getInputString();
    }

    private static DOWNLOAD_METHOD getDownloadMethod() throws IOException {
        logger.info("\nEnter Download option number\n Option 1:Download ALL \n Option Any Key: Specific VideoId");
        String downloadMethod = Utility.getInputString();
        if (downloadMethod.equalsIgnoreCase("1")) {
            return DOWNLOAD_METHOD.ALL;
        }
        return DOWNLOAD_METHOD.ONE_WITH_VIDEOID;
    }

    private enum DOWNLOAD_METHOD {
        ALL, ONE_WITH_VIDEOID
    }

    private static boolean isDownloadApproved() throws IOException {

        logger.info("\nApprove Download Videos: YES/NO");
        String inputString = Utility.getInputString();
        APPROVAL inputApproval = APPROVAL.valueOf(inputString);
        switch (inputApproval) {
            case YES:
                return true;
            case NO:
                return false;
        }
        return false;
    }

    private enum APPROVAL {
        YES, NO
    }

    private static int getInputDaysToSearch() throws IOException {
        String inputQuery;

        logger.info("\nPlease enter number of days to search: ");
        inputQuery = Utility.getInputString();

        if (inputQuery.length() < 1) {
            inputQuery = "5";
        }
        return Integer.parseInt(inputQuery);
    }

    private static int getInputVideosToDownload() throws IOException {
        String inputQuery;

        logger.info("Please enter Number of Videos to retrieve: ");
        inputQuery = Utility.getInputString();

        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "5";
        }
        return Integer.parseInt(inputQuery);
    }

    private static String getInputSearchString() throws IOException {

        String inputQuery;

        logger.info("Please enter a search term: ");
        inputQuery = Utility.getInputString();

        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
    }
}
