package com.makspasich.counters.models;

import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;

import java.math.BigDecimal;

// [START comment_class]
@IgnoreExtraProperties
public class Value {


    public String uid;
    public String author;
    public String date;
    public BigDecimal value;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(Object value) {
        if (value instanceof String) {
            try {
                this.value = new BigDecimal(value.toString());
                this.value.setScale(2);
//                this.value = new DecimalFormat("").format(Double.parseDouble(value.toString()));
            } catch (Exception e) {
                Log.d("CounterDetailActivityZZ", "setValueEXCEPTION PARSE BigDecimal: ");
            }
            Log.d("CounterDetailActivityZZ", "setValueBigDecimal: " + value);
        } else {
            Log.d("CounterDetailActivityZZ", "setValueFAIL: " + value.getClass().getSimpleName());
        }
    }


    public Value() {
        // Default constructor required for calls to DataSnapshot.getValue(Value.class)
    }

    public Value(String uid, String author, String date, String value) {
        this.uid = uid;
        this.author = author;
        this.date = date;
        this.value = new BigDecimal(value);
    }


}
// [END comment_class]
