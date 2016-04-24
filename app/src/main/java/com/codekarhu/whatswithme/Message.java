package com.codekarhu.whatswithme;

/**
 * Created by @author ${user} on ${date}
 * <p/>
 * ${file_name}
 */
public class Message {
    public boolean left;
    public String message;

    public Message(boolean isWatson, String message) {
        super();
        this.left = isWatson;
        this.message = message;
    }
}
