package com.youtube.downloader;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.youtube.downloader.config.ConfigReader;
import com.youtube.indianmovies.data.Search;
import com.youtube.workerpool.WorkerPool;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nareshm on 8/03/2015.
 */
public class ConcurrentDownloader {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentDownloader.class);
    private static String path = "C:\\Naresh Data\\Development Software\\Videos\\Movies";
    private static AtomicInteger videosToDownload = new AtomicInteger(10);
    private static String searchQuery = null;
    private static int noOfDaysToSearch;


    public static void main(String[] args) throws IOException {
        Search youtubeSearch = new Search();
        int inDownload = 0;
        try {
            searchQuery = getInputQuery();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inDownload = getInputVideosToDownload();
            noOfDaysToSearch = getInputDaysToSearch();
            if (inDownload > 0) {
                videosToDownload.set(inDownload);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Search.setNumberOfVideosReturned(videosToDownload.get());
        addSearchFilters(youtubeSearch);
        List<SearchResult> finalSearchResultList = new ArrayList<>();
        findAndFilterVideos(finalSearchResultList, youtubeSearch);
        logger.info("Final Videos being Downloaded size {}", finalSearchResultList.size());
        displaySearchResults(finalSearchResultList);

        DOWNLOAD_METHOD download_method = getDownloadMethod();

        switch (download_method) {
            case ONE_WITH_VIDEOID:
                String videoId = getVideoId();
                if (isDownloadApproved()) {
                    WorkerPool.getInstance();
                    downloadVideo(videoId, finalSearchResultList);
                }
                break;
            case ALL:
                if (isDownloadApproved()) {
                    WorkerPool.getInstance();
                    for (SearchResult searchResult : finalSearchResultList) {
                        downloadVideo(searchResult);
                    }
                }
                break;
        }

        try {
            WorkerPool.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);

    }

    private static String getVideoId() throws IOException {
        logger.info("\nEnter VideoId to download:");
        return getInputString();
    }

    private static DOWNLOAD_METHOD getDownloadMethod() throws IOException {
        logger.info("\nEnter Download option number\n Option 1:Download ALL \n Option Any Key: Specific VideoId");
        String downloadMethod = getInputString();
        if (downloadMethod.equalsIgnoreCase("1")) {
            return DOWNLOAD_METHOD.ALL;
        }
        return DOWNLOAD_METHOD.ONE_WITH_VIDEOID;
    }

    private enum DOWNLOAD_METHOD {
        ALL, ONE_WITH_VIDEOID
    }

    private static void downloadVideo(String videoId, List<SearchResult> finalSearchResults) {
        for (SearchResult searchResult : finalSearchResults) {
            if (videoId.equals(searchResult.getId().getVideoId())) {
                downloadVideo(searchResult);
                break;
            }
        }
    }

    private static void downloadVideo(SearchResult searchResult) {
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

    private static boolean isDownloadApproved() throws IOException {

        logger.info("\nApprove Download Videos: YES/NO");
        String inputString = getInputString();
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

    private static void displaySearchResults(List<SearchResult> finalSearchResultList) {
        for (SearchResult searchResult : finalSearchResultList) {
            logger.info("VideoId: {}, Title: {}", searchResult.getId().getVideoId(), searchResult.getSnippet().getTitle());
        }
    }

    private static int getInputDaysToSearch() throws IOException {
        String inputQuery;

        logger.info("\nPlease enter number of days to search: ");
        inputQuery = getInputString();

        if (inputQuery.length() < 1) {
            inputQuery = "5";
        }
        return Integer.parseInt(inputQuery);
    }

    private static void addSearchFilters(Search youtubeSearch) throws IOException {
        try {
            YouTube.Search.List searchObject = youtubeSearch.createSearchObject();
            // Format for input
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            DateTime jodatime = dtf.parseDateTime(getCurrentTimeStamp());
            String dateTime = dtf.print(jodatime.minusDays(noOfDaysToSearch));
            DateTime pdateTime1 = dtf.parseDateTime(dateTime);

            searchObject.setPublishedAfter(new com.google.api.client.util.DateTime(pdateTime1.toDate()));
            searchObject.setVideoDuration("any");//Allowed values: [any, long, medium, short]

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    private static void findAndFilterVideos(List<SearchResult> finalSearchResultList, Search ySearch) {
        logger.info("Called findAndFilterVideos");

        List<SearchResult> searchResults = ySearch.find(searchQuery);
        for (SearchResult searchResult : searchResults) {
            String title = searchResult.getSnippet().getTitle();
            if (!isFileAlreadyDownloded(searchResult.getId().getVideoId()) && !findIfAddedToList(searchResult.getId().getVideoId(), finalSearchResultList)) {
                finalSearchResultList.add(searchResult);
            }
        }
        if (finalSearchResultList.size() <= videosToDownload.get()) {
            logger.debug("Size of finalSearchResultList equals videoToDownload");
            return;
        }
        logger.info("Size of finalSearchResultList {} ", finalSearchResultList.size());
        findAndFilterVideos(finalSearchResultList, ySearch);
    }

    private static boolean findIfAddedToList(String videoId, List<SearchResult> finalSearchResultList) {
        for (SearchResult searchResult : finalSearchResultList) {
            if (searchResult.getId().getVideoId().equals(videoId)) {
                return true;
            }
        }
        return false;
    }

    private static String filterRecords(List<SearchResult> searchResults) throws IOException {
        String inputQuery;

        logger.info("Please enter Video Id to Download: ");
        inputQuery = getInputString();
        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
    }

    private static String getInputString() throws IOException {
        String inputQuery;
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();
        return inputQuery;
    }

    private static int getInputVideosToDownload() throws IOException {
        String inputQuery;

        logger.info("Please enter Number of Videos to retrieve: ");
        inputQuery = getInputString();

        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "5";
        }
        return Integer.parseInt(inputQuery);
    }

    private static boolean isFileAlreadyDownloded(String videoId) {
        String fileName = null;
        try {
            fileName = ConfigReader.getInstance().getPropertyValue("download.directory") + "\\" + ConfigReader.getInstance().getPropertyValue("config.filename");
            return FileUtils.readFileToString(new File(fileName)).contains(videoId);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return false;
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
        for (File file : listFiles != null ? listFiles : new File[0])
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

        logger.info("Please enter a search term: ");
        inputQuery = getInputString();

        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
    }
}
