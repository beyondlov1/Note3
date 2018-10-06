package com.beyond.fly.note3.Utils;

import java.util.HashMap;

/**
 * Created by beyond on 2018/1/6.
 */

public class MyHashmap extends HashMap<String,Object> {
    @Override
    public boolean equals(Object o) {
        if (o instanceof MyHashmap){
            if (this.get("_id").toString().equals(((MyHashmap) o).get("_id").toString())){
                return true;
            }
        }
        return false;
    }
}
