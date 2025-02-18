package com.hamidat.nullpointersapp;

import java.util.Date;
public class Mood {
    private String title;
    private String desc;
    private Date dateTime;
    private String emotionalState;

    //** public constructor mood takes all private attributes into consideration
    public Mood(String title, String desc, String emotionalState, Date dateTime){
        this.title = title;
        this.desc = desc;
        this.emotionalState = emotionalState;
        this.dateTime = dateTime;


    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public String getEmotionalState() {
        return emotionalState;
    }
}
