package com.example.sam.nytimessearch.model;

import android.text.TextUtils;

/**
 * Created by Sam on 7/30/16.
 */

public class Query {
    String q;
    String fq;

    public String getSort() {
        return sort;
    }

    public String getQ() {
        return q;
    }

    public String getFq() {
        return fq;
    }

    public String getBegin_date() {
        return begin_date;
    }

    String begin_date;
    String sort;

    public Query(String q, String fq, String begin_date, String sort){
        try{
            if(!TextUtils.isEmpty(q))
                this.q = q;
            else
                this.q = "";
            if(!TextUtils.isEmpty(fq))
                this.fq = fq;
            else
                this.fq = "";
            if(!TextUtils.isEmpty(begin_date))
                this.begin_date = begin_date;
            else
                this.begin_date = "";
            if(!TextUtils.isEmpty(sort))
                this.sort = sort;
            else
                this.sort = "";

        }catch(Exception e){

        }
    }
}
