package com.mokey01.mkchain;

import java.io.Serializable;

/**
 * @author: feiweiwei
 * @description: P2P通讯消息bean
 * @created Date: 18/4/9.
 * @modify by:
 */
public class Message implements Serializable{
    private int    type;
    private String data;

    public Message() {
    }

    public Message(int type) {
        this.type = type;
    }

    public Message(int type, String data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
