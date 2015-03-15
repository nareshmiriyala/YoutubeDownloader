
package com.youtube.indianmovies.data;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.youtube.indianmovies.commandline.Auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private static long numberOfVideosReturned;
    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private static YouTube youtube;

    public static long getNumberOfVideosReturned() {
        return numberOfVideosReturned;
    }

    public static void setNumberOfVideosReturned(long numberOfVideosReturned) {
        Search.numberOfVideosReturned = numberOfVideosReturned;
    }

    /*
     * Prompt the user to enter a query term and return the user-specified term.
     */
    private static String getInputQuery() throws IOException {

        String inputQuery;

        System.out.print("Please enter a search term: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();

        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
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

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + numberOfVideosReturned + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }
        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {
                System.out.print("** Video Id:" + rId.getVideoId() + " ");
                System.out.print("** Title: " + singleVideo.getSnippet().getTitle());
                System.out.println();
            }
        }
        System.out.println("\n-------------------------------------------------------------\n");

    }


    /*
    * Print information about all of the items in the playlist.
    *
    * @param size size of list
    *
    * @param iterator of Playlist Items from uploaded Playlist
    */
    private static void prettyPrint(int size, Iterator<PlaylistItem> playlistEntries) {
        System.out.println("=============================================================");
        System.out.println("\t\tTotal Videos Uploaded: " + size);
        System.out.println("=============================================================\n");

        while (playlistEntries.hasNext()) {
            PlaylistItem playlistItem = playlistEntries.next();
            System.out.println(" video name  = " + playlistItem.getSnippet().getTitle());
            System.out.println(" video id    = " + playlistItem.getContentDetails().getVideoId());
            System.out.println(" upload date = " + playlistItem.getSnippet().getPublishedAt());
            System.out.println("\n-------------------------------------------------------------\n");
        }
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
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }

        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
            }).setApplicationName("youtube-cmdline-search-sample").build();

//            // Define a list to store items in the list of uploaded videos.
//            List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();
//
//            // Retrieve the playlist of the channel's uploaded videos.
//            YouTube.PlaylistItems.List playlistItemRequest =
//                    youtube.playlistItems().list("id,contentDetails,snippet");
//            playlistItemRequest.setPlaylistId("PLE7E8B7F4856C9B19");
//
//            // playlistItemRequest.setFields(
//            //     "items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");
//            String nextToken = "";
//
//            // Call the API one or more times to retrieve all items in the
//            // list. As long as the API response returns a nextPageToken,
//            // there are still more items to retrieve.
//            do {
//                playlistItemRequest.setPageToken(nextToken);
//                PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();
//
//                playlistItemList.addAll(playlistItemResult.getItems());
//
//                nextToken = playlistItemResult.getNextPageToken();
//            } while (nextToken != null);
//
//            // Prints information about the results.
//            prettyPrint(playlistItemList.size(), playlistItemList.iterator());
            // Prompt the user to enter a query term.
            //  String queryTerm = getInputQuery();

            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

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
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return searchResultList;
    }
}
