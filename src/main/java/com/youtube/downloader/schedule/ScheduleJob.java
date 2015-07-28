package com.youtube.downloader.schedule;

import com.dellnaresh.util.Constants;
import com.google.api.services.youtube.model.SearchResult;
import com.youtube.downloader.util.Utility;
import com.youtube.indianmovies.data.Search;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nareshm on 11/07/2015.
 */
public class ScheduleJob implements Job {
    public static final String SEARCH_QUERY = "Telugu Movies";
    public static final int NUMBER_OF_VIDEOS_RETURNED = 5;
    private Logger logger = LoggerFactory.getLogger(ScheduleJob.class.getName());
    private final static Map<String,String> downloadingMap=new ConcurrentHashMap<>();
    private final static Map<Integer,Constants.VIDEO_LENGTH> videoLengthMap=new HashMap<>();
    private static Constants.VIDEO_LENGTH videoLengthFilter= Constants.VIDEO_LENGTH.LONG;
    public ScheduleJob(){
        videoLengthMap.put(1, Constants.VIDEO_LENGTH.ANY);
        videoLengthMap.put(2, Constants.VIDEO_LENGTH.LONG);
        videoLengthMap.put(3, Constants.VIDEO_LENGTH.MEDIUM);
        videoLengthMap.put(4, Constants.VIDEO_LENGTH.SHORT);
    }
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("Job run started");
        try {
            startSearchAndDownload();
        } catch (IOException e) {
            logger.error("Exception during schedule download");
            e.printStackTrace();
        }
        logger.info("Job ran successfully");
    }

    private void startSearchAndDownload() throws IOException {
        Search search = new Search();
        Search.setNumberOfVideosReturned(NUMBER_OF_VIDEOS_RETURNED);
        addFilters(search);
        List<SearchResult> finalSearchResultList = new ArrayList<>();
        Utility.findAndFilterVideos(finalSearchResultList, search, SEARCH_QUERY, NUMBER_OF_VIDEOS_RETURNED);
        Utility.displaySearchResults(finalSearchResultList);
        int downloadCount=0;
        for(SearchResult searchResult:finalSearchResultList){
        if(downloadingMap.containsKey(searchResult.getId().getVideoId())|| downloadingMap.containsValue(searchResult.getSnippet().getTitle())) {
            continue;
        }
            downloadCount++;
            Utility.downloadVideo(searchResult);
            downloadingMap.put(searchResult.getId().getVideoId(), searchResult.getSnippet().getTitle());
        }
        if(downloadCount==0){
            int i = randInt(1, 4);
            videoLengthFilter=videoLengthMap.get(i);
            logger.info("Haven't downloaded any video");
        }

    }
    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    private void addFilters(Search searchObject) throws IOException {
        logger.debug("Value of videoLengthFilter {}",videoLengthFilter);
        Utility.addSearchFilters(searchObject, 10, videoLengthFilter);
    }
}
