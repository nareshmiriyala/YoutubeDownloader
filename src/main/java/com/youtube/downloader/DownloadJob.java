package com.youtube.downloader;

import com.youtube.workerpool.WorkerThread;

import java.io.File;

/**
 * Created by nareshm on 8/03/2015.
 */
public class DownloadJob extends WorkerThread {
    String urlToDownload;
    String fileDownloadPath;
    String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileDownloadPath() {
        return fileDownloadPath;
    }

    public void setFileDownloadPath(String fileDownloadPath) {
        this.fileDownloadPath = fileDownloadPath;
    }

    public String getUrlToDownload() {
        return urlToDownload;
    }

    public void setUrlToDownload(String urlToDownload) {
        this.urlToDownload = urlToDownload;
    }

    public DownloadJob(String s) {
        super(s);
    }

    @Override
    public void processCommand() {
        AppManagedDownload appManagedDownload=new AppManagedDownload();
        System.out.println("Downloading ULR:"+this.urlToDownload+" to path:"+this.fileDownloadPath);
        appManagedDownload.run(this.urlToDownload,new File(fileDownloadPath),this.title);

    }

}
