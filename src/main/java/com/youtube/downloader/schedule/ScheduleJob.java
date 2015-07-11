package com.youtube.downloader.schedule;

import com.google.api.services.youtube.model.SearchResult;
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

/**
 * Created by nareshm on 11/07/2015.
 */
public class ScheduleJob implements Job {
    public static final String SEARCH_QUERY = "Ownage Pranks";
    public static final int NUMBER_OF_VIDEOS_RETURNED = 1;
    private Logger logger = LoggerFactory.getLogger(ScheduleJob.class.getName());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("Job run started");
        try {
            startSearchAndDownload();
            Utility.shutDownPool();
        } catch (IOException e) {
            logger.error("Exception during schedule download");
            e.printStackTrace();
        }
        logger.info("Job ran successfully");
    }

    private void startSearchAndDownload() throws IOException {
        Search search = new Search();
        search.setNumberOfVideosReturned(NUMBER_OF_VIDEOS_RETURNED);
        search.createSearchObject();
        addFilters(search);
        List<SearchResult> finalSearchResultList = new ArrayList<>();
        Utility.findAndFilterVideos(finalSearchResultList, search, SEARCH_QUERY, NUMBER_OF_VIDEOS_RETURNED);
        Utility.displaySearchResults(finalSearchResultList);
        if (finalSearchResultList.size() == 1) {
            Utility.downloadVideo(finalSearchResultList.get(0));
        }

    }

    private void addFilters(Search searchObject) throws IOException {
        Utility.addSearchFilters(searchObject, 10, "long");
    }
}
