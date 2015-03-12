package com.youtube.downloader.testing;

/**
 * Created by nareshm on 12/03/2015.
 */
public class HVDDefaultWalletAdjustManager implements WalletAdjustManager {
    private volatile static HVDDefaultWalletAdjustManager uniqueInstance;

    private HVDDefaultWalletAdjustManager() {

    }

    public static HVDDefaultWalletAdjustManager getInstance() {
        if (uniqueInstance == null) {
            synchronized (HVDDefaultWalletAdjustManager.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new HVDDefaultWalletAdjustManager();
                }
            }
        }
        return uniqueInstance;
    }

    @Override
    public void adjustWallet() {
        System.out.println("HVD adjust:" + this);
    }
}
