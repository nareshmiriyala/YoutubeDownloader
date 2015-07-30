
package com.youtube.indianmovies.data;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.youtube.downloader.filters.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Print a list of videos matching a search term.
 *
 * @author Jeremy Walker
 */
public class Search {

    /**
     * Define a global variable that identifies the name of a file that
     * contains the developer's API key.
     */

    private static final Logger logger = LoggerFactory.getLogger(Search.class);

    private YouTube.Search.List search;

    private SearchFilter searchFilter;

    public Search(SearchFilter searchFilter) {
        this.search = createSearchObject();
        this.searchFilter=searchFilter;
    }

    /**
     * Initialize a YouTube object to search for videos on YouTube. Then
     * display the name and thumbnail image of each video in the result set.
     *
     * @param searchQuery
     */
    public List<SearchResult> find(String searchQuery) throws IOException {
        List<SearchResult> searchResultList = null;
        try {
            search.setQ(searchQuery);
            addSearchFilters();
            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            searchResultList = searchResponse.getItems();
        } catch (GoogleJsonResponseException e) {
            logger.error("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            logger.error("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            logger.error("Error is:" + t);
        }
        return searchResultList;
    }

    public void addSearchFilters() throws IOException {
        searchFilter.addCommonFilters(search);
    }

    public YouTube.Search.List createSearchObject() {
        YouTube youtube = YoutubeBuilder.getInstance().getYouTube();
        // Define the API request for retrieving search results.
        try {
            search = youtube.search().list("id,snippet");
        } catch (IOException e) {
            logger.error("Exception during creation of search object {}", e.getMessage());
        }
        return search;
    }
}
