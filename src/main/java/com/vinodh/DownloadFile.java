package com.vinodh;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

/**
 * Created by vthiagarajan on 4/6/18.
 */
public class DownloadFile {

    public static void main(String[] args) throws MalformedURLException, IOException{
        File f = new File("./src/main/resources/a9d93e53-8c1d-4567-8074-a172e1f9a8f4.jpg");
        URL url = new URL("https://a0.muscache.com/im/pictures/a9d93e53-8c1d-4567-8074-a172e1f9a8f4.jpg");
        FileUtils.copyURLToFile(url, f);
    }
}
