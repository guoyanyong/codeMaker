package com.cloud.coder.javadoc.info;

import com.google.common.collect.Lists;

import javax.swing.*;
import java.io.Serializable;
import java.util.List;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/31 12:37
 */
public class MethodApi extends ApiBaseInfo implements Serializable {

    private String method;
    private String version;
    private String authorization;
    private String returnName;
    private String methodName;
    private List<Param> paramList = Lists.newArrayList();
    private List<Response> responseList = Lists.newArrayList();
    /**分类标识*/
    private String sourceCategorySignature;
    private transient ImageIcon icon = new ImageIcon();
    private ClassApi classApi;

    public MethodApi() {
    }

    public MethodApi(String method, String version, String authorization, String returnName, String methodName, List<Param> paramList, List<Response> responseList, String sourceCategorySignature, ImageIcon icon, ClassApi classApi) {
        this.method = method;
        this.version = version;
        this.authorization = authorization;
        this.returnName = returnName;
        this.methodName = methodName;
        this.paramList = paramList;
        this.responseList = responseList;
        this.sourceCategorySignature = sourceCategorySignature;
        this.icon = icon;
        this.classApi = classApi;
    }

    public ClassApi getClassApi() {
        return classApi;
    }

    public void setClassApi(ClassApi classApi) {
        this.classApi = classApi;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getReturnName() {
        return returnName;
    }

    public void setReturnName(String returnName) {
        this.returnName = returnName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<Param> getParamList() {
        return paramList;
    }

    public void setParamList(List<Param> paramList) {
        this.paramList = paramList;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public String getVersion() {
        return version==null?"BASE":version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Response> getResponseList() {
        return responseList;
    }

    public void setResponseList(List<Response> responseList) {
        this.responseList = responseList;
    }

    public String getSourceCategorySignature() {
        return sourceCategorySignature;
    }

    public void setSourceCategorySignature(String sourceCategorySignature) {
        this.sourceCategorySignature = sourceCategorySignature;
    }

    @Override
    public String toString() {
        return this.getName().isEmpty()?this.getMethodName():this.getName();
    }
}
