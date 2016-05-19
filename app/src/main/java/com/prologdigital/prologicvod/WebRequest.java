package com.prologdigital.prologicvod;

/**
 * Created by DanM on 17/05/2016.
 */
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class WebRequest {
    public String getURL(String stringURL){
        try{
            URL url = new URL(stringURL);
            try{
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedReader reader =new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String webPage = "";
                String data="";

                while ((data = reader.readLine()) != null){
                    webPage += data + "\n";
                }
                return webPage;
            } catch (IOException e){
                return e.toString();
            }
        } catch (MalformedURLException e){
            return e.toString();
        }
    }
    public void savePDF(String stringURL){
        try {
            String extStorageDirectory = Environment.getExternalStorageDirectory()
                    .toString();
            File folder = new File(extStorageDirectory, "pdf");
            folder.mkdir();
            File file = new File(folder, "Read.pdf");

            FileOutputStream f = new FileOutputStream(file);
            URL u = new URL(stringURL);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            InputStream in = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

