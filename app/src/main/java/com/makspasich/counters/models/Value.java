package com.makspasich.counters.models;

import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;

import java.math.BigDecimal;

@IgnoreExtraProperties
public class Value {
    private final String TAG = "MyLogValue";

    public String uid;
    public String author;
    public String date;
    public BigDecimal value;

    public String getValue() {
        return value.toString();
    }

    public void setValue(Object value) {
        if (value instanceof String) {
            try {
                this.value = new BigDecimal(value.toString());
                this.value.setScale(2);
            } catch (Exception e) {
                Log.d(TAG, "setValueEXCEPTION PARSE BigDecimal: "+value.toString());
            }
            Log.d(TAG, "setValueBigDecimal: " + value);
        } else {
            Log.d(TAG, "setValueFAIL: " + value.getClass().getSimpleName());
        }
    }

    public Value() {
        // Default constructor required for calls to DataSnapshot.getValue(Value.class)
    }

    public Value(String uid, String author, String date, String value) {
        this.uid = uid;
        this.author = author;
        this.date = date;
        try {
            this.value = new BigDecimal(value);
        } catch (Exception e) {
            Log.d(TAG, "exception value");

        }
    }

    @Override
    public String toString() {
        return "Value{" +
                "TAG='" + TAG + '\'' +
                ", uid='" + uid + '\'' +
                ", author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", value=" + value +
                '}';
    }
}