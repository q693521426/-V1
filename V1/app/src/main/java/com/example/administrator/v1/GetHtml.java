package com.example.administrator.v1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by Administrator on 2016/11/16 0016.
 */

public final class GetHtml{
    public static String read(String html){
        try{
            URL url=new URL(html);
            StringBuffer sourceHtml=new StringBuffer();
            BufferedReader in=new BufferedReader(new InputStreamReader(url.openStream()));
            String buffer=new String();
            while(!(null==(buffer=in.readLine()))){
                sourceHtml.append(buffer).append("\r\n");
            }
            in.close();
            return sourceHtml.toString();
        }catch(Exception e){
            System.out.println("Error:\n"+e);
        }
        return null;
    }
}