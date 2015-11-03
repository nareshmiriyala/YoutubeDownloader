package com.youtube.downloader.util;

import com.dellnaresh.util.Constants;
import com.google.api.services.youtube.model.SearchResult;
import com.youtube.downloader.biz.DownloadJob;
import com.youtube.downloader.filters.TeluguHDLongMoviesFilter;
import com.youtube.indianmovies.data.Search;
import com.youtube.workerpool.WorkerPool;
import info.debatty.java.stringsimilarity.Damerau;
import info.debatty.java.stringsimilarity.JaroWinkler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by nareshm on 11/07/2015.
 */
public class Utility {
    private static final Logger logger = LoggerFactory.getLogger(Utility.class.getName());
    private static String path = Utility.getPropertyValue("download.directory");
    private static Constants.VIDEO_LENGTH inputVideoLength = Constants.VIDEO_LENGTH.ANY;
    private static int noOfDaysToSearch=1;
    private static int searchQueryRetryCount = 0;
    private static int tempSearchSize = 0;

    public static void setSearchQueryRetryCount(int searchQueryRetryCount) {
        Utility.searchQueryRetryCount = searchQueryRetryCount;
    }


    public static String getInputString() throws IOException {
        String inputQuery=null;
        BufferedReader bReader=null;
             bReader = new BufferedReader(new InputStreamReader(System.in));
            if(bReader!=null)
            inputQuery = bReader.readLine();
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

    public static void findAndFilterVideos(List<SearchResult> searchResultList, String searchQuery, int videosToDownload) throws IOException {
        List<SearchResult> searchResults = getSearchResults(searchQuery, videosToDownload);
        logger.debug("Search result size {} and noofDaysToSearch is {}", searchResults.size(), noOfDaysToSearch);
        searchResults.stream().filter(searchResult -> !isFileAlreadyDownloaded(searchResult) && !findIfAddedToList(searchResult.getId().getVideoId(), searchResultList)).forEach(searchResultList::add);
        if (tempSearchSize == searchResultList.size()) {
            searchQueryRetryCount++;
        }
        if (searchQueryRetryCount >= 50) {
            logger.info("Stop searching search retry count {} reached", searchQueryRetryCount);
            return;
        }
        tempSearchSize = searchResultList.size();

        if (searchResultList.size() >= videosToDownload) {

            logger.debug("Size of searchResultList equals videoToDownload");
            return;
        }
        //increment search days by 1
        noOfDaysToSearch = noOfDaysToSearch + 1;
        logger.debug("Size of searchResultList {} ", searchResultList.size());
        findAndFilterVideos(searchResultList, searchQuery, videosToDownload);
    }

    private static List<SearchResult> getSearchResults(String searchQuery, int videosToDownload) throws IOException {
        TeluguHDLongMoviesFilter teluguHDLongMoviesFilter = new TeluguHDLongMoviesFilter();
        teluguHDLongMoviesFilter.setNoOfDaysToSearch(noOfDaysToSearch);
        Search search = new Search(teluguHDLongMoviesFilter);
        logger.debug("Called findAndFilterVideos  searchQuery {} and no of videos to download {}", searchQuery, videosToDownload);

        return search.find(searchQuery);
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

    public static Map<String, String> removeDuplicateVideos(Map<String, String> videoMap) {
        Set<String> valueSet = new TreeSet<>(videoMap.values());
        Iterator<String> iterator = valueSet.iterator();
        Map<String, String> uniqueMap = new HashMap<>();
        while (iterator.hasNext()) {
            String value = iterator.next();
            videoMap.entrySet().stream().filter(e -> value.equals(e.getValue()) && !uniqueMap.containsValue(value)).forEach(e -> {
                uniqueMap.put(e.getKey(), value);
            });

        }
        return uniqueMap;
    }

    public static Map<String, String> getVideosMap(List<SearchResult> searchResults) {
        Map<String, String> originalVideo = new HashMap<>();
        for (SearchResult result : searchResults) {
            originalVideo.put(result.getId().getVideoId(), result.getSnippet().getTitle());
        }
        return originalVideo;
    }

    public static void downloadVideo(SearchResult searchResult) {
        String videoId = searchResult.getId().getVideoId();
        downloadVideo(searchResult, videoId);
    }
    public static void downloadVideo(String videoId){
        downloadVideo(null,videoId);
    }
    private static void downloadVideo(SearchResult searchResult, String videoId) {
        WorkerPool.getInstance();
        String url = createURL(videoId);
        String name ="Video_Sample";
        if(searchResult!=null) {
            name = searchResult.getSnippet().getTitle();
        }
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

    public static void displaySearchResults(Map<String, String> videoMap) {
        Map<String, String> sortedMap = sortByValue(videoMap);
        int count = 0;
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            logger.info("No {},Title: {}", ++count, entry.getValue());
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();

        st.sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    public static void shutDownPool() {
        try {
            WorkerPool.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean isFileExistsInFolder(SearchResult searchResult) throws IOException {
        File folder = new File(getPropertyValue("download.directory"));
        if(!folder.exists()){
            folder.createNewFile();
        }
        String videoTitle = searchResult.getSnippet().getTitle();
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles!=null && listOfFiles.length!=0){

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String name = listOfFiles[i].getName();
                logger.debug("file {} name is {}", i, name);
                if (getJaroWinkerSimilarity(videoTitle, name) > .85) {
                    return true;
                }
            }

        }
        }
        return false;
    }

    private static double getJaroWinkerSimilarity(String video1, String video2) {
        JaroWinkler janWinkler = new JaroWinkler();
        double similarity = janWinkler.similarity(video1, video2);
        logger.debug("JaroWinkler score of {} and {} is {}", video1, video2, similarity);
        return similarity;
    }
}

