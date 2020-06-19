package com.cloud.coder;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/6/4 18:49
 */
public class SocketDTO {

    private ActionsEnum action = ActionsEnum.InsertDocument;

    private Object data;

    public ActionsEnum getAction() {
        return action;
    }

    public void setAction(ActionsEnum action) {
        this.action = action;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
