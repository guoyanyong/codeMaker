package com.cloud.coder.javadoc.info;

import java.io.Serializable;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/31 12:40
 */
public class Param implements Serializable {
    private String name;
    private String dataType;
    private String comment;
    private String defaultValue;
    private String defaultValueRegular;
    private Boolean isRequired;
    private ParamType paramType;

    public enum ParamType{

        Header,
        Path,
        Form,
        Body;

        private String type;
        ParamType(){
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String type) {
        this.dataType = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValueRegular() {
        return defaultValueRegular;
    }

    public void setDefaultValueRegular(String defaultValueRegular) {
        this.defaultValueRegular = defaultValueRegular;
    }

    public Boolean getRequired() {
        return isRequired;
    }

    public void setRequired(Boolean required) {
        isRequired = required;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(ParamType paramType) {
        this.paramType = paramType;
    }
}
