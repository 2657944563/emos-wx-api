package com.example.emos.wx.common.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class R extends HashMap<String, Object> {
    public R() {
        put("code", HttpStatus.SC_OK);
        put("msg", "success");
    }

    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static R ok() {
        return new R();
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
//        Iterator<Entry<String, Object>> i = entrySet().iterator();
//        if (!i.hasNext()) {
//            return "{}";
//        }
//        StringBuilder sb = new StringBuilder();
//        sb.append('{');
//        for (; ; ) {
//            Entry<String, Object> e = i.next();
//            String key = e.getKey();
//            Object value = e.getValue();
//            sb.append(key.equals(this) ? "(this Map)" : "\"" + key + "\"");
//            sb.append(':');
//            sb.append(value.equals(this) ? "(this Map)" : "\"" + value + "\"");
//            if (!i.hasNext()) {
//                return sb.append('}').toString();
//            }
//            sb.append(',').append(' ');
//        }
    }

    public static R ok(String msg) {
        return new R().put("msg", msg);
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }


    public static R error(int code, String msg) {
        return new R().put("msg", msg).put("code", code);
    }

    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error() {
        return error("网站异常,请联系管理员");
    }

}
