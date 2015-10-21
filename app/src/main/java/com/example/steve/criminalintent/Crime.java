package com.example.steve.criminalintent;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Steve on 10/7/2015.
 */
public class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;
    private String mIdSuspect;


    public Crime(){
        this(UUID.randomUUID());
    }

    public Crime(UUID id){
        mId = id;
        mDate = new Date();
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public UUID getId() {
        return mId;
    }

    public Date getDate() {
        return mDate;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getIdSuspect() {
        return mIdSuspect;
    }

    public void setSuspectId(String idSuspect) {
        mIdSuspect = idSuspect;
    }

    public String getPhotoFilename(){
        return "IMG_CRIME_" + mId.toString() + ".jpg";
    }
}
