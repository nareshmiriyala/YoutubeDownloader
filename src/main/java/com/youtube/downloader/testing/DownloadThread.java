package com.youtube.downloader.testing;

import com.dellnaresh.videodownload.info.VideoInfo;
import com.dellnaresh.wget.info.DownloadInfo;
import com.youtube.downloader.config.ConfigReader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nareshm on 7/07/2015.
 */
public class DownloadThread implements Runnable {
    private VideoInfo info;
    private long last;
    private Logger logger = LoggerFactory.getLogger(DownloadThread.class);
    private AtomicBoolean stop = new AtomicBoolean(false);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private String title;
    private volatile double downloadStatus;
    private boolean isFailedDownload = false;
    private String videoId;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public DownloadThread() {

    }

    public DownloadThread(VideoInfo info, String title) {
        this.info = info;
        this.title = title;
    }

    public boolean isFailedDownload() {
        return isFailedDownload;
    }

    public double getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(double downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public VideoInfo getInfo() {
        return info;
    }

    public void setInfo(VideoInfo info) {
        this.info = info;
    }

    public AtomicInteger getRetryCount() {
        return retryCount;
    }

    public AtomicBoolean getStop() {
        return stop;
    }

    public void setStop(AtomicBoolean stop) {
        this.stop = stop;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFailedDownload(boolean failedDownload) {
        this.isFailedDownload = failedDownload;
    }

    @Override
    public void run() {
        VideoInfo i1 = info;
        DownloadInfo i2 = i1.getDownloadInfo();
        String s = "\n"+title + ":: ";

        // notify app or save downloadVideo state
        // you can extractDownloadInfo information from DownloadInfo info;
        switch (i1.getState()) {
            case EXTRACTING:
                logger.info("Download state {},Downloaded video quality {}", i1.getState(), i1.getVideoQuality());
                break;
            case EXTRACTING_DONE:
                logger.info("Download state {},Downloaded video quality {}", i1.getState(), i1.getVideoQuality());
                break;
            case DONE:
                downloadStatus = 1.00;
                logger.info(s + i1.getState() + " " + i1.getVideoQuality());
                logger.info("Successfully Downloaded");
                logger.info("Download state {},Downloaded video quality {}", i1.getState(), i1.getVideoQuality());
                updateDownloadRecord(videoId,title);
                break;
            case RETRYING:
                logger.debug(s + i1.getState() + " " + i1.getDelay());
                retryCount.incrementAndGet();
                if (retryCount.get() > 10) {
                    isFailedDownload = true;
                    logger.error("Can't Download the Video");
                    stop.set(true);
                }
                break;
            case STOP:
                logger.error("Stopping download");
                stop.set(true);
                break;
            case ERROR:
                logger.error("Error during download");
                isFailedDownload = true;
                stop.set(true);
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

    private void updateDownloadRecord(String videoId,String title) {
        String fileName = null;
        try {
            fileName = ConfigReader.getInstance().getPropertyValue("download.directory") + "\\" + ConfigReader.getInstance().getPropertyValue("config.filename");
            FileUtils.writeStringToFile(new File(fileName), "VideoId:"+videoId+" Title:"+title+"\n",true);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "DownloadThread{" +
                "info=" + info.toString() +
                ", stop=" + stop +
                ", retryCount=" + retryCount +
                ", title='" + title + '\'' +
                ", downloadStatus=" + downloadStatus +
                ", isFailedDownload=" + isFailedDownload +
                '}';
    }
}
