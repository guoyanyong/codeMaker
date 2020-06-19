package com.cloud.tasks;

import com.cloud.coder.javadoc.info.ClassApi;
import com.cloud.coder.javadoc.info.MethodApi;
import com.cloud.properties.PropertiesComponentUtil;
import com.cloud.ui.HttpConfigDialog;
import com.cloud.util.HttpsUtils;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/6/5 15:00
 */
public class UploadDocProcess extends Task.Backgroundable{

    private List<ClassApi> classApiList;
    private Project project;
    private String url;

    public UploadDocProcess(@Nullable Project project, String title, List<ClassApi> classApiList, String url){
        super(project,title);
        this.project = project;
        this.classApiList = classApiList;
        this.url = url;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {

        for (ClassApi classApi : classApiList) {
            String text = String.format("正在上传 %s  所有文档...",classApi.getName());
            this.setTitle(text);

            for (MethodApi methodApi : classApi.getMethodApis()) {
                methodApi.setClassApi(null);
            }

            HttpsUtils.doPost(this.url, new Gson().toJson(classApi));
        }
    }
}
