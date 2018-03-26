package pasu.ntracker.utils;

import pasu.ntracker.BuildConfig;

/**
 * Created by Admin on 24-03-2018.
 */

public class Systems {
    public static class out{
        public static void println(String message){
            if(BuildConfig.DEBUG)
                System.out.println(message);
        }
    }
}
