package com.youtube.downloader.testing;

/**
 * Created by nareshm on 12/03/2015.
 */
public class WalletFactory {
    private volatile static WalletFactory uniqueInstance;

    private WalletFactory() {

    }

    public static WalletFactory getInstance() {
        if (uniqueInstance == null) {
            synchronized (WalletFactory.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new WalletFactory();
                }
            }
        }
        return uniqueInstance;
    }

    public WalletAdjustManager createWalletAdjustManager(boolean isHVD) {
        WalletAdjustManager walletAdjustManager;
        if (isHVD) {
            walletAdjustManager = HVDDefaultWalletAdjustManager.getInstance();
        } else {
            walletAdjustManager = DefaultWalletAdjustManager.getInstance();
        }
        return walletAdjustManager;
    }
}
