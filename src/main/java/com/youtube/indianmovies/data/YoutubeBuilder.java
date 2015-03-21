package com.youtube.indianmovies.data;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.youtube.indianmovies.commandline.Auth;

import java.io.IOException;

/**
 * Created by nareshm on 21/03/2015.
 */
public class YoutubeBuilder {
    private volatile static YoutubeBuilder youtubeBuilder = null;
    private static YouTube youTube;

    private YoutubeBuilder() {

    }

    public static YoutubeBuilder getInstance() {
        if (youtubeBuilder == null) {
            synchronized (YoutubeBuilder.class) {
                if (youtubeBuilder == null) {
                    youtubeBuilder = new YoutubeBuilder();
                    if (youTube == null) {
                        youTube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
                            @Override
                            public void initialize(HttpRequest request) throws IOException {
                            }
                        }).setApplicationName("youtube-app").build();
                    }
                }
            }
        }
        return youtubeBuilder;
    }

    public YouTube getYouTube() {
        return youTube;
    }

    public void setYouTube(YouTube youTube) {
        YoutubeBuilder.youTube = youTube;
    }

}
