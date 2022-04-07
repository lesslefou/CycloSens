package com.example.cyclosens.classes;

import java.io.Serializable;

public class Activity  implements Serializable {

    private String key;
    private String nameActivity;
    private String dateActivity;
    private long duration;
    private int bpmAv;
    private int strenghAv;

    public String getNameActivity() { return nameActivity; }
    public String getDateActivity() { return dateActivity; }
    public long getDuration() { return duration; }
    public String getKey() { return key; }
    public int getBpmAv() { return bpmAv; }
    public int getStrenghAv() { return strenghAv; }

    public void setNameActivity(String nameActivity) { this.nameActivity = nameActivity; }
    public void setDateActivity(String dateActivity) { this.dateActivity = dateActivity; }
    public void setDuration(long duration) { this.duration = duration; }
    public void setKey(String key) { this.key = key; }
    public void setBpmAv(int bpmAv) { this.bpmAv = bpmAv; }
    public void setStrenghAv(int strenghAv) { this.strenghAv = strenghAv; }

    public Activity(String key, String nameActivity, String dateActivity, long duration, int bpmAv, int strenghAv) {
        setKey(key);
        setNameActivity(nameActivity);
        setDateActivity(dateActivity);
        setDuration(duration);
        setBpmAv(bpmAv);
        setStrenghAv(strenghAv);
    }

}
