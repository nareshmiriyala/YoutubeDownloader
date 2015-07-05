package com.youtube.downloader;

import com.dellnaresh.videodownload.VideoDownload;
import com.dellnaresh.videodownload.info.VideoInfo;
import com.dellnaresh.videodownload.info.VideoParser;
import com.dellnaresh.videodownload.vhs.YouTubeQParser;
import com.dellnaresh.wget.info.DownloadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AppManagedDownload {

    VideoInfo info;
    long last;
    Logger logger = LoggerFactory.getLogger(AppManagedDownload.class);
    private double downloadStatus;
    private boolean cantDownload = false;

    public boolean isCantDownload() {
        return cantDownload;
    }

    public double getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(double downloadStatus) {
        this.downloadStatus = downloadStatus;
    }
    public void run(String url, File path, final String title) {
        try {
            AtomicBoolean stop = new AtomicBoolean(false);
            final AtomicInteger retryCount = new AtomicInteger(0);
            Runnable notify = new Runnable() {

                @Override
                public void run() {
                    VideoInfo i1 = info;
                    DownloadInfo i2 = i1.getDownloadInfo();
                    String s = Thread.currentThread().getName() + ":: " + title + ":: ";

                    // notify app or save downloadVideo state
                    // you can extractDownloadInfo information from DownloadInfo info;
                    switch (i1.getState()) {
                        case EXTRACTING:
                        case EXTRACTING_DONE:
                        case DONE:
                            downloadStatus = 1.00;
                            logger.info(s + i1.getState() + " " + i1.getVideoQuality());
                            logger.info("Successfully Downloaded");
                            logger.info("Download state {},Downloaded video quality {}", i1.getState(), i1.getVideoQuality());
                            break;
                        case RETRYING:
                            logger.debug(s + i1.getState() + " " + i1.getDelay());
                            retryCount.incrementAndGet();
                            if (retryCount.get() > 10) {
                                cantDownload = true;
                                logger.error("Can't Download the Video");
                                throw new RuntimeException("Cant Download the file");
                            }
                            break;
                        case STOP:
                            logger.error("Stopping download");
                            System.exit(0);
                            break;
                        case ERROR:
                            logger.error("Error during download");
                            System.exit(0);
                            break;
                        case DOWNLOADING:
                            long now = System.currentTimeMillis();
                            if (now - 1000 > last) {
                                last = now;

                                String parts = "";

                                List<DownloadInfo.Part> pp = i2.getParts();
                                if (pp != null) {
                                    // isMultiPart downloadVideo
                                    for (DownloadInfo.Part p : pp) {
                                        if (p.getState().equals(DownloadInfo.Part.States.DOWNLOADING)) {
                                            parts += String.format("Part#%d(%.2f) ", p.getNumber(), p.getCount()
                                                    / (float) p.getLength());
                                        }
                                    }
                                }
                                downloadStatus = (i2.getCount() / (float) i2.getLength());
                                setDownloadStatus(downloadStatus);
                                logger.info(String.format("%s %.2f %s", s + i1.getState(), downloadStatus, parts));
                            }
                            break;
                        default:
                            break;
                    }
                }
            };

            info = new VideoInfo(new URL(url));

            // [OPTIONAL] limit maximum quality, or do not call this function if
            // you wish maximum quality available.
            //
            // if youtube does not have video with requested quality, program
            // will raise en exception.
            VideoParser user;

            // create simple youtube request
            //user = new YouTubeParser(info.getWebUrl());
            // downloadVideo maximum video quality
            user = new YouTubeQParser(info.getWebUrl(), VideoInfo.VideoQuality.p360);
            // downloadVideo non webm only
            //user = new YouTubeMPGParser(info.getWebUrl(), VideoQuality.p480);

            VideoDownload v = new VideoDownload(info, path);

            // [OPTIONAL] call v.extractDownloadInfo() only if you d like to get video title
            // or downloadVideo url link
            // before start downloadVideo. or just skip it.
            v.extractVideo(user, stop, notify);

            logger.info("Title: {} ", info.getTitle());
            logger.info("Download URL: {} ", info.getDownloadInfo().getSource());

            v.downloadVideo(user, stop, notify);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
