package com.nowcoder.wenda.model;

import java.util.HashMap;
import java.util.Map;

//可以在View传输任何对象,用viewObject类,set进来,get出去
public class ViewObject {
    private Map<String, Object> objs = new HashMap<String, Object>();
    public void set(String key, Object value) {
        objs.put(key, value);
    }

    public Object get(String key) {
        return objs.get(key);
    }
}
