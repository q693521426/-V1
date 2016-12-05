package com.example.administrator.v1;

/**
 * Created by Administrator on 2016/11/16 0016.
 */

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ParseHtml {
    static ArrayList<String>book_info=new ArrayList<String>();
    static final int TYPE_HTTP = 0;
    static final int TYPE_NOVEL_INFO = 1;
    static final int TYPE_NOVEL_INFO1 = 2;
    static final int TYPE_NOVEL_LIST = 3;
    static final int TYPE_NOVEL_INDEX = 4;
    static final int TYPE_NOVEL_CHAPTER = 5;
    static final int TYPE_NOVEL_CONTENT = 6;
    static final int TYPE_NOVEL_HTML=7;
    static final int TYPE_NOVEL_INFO_WITHOUT=8;
    private static Pattern pattern;

    ParseHtml() {
    }

    private static void setParseType(int parseType) {
        switch (parseType) {
            case TYPE_HTTP:
                pattern = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:]+");
                break;
            case TYPE_NOVEL_INFO:
                pattern = Pattern.compile("(<div id=\"info\">(.+?)</div>)|(<div id=\"intro\">(.+?)</div>)|(<div id=\"sidebar\">(.+?)</div>)|(<meta property=\"og:novel:category\" content=(.+?)/>)", Pattern.DOTALL);
                break;
            case TYPE_NOVEL_INFO1:
                pattern = Pattern.compile("(<p>(.+?)</p>)|(src=\"(.+?)\")|(<h1>(.+?)</h1>)|(<meta property=\"og:novel:category\" content=(.+?)/>)", Pattern.DOTALL);
                break;
            case TYPE_NOVEL_LIST:
                pattern = Pattern.compile("<div id=\"list\">(.+?)</div>", Pattern.DOTALL);
                break;
            case TYPE_NOVEL_INDEX:
                pattern = Pattern.compile("(?i)<A HREF=\"(.+?)\\.html\"");
                break;
            case TYPE_NOVEL_CHAPTER:
                pattern = Pattern.compile("(?i)<A HREF=\"(.+?)\">(.+?)</A>", Pattern.DOTALL);
                break;
            case TYPE_NOVEL_CONTENT:
                pattern = Pattern.compile("<div id=\"content\">(.+?)</div>", Pattern.DOTALL);
                break;
            case TYPE_NOVEL_HTML:
                pattern = Pattern.compile("<div\\s+onclick=\"window.location='(.+?)'", Pattern.DOTALL);
                break;
            case TYPE_NOVEL_INFO_WITHOUT:
                pattern = Pattern.compile("(<div id=\"info\">(.+?)</div>)|(<div id=\"sidebar\">(.+?)</div>)|(<meta property=\"og:novel:category\" content=(.+?)/>)", Pattern.DOTALL);
                break;
            default:
                pattern = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:]+");
                break;
        }
    }
    private static String sub_string(String s,String c_start,String c_end,int index){
        int start = s.indexOf(c_start);
        for(int i=1;i<index;++i)
            start=s.indexOf(c_start,start+1);
        int end = s.indexOf(c_end, start+1);
        s = s.substring(start + 1, end);
        return s;
    }
    private static String preParse(String s, int parseType) {
        String tmp = s;
        switch (parseType) {
            case TYPE_NOVEL_INFO:{
                book_info.addAll(getParseResults(s,TYPE_NOVEL_INFO1));
            }
            break;
            case TYPE_NOVEL_INFO1:{
                if(tmp.substring(0,3).equals("src")){
                    tmp=tmp.substring(5, tmp.length()-1);
                }else if(tmp.contains("<meta property")){
                    tmp=tmp.replace("<meta property=\"og:novel:category\" content=\"","");
                    tmp=tmp.replace("\"","");
                    tmp=tmp.replace("/>","");
                    tmp="分    类："+tmp;
                }else {
                    tmp = tmp.replace("\r\n","");
                    tmp = tmp.replaceAll("<a.*?>","");
                    tmp = tmp.replaceAll("</a>","");
                    tmp = tmp.replace("&nbsp;", " ");
                    tmp=sub_string(tmp,">","<",1);
                }
            }
            break;
            case TYPE_NOVEL_INDEX: {
                tmp=sub_string(tmp,"\"","\"",1);
            }
            break;
            case TYPE_NOVEL_CHAPTER: {
                tmp=sub_string(tmp,">","<",1);
            }
            break;
            case TYPE_NOVEL_CONTENT: {
                tmp = tmp.replaceFirst("<div id=\"content\">", "    ");
                tmp = tmp.replaceAll("<script.*>(.+?)</script>", "");
                tmp = tmp.replaceFirst("</div>", "");
                tmp = tmp.replace("&nbsp;", " ");
                tmp = tmp.replaceAll("<br/><br/>", "\r\n");
            }
            break;
            case TYPE_NOVEL_LIST:{
                if(tmp.contains("最新章节")&&tmp.contains("作品相关"))
                    tmp = sub_string(tmp,"<dt>","</div>",3);
                else if(tmp.contains("最新章节")&&!tmp.contains("作品相关"))
                    tmp = sub_string(tmp,"<dt>","</div>",2);
            }
            break;
            case TYPE_NOVEL_HTML:{
                tmp = tmp.replaceAll("<div\\s+onclick=\"window.location=","");
                tmp = tmp.replace("'","");
            }
            break;
            case TYPE_NOVEL_INFO_WITHOUT:{
                book_info.addAll(getParseResults(s,TYPE_NOVEL_INFO1));
            }
            break;
            default:
                break;
        }
        return tmp;
    }

    public static ArrayList<String> getParseResults(String sourceHtml, int parseType) {
        setParseType(parseType);

        ArrayList<String> result = new ArrayList<String>();
        Matcher matcher = pattern.matcher(sourceHtml);

        while (matcher.find()) {
            String str = matcher.group();
            str = preParse(str, parseType);
    //        if (result.contains(str) == false)
            result.add(str);
        }
        return result;
    }
}
