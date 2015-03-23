package com.youtube.downloader;

import com.youtube.workerpool.WorkerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by nareshm on 8/03/2015.
 */
public class DownloadJob extends WorkerThread {
    private static Logger logger = LoggerFactory.getLogger(DownloadJob.class);
    String urlToDownload;
    String fileDownloadPath;
    String title;
    private double downloadProgress;
    private AppManagedDownload appManagedDownload;

    public DownloadJob(String s, String urlToDownload, String fileDownloadPath, String title) {
        super(s);
        this.urlToDownload = urlToDownload;
        this.fileDownloadPath = fileDownloadPath;
        this.title = title;
        this.appManagedDownload = new AppManagedDownload();
    }

    public DownloadJob(String s) {
        super(s);
        this.appManagedDownload = new AppManagedDownload();
    }

    public double getDownloadProgress() {
        Thread progressUpdate = new Thread(new Runnable() {
            @Override
            public void run() {
                downloadProgress = appManagedDownload.getDownloadStatus();

            }
        });
        progressUpdate.start();
        downloadProgress=round(downloadProgress, 3);
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
        logger.info("Downloading ULR:" + this.urlToDownload + " to path:" + this.fileDownloadPath);
        appManagedDownload.run(this.urlToDownload, new File(fileDownloadPath), this.title);


    }
     public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
