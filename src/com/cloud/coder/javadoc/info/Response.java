package com.cloud.coder.javadoc.info;

import java.io.Serializable;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/6/9 15:47
 */
public class Response implements Serializable {

    /** 主健 */
    private Integer id;
    /** 关联接口 */
    private Integer apiId;
    /** http状态值 */
    private String httpCode;
    /** 是否弃用 */
    private Integer deprecated;
    /** 字段全路径 */
    private String fullPath;
    /** 字段名称 */
    private String name;
    /** 数据类型 */
    private String dataType;
    /** 字段描述 */
    private String comment;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getApiId() {
        return apiId;
    }

    public void setApiId(Integer apiId) {
        this.apiId = apiId;
    }

    public String getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(String httpCode) {
        this.httpCode = httpCode;
    }

    public Integer getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Integer deprecated) {
        this.deprecated = deprecated;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
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

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
