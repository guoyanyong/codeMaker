package com.cloud.coder.javadoc.info;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/31 12:28
 */
public class ClassApi extends ApiBaseInfo implements Serializable {

    private List<MethodApi> methodApis;

    public List<MethodApi> getMethodApis() {
        return methodApis;
    }

    public void setMethodApis(List<MethodApi> methodApis) {
        this.methodApis = methodApis;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
