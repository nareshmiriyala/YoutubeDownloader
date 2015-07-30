package com.youtube.downloader.biz;

import com.dellnaresh.videodownload.VideoDownload;
import com.dellnaresh.videodownload.info.VideoInfo;
import com.dellnaresh.videodownload.info.VideoParser;
import com.dellnaresh.videodownload.vhs.YouTubeQParser;
import com.youtube.workerpool.WorkerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nareshm on 8/03/2015.
 */
public class DownloadJob extends WorkerThread {
    private static Logger logger = LoggerFactory.getLogger(DownloadJob.class);
    private String urlToDownload;
    private String fileDownloadPath;
    private String title;
    private double downloadProgress;
    private DownloadThread downloadThread;
    private boolean failedDownload = false;
    private String videoId;

    public DownloadJob(String s) {
        super(s);
        downloadThread=new DownloadThread();
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public double getDownloadProgress() {
        Thread progressUpdate = new Thread(new Runnable() {
            @Override
            public void run() {
                downloadProgress = downloadThread.getDownloadStatus();

            }
        });
        progressUpdate.start();
        downloadProgress = round(downloadProgress, 3);
        return downloadProgress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFileDownloadPath(String fileDownloadPath) {
        this.fileDownloadPath = fileDownloadPath;
    }

    public void setUrlToDownload(String urlToDownload) {
        this.urlToDownload = urlToDownload;
    }

    @Override
    public void processCommand() {
        logger.info("Downloading ULR:{} to path:{}" ,this.urlToDownload , this.fileDownloadPath);
        startDownload(this.urlToDownload, new File(fileDownloadPath), this.title,this.videoId);
        if (downloadThread.isFailedDownload()) {
            failedDownload = true;
        }

    }

    public void startDownload(String url, File path, String title,String videoId) {
        try {
            VideoInfo info = new VideoInfo(new URL(url));
            downloadThread.setVideoInfo(info);
            downloadThread.setTitle(title);
            downloadThread.setVideoId(videoId);
            AtomicBoolean stop = downloadThread.getStop();

            VideoParser user = new YouTubeQParser(info.getWebUrl(), VideoInfo.VideoQuality.p720);

            VideoDownload v = new VideoDownload(info, path);

            v.extractVideo(user, stop, downloadThread);

            logger.info("Title: {} ", info.getTitle());
            logger.info("Download URL: {} ", info.getDownloadInfo().getSource());

            v.downloadVideo(user, stop, downloadThread);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
