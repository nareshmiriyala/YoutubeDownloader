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

import static org.junit.Assert.*;

/**
 * Created by nareshm on 13/07/2015.
 */
@RunWith(PowerMockRunner.class)
public class UtilityTest {

    private Utility utility;

    private Search mockSearch;

    private SearchResult mockSearchResult;


    @Before
    public void setUp() throws Exception {
        utility= PowerMockito.spy(new Utility());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testAddSearchFilters() throws Exception {
        mockSearch=new Search();
        utility.addSearchFilters(mockSearch,10,"any");
    }

    @Test
    public void testGetInputString() throws Exception {

    }

    @Test
    public void testCreateURL() throws Exception {
        String url = utility.createURL("1223");
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
        utility.isFileAlreadyDownloaded(mockSearchResult);
    }

    @Test
    public void testFindAndFilterVideos() throws Exception {

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