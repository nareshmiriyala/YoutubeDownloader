package com.youtube.downloader.util;

import com.dellnaresh.util.Constants;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.youtube.indianmovies.data.Search;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by nareshm on 13/07/2015.
 */
@RunWith(PowerMockRunner.class)
public class UtilityTest {


    private Search mockSearch;

    private SearchResult mockSearchResult;


    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetInputString() throws Exception {

    }

    @Test
    public void testCreateURL() throws Exception {
        String url = Utility.createURL("1223");
        assertEquals(Constants.YOUTUBE_URL_START.concat("1223"),url);
    }

    @Test
    public void testGetPropertyValue() throws Exception {

    }

    @Test
    public void testIsFileAlreadyDownloaded() throws Exception {
        mockSearchResult=new SearchResult();
        SearchResultSnippet mockSearchResultSnippet=new SearchResultSnippet();
        mockSearchResultSnippet.setTitle("Hello_Naresh");
        mockSearchResult.setSnippet(mockSearchResultSnippet);
        Utility.isFileAlreadyDownloaded(mockSearchResult);
    }

    @Test
    public void testFindAndFilterVideos() throws Exception {
        List<SearchResult> finalSearchResultList=new ArrayList<>();
        String searchQuery="Telugu Movies";
        Utility.setSearchQueryRetryCount(0);
        Utility.findAndFilterVideos(finalSearchResultList, searchQuery, 500);
        Utility.displaySearchResults(Utility.removeDuplicateVideos(Utility.getVideosMap(finalSearchResultList)));
    }

    @Test
    public void testDownloadVideo() throws Exception {

    }

    @Test
    public void testDownloadVideo1() throws Exception {

    }

    @Test
    public void testDisplaySearchResults() throws Exception {

    }

    @Test
    public void testShutDownPool() throws Exception {

    }
}