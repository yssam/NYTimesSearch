package com.example.sam.nytimessearch.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by Sam on 7/26/16.
 */
@Parcel
public class Article {


    public String getWebUrl() {
        return webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getThumbNail() {
        return thumbNail;
    }

    public String getDate() {
        return date;
    }
    String headline;
    String thumbNail;
    String webUrl;
    String date;

    public Article(){
        
    }
    public Article(JSONObject jsonObject){
        try{
            this.webUrl = jsonObject.getString("web_url");
            this.headline = jsonObject.getJSONObject("headline").getString("main");
            this.date = jsonObject.getString("pub_date").substring(0, 10);

            JSONArray multimedia = jsonObject.getJSONArray("multimedia");

            //if(multimedia.length() > 0){
                JSONObject multimediaJson = multimedia.getJSONObject(0);
                this.thumbNail = "http://www.nytimes.com/" + multimediaJson.getString("url");
            //}else{
              //  this.thumbNail = "";
           // }

        }catch(JSONException e){

        }
    }

    public static ArrayList<Article> fromJSONArray(JSONArray array){
        ArrayList<Article> results = new ArrayList<>();

        for(int x=0; x<array.length(); ++x){
            try{
                results.add(new Article(array.getJSONObject(x)));
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return results;
    }
}
