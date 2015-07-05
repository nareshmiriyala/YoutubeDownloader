package com.youtube.downloader.testing.classloader;

import com.youtube.downloader.testing.DefaultWalletAdjustManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by NARESHM on 13/03/2015.
 */
public class ClassTest {
    public static void main(String[] args) {
        ClassLoader classLoader=ClassLoader.getSystemClassLoader();
        System.out.println(classLoader);
        System.out.println(DefaultWalletAdjustManager.getInstance());
        System.out.println(DefaultWalletAdjustManager.getInstance());
        System.out.println(DefaultWalletAdjustManager.getInstance());

        System.out.println(classLoader);

        URL[] urls = null;
        try {
            // Convert the file object to a URL
            File dir = new File(System.getProperty("user.dir")
                    +File.separator+"com.youtube.downloader.testing"+ File.separator);
            String urlString="file:C:/Users/NARESHM/Documents/NetBeansProjects/youtubeindianmovies/target/classes/com/youtube/downloader/testing/";
            URL url =new URL(urlString);
            urls = new URL[]{url};
        } catch (MalformedURLException ignored) {
        }
        ExampleClassLoader exampleClassLoader=new ExampleClassLoader(urls,ClassLoader.getSystemClassLoader());
        try {
            Class<?> aClass = exampleClassLoader.loadClass("com.youtube.downloader.testing.DefaultWalletAdjustManager");
            System.out.println(exampleClassLoader);

                try {
                    Method getInstance = aClass.getMethod("getInstance", null);
                    try {
                        System.out.println(getInstance.invoke(null));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        ClassLoader classLoaderNew = ClassTest.class.getClassLoader();

        try {
            Class aClass = classLoaderNew.loadClass("com.youtube.downloader.testing.DefaultWalletAdjustManager");
            System.out.println("aClass.getName() = " + aClass.getName());
            System.out.println(classLoader);
            System.out.println(DefaultWalletAdjustManager.getInstance());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}

