package com.cloud.handlers;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/6/3 18:28
 */
public interface MessageHandler {

    public static Map<String, MessageHandler> hanlers = Maps.newHashMap();

    default MessageHandler getHanlder(String messge){
        JsonObject msg = new JsonObject().getAsJsonObject(messge);
        String action = msg.get("action").toString();
        try {
            return hanlers.get(action).getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void doMsg(String message);
}
