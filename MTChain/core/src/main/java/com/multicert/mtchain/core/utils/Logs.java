package com.multicert.mtchain.core.utils;

import java.time.LocalTime;

public final class Logs {

    private Logs(){

    }

    public static void write(String... s){
        LocalTime time = LocalTime.now();
        for(String str: s) {
            System.out.println(time.toString() + ": " + str);
        }
    }

    public static void writeInLine(boolean isFirstPart, String... s){
        LocalTime time = LocalTime.now();
        if(isFirstPart){
            System.out.print(time.toString() + ": ");
        }

        for(String str: s) {
            System.out.print(str);
        }
    }

}
