package com.cloud.coder.javadoc.info;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.javadoc.SourcePosition;

import java.io.Serializable;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/31 14:45
 */
public class ApiBaseInfo implements Serializable {
    protected String name;
    protected String url;
    protected String author;
    protected String createTime;
    protected transient SourcePosition position;
    protected transient VirtualFile virtualFile;
    private String signature;
    private transient int textOffset;
    private transient Project project;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public void setVirtualFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    public int getTextOffset() {
        return textOffset;
    }

    public void setTextOffset(int textOffset) {
        this.textOffset = textOffset;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public SourcePosition getPosition() {
        return position;
    }

    public void setPosition(SourcePosition position) {
        this.position = position;
    }
}
