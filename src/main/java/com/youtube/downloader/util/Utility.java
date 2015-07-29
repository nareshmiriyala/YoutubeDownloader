package com.youtube.downloader.util;

import com.dellnaresh.util.Constants;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.youtube.downloader.biz.DownloadJob;
import com.youtube.indianmovies.data.Search;
import com.youtube.workerpool.WorkerPool;
import info.debatty.java.stringsimilarity.Damerau;
import info.debatty.java.stringsimilarity.JaroWinkler;
import org.apache.commons.io.FileUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by nareshm on 11/07/2015.
 *
 */
public class Utility {
    private static final Logger logger = LoggerFactory.getLogger(Utility.class.getName());
    private static String path = Utility.getPropertyValue("download.directory");
    private static Constants.VIDEO_LENGTH inputVideoLength = Constants.VIDEO_LENGTH.ANY;
    private static int inputNoOfDaysToSearch;
    private static int searchQueryRetryCount = 0;
    private static int tempSearchSize = 0;

    public static void setSearchQueryRetryCount(int searchQueryRetryCount) {
        Utility.searchQueryRetryCount = searchQueryRetryCount;
    }

    public static void addSearchFilters(Search youtubeSearch, int noOfDaysToSearch, Constants.VIDEO_LENGTH videoLength) throws IOException {
        try {
            YouTube.Search.List searchObject = youtubeSearch.createSearchObject();
            // Format for input
            //check https://developers.google.com/youtube/v3/docs/search/list#type
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            logger.debug("noOfDaysToSearch Value {} and inputNoOfDasyToSearch value {}", noOfDaysToSearch, inputNoOfDaysToSearch);
            String dateTime = dtf.print(dtf.parseDateTime(getCurrentTimeStamp()).minusDays(noOfDaysToSearch));
            searchObject.setPublishedAfter(new com.google.api.client.util.DateTime(dtf.parseDateTime(dateTime).toDate()));
            inputVideoLength = videoLength;
            inputNoOfDaysToSearch = noOfDaysToSearch;
            searchObject.setRegionCode("IN");
            searchObject.setVideoType("movie");
            searchObject.setRelevanceLanguage("te");
            searchObject.setVideoDefinition("high");
            searchObject.setSafeSearch("moderate");
            searchObject.setOrder("viewCount");
            searchObject.setVideoDuration(videoLength.getLength());//Allowed values: [any, long, medium, short]

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
        if (bReader != null) {
            bReader.close();
        }
        return inputQuery;
    }

    public static String createURL(String videoId) {
        return Constants.YOUTUBE_URL_START.concat(videoId);
    }

    public static String getPropertyValue(String property) {
        try {
            return ConfigReader.getInstance().getPropertyValue(property);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isFileAlreadyDownloaded(SearchResult searchResult) {
        String fileName = null;
        try {
            fileName = getPropertyValue("download.directory") + "\\" + ConfigReader.getInstance().getPropertyValue("config.filename");
            if (isFileExistsInFolder(searchResult)) {
                return true;
            }
            File downloadHistoryFile = new File(fileName);
            if (!downloadHistoryFile.exists()) {
                downloadHistoryFile.createNewFile();
            }

            return FileUtils.readFileToString(downloadHistoryFile).contains(searchResult.getId().getVideoId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void findAndFilterVideos(List<SearchResult> finalSearchResultList, Search ySearch, String searchQuery, int videosToDownload) throws IOException {
        logger.debug("Called findAndFilterVideos  searchQuery {} and no of videos to download {}", searchQuery, videosToDownload);

        List<SearchResult> searchResults = ySearch.find(searchQuery);
        logger.debug("Search result size {} and noofDaysToSearch is {}", searchResults.size(), inputNoOfDaysToSearch);
        for (SearchResult searchResult : searchResults) {
            if (!isFileAlreadyDownloaded(searchResult) && !findIfAddedToList(searchResult.getId().getVideoId(), finalSearchResultList)) {
                finalSearchResultList.add(searchResult);
            }
        }
        if (tempSearchSize == finalSearchResultList.size()) {
            searchQueryRetryCount++;
        }
        if (searchQueryRetryCount >= 50) {
            logger.info("Stop searching search retry count {} reached", searchQueryRetryCount);
            return;
        }
        tempSearchSize = finalSearchResultList.size();

        if (finalSearchResultList.size() >= videosToDownload) {

            logger.debug("Size of finalSearchResultList equals videoToDownload");
            return;
        }
        //increment search days by 10
        inputNoOfDaysToSearch = inputNoOfDaysToSearch + 1;
        addSearchFilters(ySearch, inputNoOfDaysToSearch, inputVideoLength);
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
    public static Map<String,String> removeDuplicateVideos(Map<String,String> videoMap){
        Set<String> valueSet=new TreeSet<>(videoMap.values());
        Iterator<String> iterator=valueSet.iterator();
        Map<String,String> uniqueMap=new HashMap<>();
        while (iterator.hasNext()) {
            String value = iterator.next();
            videoMap.entrySet().stream().filter(e -> value.equals(e.getValue()) && !uniqueMap.containsValue(value)).forEach(e -> {
                uniqueMap.put(e.getKey(), value);
            });

        }
        return uniqueMap;
    }

    public static Map<String,String> getVideosMap(List<SearchResult> searchResults) {
        Map<String,String> originalVideo=new HashMap<>();
        for(SearchResult result:searchResults){
            originalVideo.put(result.getId().getVideoId(),result.getSnippet().getTitle());
        }
        return originalVideo;
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
        logger.debug("Download progress {}", downloadJob.getDownloadProgress());
        WorkerPool.deployJob(downloadJob);
    }

    public static void displaySearchResults(List<SearchResult> finalSearchResultList) {
        Collections.sort(finalSearchResultList, new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult o1, SearchResult o2) {
                return o1.getSnippet().getTitle().compareTo(o2.getSnippet().getTitle());
            }
        });
        int count = 0;
        for (SearchResult searchResult : finalSearchResultList) {
            logger.info("No {} VideoId: {}, Title: {}", ++count, searchResult.getId().getVideoId(), searchResult.getSnippet().getTitle());
        }
    }
    public static void displaySearchResults(Map<String,String> videoMap) {
        Map<String, String> sortedMap = sortByValue(videoMap);
        int count = 0;
        for (Map.Entry<String,String> entry : sortedMap.entrySet()) {
            logger.info("No {},Title: {}", ++count, entry.getValue());
        }
    }
    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map )
    {
        Map<K,V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K,V>> st = map.entrySet().stream();

        st.sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(e ->result.put(e.getKey(),e.getValue()));

        return result;
    }
    public static void shutDownPool() {
        try {
            WorkerPool.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean isFileExistsInFolder(SearchResult searchResult) {
        File folder = new File(getPropertyValue("download.directory"));
        String videoTitle = searchResult.getSnippet().getTitle();
        Damerau d = new Damerau();
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String name = listOfFiles[i].getName();
                logger.debug("file {} name is {}", i, name);
                if (getJaroWinkerSimilarity(videoTitle,name) > .85) {
                    return true;
                }
            }

        }
        return false;
    }
    private static double getJaroWinkerSimilarity(String video1,String video2){
        JaroWinkler janWinkler = new JaroWinkler();
        double similarity = janWinkler.similarity(video1, video2);
        logger.debug("JaroWinkler score of {} and {} is {}", video1, video2,similarity );
        return similarity ;
    }
}

