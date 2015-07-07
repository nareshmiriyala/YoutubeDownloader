package com.youtube.downloader;

import com.google.api.services.youtube.model.SearchResult;
import com.youtube.indianmovies.data.Search;
import com.youtube.workerpool.WorkerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nareshm on 8/03/2015.
 */
public class ConcurrentDownloader {
    private static final Logger logger= LoggerFactory.getLogger(ConcurrentDownloader.class);
   private static String path = "C:\\Naresh Data\\Development Software\\Videos\\Movies";
    private static AtomicInteger videosToDownload = new AtomicInteger(10);
  private static  String searchQuery = null;

    public static void main(String[] args) throws IOException {
        Search youtubeSearch = new Search();
        AtomicInteger count = new AtomicInteger(0);
        int inDownload = 0;
        try {
            logger.info("Entered search Query {}",searchQuery);
            searchQuery = getInputQuery();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inDownload = getInputVideosToDownload();
            if (inDownload > 0) {
                videosToDownload.set(inDownload);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            Search.setNumberOfVideosReturned(videosToDownload.get());
            List<SearchResult> finalSearchResultList=new ArrayList<>();
            findAndFilterVideos(finalSearchResultList,youtubeSearch);
            logger.info("Final Videos being Downloaded size {}", finalSearchResultList.size());

            WorkerPool.getInstance();
            for(SearchResult searchResult:finalSearchResultList){
                    String url = createURL(searchResult.getId().getVideoId());
                    String name = searchResult.getSnippet().getTitle();
                    //don't download file if its in the directory
                        final DownloadJob downloadJob = new DownloadJob("Download Job:" + url);
                        downloadJob.setFileDownloadPath(path);
                        downloadJob.setUrlToDownload(url);
                        downloadJob.setTitle(name);
                        count.incrementAndGet();
                        System.out.println("Download progress 1:" + downloadJob.getDownloadProgress());
                        WorkerPool.deployJob(downloadJob);
            }
           
            if (count.get() == 0) {
                videosToDownload.addAndGet(inDownload);
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

    private static void findAndFilterVideos(List<SearchResult> finalSearchResultList,Search ySearch) {
        logger.info("Called findAndFilterVideos");
        List<SearchResult> searchResults = ySearch.find(searchQuery);
        logger.info("Search Results list size {}",searchResults.size());
        for(SearchResult searchResult:searchResults){
            String title = searchResult.getSnippet().getTitle();
            if(!isFileExists(title,path) && !findIfAddedToList(searchResult.getId().getVideoId(),finalSearchResultList) ){
                logger.info("{} doesn't exists adding it to list",title);

                finalSearchResultList.add(searchResult);
            }
        }
        if(finalSearchResultList.size()<=videosToDownload.get()){
            logger.info("Size of finalSearchResultList equals videoToDownload");
            return;
        }
        logger.info("Size of finalSearchResultList {} ",finalSearchResultList.size());
        findAndFilterVideos(finalSearchResultList,ySearch);
    }

    private static boolean findIfAddedToList(String videoId, List<SearchResult> finalSearchResultList) {
        for(SearchResult searchResult:finalSearchResultList){
            if(searchResult.getId().getVideoId().equals(videoId)){
                return true;
            }
        }
        return false;
    }

    private static String filterRecords(List<SearchResult> searchResults) throws IOException {
        String inputQuery;

        System.out.print("Please enter Video Id to Download: ");
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

        System.out.print("Please enter Number of Videos to retrieve: ");
        inputQuery = getInputString();

        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "5";
        }
        return Integer.parseInt(inputQuery);
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

        System.out.print("Please enter a search term: ");
        inputQuery = getInputString();

        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
    }
}
