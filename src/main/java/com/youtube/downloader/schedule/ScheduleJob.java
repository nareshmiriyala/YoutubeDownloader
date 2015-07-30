package com.youtube.downloader.schedule;

import com.dellnaresh.util.Constants;
import com.google.api.services.youtube.model.SearchResult;
import com.youtube.downloader.filters.TeluguHDLongMoviesFilter;
import com.youtube.downloader.util.Utility;
import com.youtube.indianmovies.data.Search;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nareshm on 11/07/2015.
 */
public class ScheduleJob implements Job {
    public static final String SEARCH_QUERY = "Telugu Movies";
    public static final int NUMBER_OF_VIDEOS_TO_DOWNLOAD = 500;
    private Logger logger = LoggerFactory.getLogger(ScheduleJob.class.getName());
    private final static Map<String, String> downloadingMap = new ConcurrentHashMap<>();
    private static Constants.VIDEO_LENGTH videoLengthFilter = Constants.VIDEO_LENGTH.LONG;

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

        List<SearchResult> finalSearchResultList = new ArrayList<>();
        Utility.setSearchQueryRetryCount(0);
        Utility.findAndFilterVideos(finalSearchResultList,SEARCH_QUERY, NUMBER_OF_VIDEOS_TO_DOWNLOAD);
        Utility.displaySearchResults(Utility.removeDuplicateVideos(Utility.getVideosMap(finalSearchResultList)));
        for (SearchResult searchResult : finalSearchResultList) {
            if (downloadingMap.containsKey(searchResult.getId().getVideoId()) || downloadingMap.containsValue(searchResult.getSnippet().getTitle())) {
                continue;
            }
            Utility.downloadVideo(searchResult);
            downloadingMap.put(searchResult.getId().getVideoId(), searchResult.getSnippet().getTitle());
        }
        if (downloadingMap.size() == 0) {
            logger.info("Haven't downloaded any video");
        }

    }

}
