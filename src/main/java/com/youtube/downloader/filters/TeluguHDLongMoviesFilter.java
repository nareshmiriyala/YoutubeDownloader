package com.youtube.downloader.filters;

import com.dellnaresh.util.Constants;
import com.google.api.services.youtube.YouTube;

/**
 * Created by nareshm on 30/07/2015.
 */
public class TeluguHDLongMoviesFilter extends SearchFilter  {
    @Override
    protected void addExtraFilters(YouTube.Search.List search) {
        search.setVideoType(Constants.VIDEO_TYPE.MOVIE.getVideoType());
        search.setRelevanceLanguage(Constants.RELEVANCE_LANGUAGE.TE.getValue());
        search.setVideoDefinition(Constants.VIDEO_DEFINITION.HIGH.getValue());
        search.setSafeSearch(Constants.SAFE_SEARCH.MODERATE.getValue());
        search.setOrder(Constants.ORDER.VIEW_COUNT.getValue());
        search.setVideoDuration(Constants.VIDEO_LENGTH.LONG.getLength());
    }
}
