package com.makspasich.counters.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Counter {

    public String uid;
    public String counter_creator;
    public String name_counter;
    public int type_counter = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Counter() {
        // Default constructor required for calls to DataSnapshot.getValue(Counter.class)
    }

    public Counter(String uid, String id_counter_creator, String name_counter, int type_counter) {
        this.uid = uid;
        this.counter_creator = id_counter_creator;
        this.name_counter = name_counter;
        this.type_counter = type_counter;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("counter_creator", counter_creator);
        result.put("name_counter", name_counter);
        result.put("type_counter", type_counter);
        result.put("stars", stars);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
