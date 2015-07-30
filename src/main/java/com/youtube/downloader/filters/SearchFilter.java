package com.youtube.downloader.filters;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.youtube.indianmovies.data.Search;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by nareshm on 30/07/2015.
 */
public abstract class SearchFilter {
    private static final Logger logger = LoggerFactory.getLogger(SearchFilter.class);

    private static final String PROPERTIES_FILENAME = "youtube.properties";

    private int noOfDaysToSearch=1;//default value to 1

    public int getNoOfDaysToSearch() {
        return noOfDaysToSearch;
    }

    public void setNoOfDaysToSearch(int noOfDaysToSearch) {
        this.noOfDaysToSearch = noOfDaysToSearch;
    }

    private DateTime getDateTimeFilter(){
        // Format for input
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = dtf.print(dtf.parseDateTime(getCurrentTimeStamp()).minusDays(noOfDaysToSearch));
        return new com.google.api.client.util.DateTime(dtf.parseDateTime(dateTime).toDate());

    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public void addCommonFilters(YouTube.Search.List search) throws IOException {
        // Restrict the search results to only include videos. See:
        // https://developers.google.com/youtube/v3/docs/search/list#type
        search.setType("video");
        String apiKey = getApiKey();
        search.setKey(apiKey);
        search.setMaxResults(50l);
        search.setRegionCode("IN");

        // To increase efficiency, only retrieve the fields that the
        // application uses.
        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
        search.setPublishedAfter(getDateTimeFilter());
        addExtraFilters(search);
    }

    protected abstract void addExtraFilters(YouTube.Search.List search);

    private String getApiKey() throws IOException {
        // Read the developer key from the properties file.
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            logger.error("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        } finally {
            if (in != null)
                in.close();
        }
        return properties.getProperty("youtube.apikey");
    }

}
