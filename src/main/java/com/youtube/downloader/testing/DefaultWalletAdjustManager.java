package com.youtube.downloader.testing;

/**
 * Created by nareshm on 12/03/2015.
 */
public class DefaultWalletAdjustManager implements WalletAdjustManager {
    private volatile static DefaultWalletAdjustManager uniqueInstance;

    private DefaultWalletAdjustManager() {

    }

    public static DefaultWalletAdjustManager getInstance() {
        if (uniqueInstance == null) {
            synchronized (DefaultWalletAdjustManager.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new DefaultWalletAdjustManager();
                }
            }
        }
        return uniqueInstance;
    }

    @Override
    public void adjustWallet() {
        System.out.println("Default Adjust:" + this);
    }
}
