package com.youtube.indianmovies.data;

import org.junit.Test;

public class SearchTest {

    Search search = new Search();

    @Test
    public void testFind() throws Exception {
        Search.setNumberOfVideosReturned(20);
        search.find("Chiranjeevi");
        search.find("pawan");
    }
}