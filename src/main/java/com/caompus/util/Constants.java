package com.caompus.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by credtitone on 2016/10/22.
 */
public class Constants extends Properties{

    private static Constants constants = new Constants();

    public static Constants getInstance(){
        return constants;
    }
    private Constants(){
        try {
            InputStream in = Constants.class.getResourceAsStream("/config.properties");
            load(in);
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
