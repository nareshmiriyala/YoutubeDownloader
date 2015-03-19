package com.youtube.downloader.testing;

import java.math.BigDecimal;

/**
 * Created by NARESHM on 17/03/2015.
 */
public class BigDecimalTest {
    public static void main(String[] args) {
        BigDecimal am=new BigDecimal(10);
        System.out.println(am.setScale(0,BigDecimal.ROUND_HALF_UP).movePointRight(0));
    }
}
