package com.cloud.coder.javadoc;/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/17 12:40
 */

import com.intellij.openapi.project.Project;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/17 12:40
 */
public class ProjectConfig {

    Project project;
    String scanPackage;
    String classPath;
    String sourcePath;
    String subpackage;

    public ProjectConfig(String scanPackage, String classPath, String sourcePath, String subpackage) {
        this.scanPackage = scanPackage;
        this.classPath = classPath;
        this.sourcePath = sourcePath;
        this.subpackage = subpackage;
    }

    public String getScanPackage() {
        return scanPackage;
    }

    public void setScanPackage(String scanPackage) {
        this.scanPackage = scanPackage;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSubpackage() {
        return subpackage;
    }

    public void setSubpackage(String subpackage) {
        this.subpackage = subpackage;
    }
}
