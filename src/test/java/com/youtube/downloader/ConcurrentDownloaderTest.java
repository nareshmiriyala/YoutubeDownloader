package com.youtube.downloader;

import com.youtube.downloader.biz.ConcurrentDownloader;
import com.youtube.downloader.biz.DownloadThread;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.*;

/**
 * Created by nareshm on 9/07/2015.
 */
@RunWith(PowerMockRunner.class)
public class ConcurrentDownloaderTest {
    public static final String VIDEO_ID = "1234fdfd";
    private ConcurrentDownloader concurrentDownloader;
    @Before
    public void setUp() throws Exception {

        concurrentDownloader= PowerMockito.spy(new ConcurrentDownloader());

    }

    @Test
    public void testFindAndFilterVideos() throws Exception{
        createFile();
        Object isFileAlreadyDownloded = Whitebox.invokeMethod(concurrentDownloader, "isFileAlreadyDownloded", VIDEO_ID);
        assertTrue((Boolean) isFileAlreadyDownloded);
    }

    private void createFile() throws Exception {
        Whitebox.invokeMethod(PowerMockito.spy(new DownloadThread()),"updateDownloadRecord", VIDEO_ID,"Naresh Test");
    }
}