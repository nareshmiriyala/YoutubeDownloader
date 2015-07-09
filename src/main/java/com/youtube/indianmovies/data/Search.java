
package com.youtube.indianmovies.data;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
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
    private static final String PROPERTIES_FILENAME = "youtube.properties";

    private static final Logger logger = LoggerFactory.getLogger(Search.class);

    private static long numberOfVideosReturned;

    private YouTube.Search.List search;

    public static void setNumberOfVideosReturned(long numberOfVideosReturned) {
        Search.numberOfVideosReturned = numberOfVideosReturned;
    }

    public YouTube.Search.List getSearch() {
        return search;
    }

    public void setSearch(YouTube.Search.List search) {
        this.search = search;
    }

    /*
         * Prints out all results in the Iterator. For each result, print the
         * title, video ID, and thumbnail.
         *
         * @param iteratorSearchResults Iterator of SearchResults to print
         *
         * @param query Search query (String)
         */
    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        logger.debug("\n=============================================================");
        logger.debug(
                "   First " + numberOfVideosReturned + " videos for search on \"" + query + "\".");
        logger.debug("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            logger.warn(" There aren't any results for your query.");
        }
        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
//            if (rId.getKind().equals("youtube#video")) {
//                System.out.print("** Video Id:" + rId.getVideoId() + " ");
//                System.out.print("** Title: " + singleVideo.getSnippet().getTitle());
//                System.out.println();
//            }
        }
        logger.debug("\n-------------------------------------------------------------\n");

    }


    /**
     * Initialize a YouTube object to search for videos on YouTube. Then
     * display the name and thumbnail image of each video in the result set.
     *
     * @param searchQuery
     */
    public List<SearchResult> find(String searchQuery) {
        // Read the developer key from the properties file.
        Properties properties = new Properties();
        List<SearchResult> searchResultList = null;
        try {
            InputStream in = Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            logger.error("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }

        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
             /*
      Define a global instance of a Youtube object, which will be used
      to make YouTube Data API requests.
     */
            // Set your developer key from the {{ Google Cloud Console }} for
            // non-authenticated requests. See:
            // {{ https://cloud.google.com/console }}
            String apiKey = properties.getProperty("youtube.apikey");
            search.setKey(apiKey);
            search.setQ(searchQuery);


            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(numberOfVideosReturned);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                prettyPrint(searchResultList.iterator(), searchQuery);
            }
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

    public YouTube.Search.List createSearchObject() throws IOException {
        YouTube youtube = YoutubeBuilder.getInstance().getYouTube();

        // Define the API request for retrieving search results.
        search= youtube.search().list("id,snippet");
        return search;
    }
}
