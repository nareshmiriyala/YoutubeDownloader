package com.youtube.downloader;

import com.dellnaresh.videodownload.VideoDownload;
import com.dellnaresh.videodownload.info.VideoInfo;
import com.dellnaresh.videodownload.info.VideoInfo.VideoQuality;
import com.dellnaresh.videodownload.info.VideoParser;
import com.dellnaresh.videodownload.vhs.YouTubeQParser;
import com.github.axet.wget.info.DownloadInfo;
import com.github.axet.wget.info.DownloadInfo.Part;
import com.github.axet.wget.info.DownloadInfo.Part.States;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AppManagedDownload {

    VideoInfo info;
    long last;

    public void run(String url, File path, final String title) {
        try {
            AtomicBoolean stop = new AtomicBoolean(false);
            final AtomicInteger retryCount = new AtomicInteger(0);
            Runnable notify =new Runnable() {
                @Override
                public void run() {

                VideoInfo i1 = info;
                DownloadInfo i2 = i1.getInfo();
                String s = Thread.currentThread().getName() + ":: " + title + ":: ";

                // notify app or save download state
                // you can extract information from DownloadInfo info;
                switch (i1.getState()) {
                    case EXTRACTING:
                    case EXTRACTING_DONE:
                    case DONE:
                        System.out.println(s + i1.getState() + " " + i1.getVideoQuality());
                        break;
                    case RETRYING:
                        System.out.println(s + i1.getState() + " " + i1.getDelay());
                        retryCount.incrementAndGet();
                        if (retryCount.get() > 10) {
                            System.out.println("Can't Download the Video");
                            throw new RuntimeException("Cant Download the file");
                        }
                        break;
                    case DOWNLOADING:
                        long now = System.currentTimeMillis();
                        if (now - 1000 > last) {
                            last = now;

                            String parts = "";

                            List<Part> pp = i2.getParts();
                            if (pp != null) {
                                // multipart download
                                for (Part p : pp) {
                                    if (p.getState().equals(States.DOWNLOADING)) {
                                        parts += String.format("Part#%d(%.2f) ", p.getNumber(), p.getCount()
                                                / (float) p.getLength());
                                    }
                                }
                            }

                            System.out.println(String.format("%s %.2f %s", s + i1.getState(),
                                    i2.getCount() / (float) i2.getLength(), parts));
                        }
                        break;
                    default:
                        break;
                }
            }};

            info = new VideoInfo(new URL(url));

            // [OPTIONAL] limit maximum quality, or do not call this function if
            // you wish maximum quality available.
            // if youtube does not have video with requested quality, program
            // will raise en exception.
            VideoParser user;

            // create simple youtube request
            //user = new YouTubeParser(info.getWeb());
            // download maximum video quality
            user = new YouTubeQParser(info.getWeb(), VideoQuality.p360);
            // download non webm only
            //user = new YouTubeMPGParser(info.getWeb(), VideoQuality.p480);

            VideoDownload v = new VideoDownload(info, path);

            // [OPTIONAL] call v.extract() only if you d like to get video title
            // before start download. or just skip it.
            v.extract(user, stop, notify);
            System.out.println(info.getTitle());

            v.download(user, stop, notify);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
