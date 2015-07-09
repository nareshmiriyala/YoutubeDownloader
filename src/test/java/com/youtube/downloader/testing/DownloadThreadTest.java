package com.youtube.downloader.testing;

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
public class DownloadThreadTest {

    private DownloadThread downloadThread;

    @Before
    public void setUp() throws Exception {
        downloadThread= PowerMockito.spy(new DownloadThread());
    }
    @Test
    public void testUpdateDownloadRecord()throws Exception{
        Whitebox.invokeMethod(downloadThread,"updateDownloadRecord","1234fdfd","Naresh Test");
    }
}