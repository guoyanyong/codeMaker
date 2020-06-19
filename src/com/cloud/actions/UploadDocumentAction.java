package com.cloud.actions;

import com.cloud.coder.javadoc.info.ClassApi;
import com.cloud.properties.PropertiesComponentUtil;
import com.cloud.tasks.UploadDocProcess;
import com.cloud.ui.HttpConfigDialog;
import com.cloud.util.HttpsUtils;
import com.google.gson.Gson;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/28 14:45
 */
public class UploadDocumentAction extends AnAction {

    private JTabbedPane tabbedPane;

    public UploadDocumentAction(JTabbedPane tabbedPane){
        super("将API同步到服务", "将API同步到服务", AllIcons.Actions.Upload);
        this.tabbedPane = tabbedPane;
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        Project project = anActionEvent.getProject();

        int index = tabbedPane.getSelectedIndex();
        if (index==-1){
            Messages.showErrorDialog("当前未打开任何文档，不会做任何操作","操作提醒");
            return;
        }
        String title = tabbedPane.getTitleAt(index);
        int isOk = Messages.showOkCancelDialog(String.format("是否要当前模块《%s》中的Api信息全量上传到服务器？\n本操作上传的API信息不包含返回值描述\n若需要上传Response信息描述，请在接口详情面板保存接口信息",title), "请谨慎操作", AllIcons.General.Information);
        if (isOk==0) {
            List<ClassApi> classApiList = PropertiesComponentUtil.apiList.get(title).stream().filter(o-> Optional.ofNullable(o.getMethodApis()).isPresent()).collect(Collectors.toList());

            PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
            String projectTag = propertiesComponent.getValue(HttpConfigDialog.prefix + "projectTag");

            String url = HttpsUtils.serverUrl + "/sourceDocument/saveAll/" + projectTag + "/" + title;

            String text = String.format("正在上传所有文档...");
            new BackgroundTaskQueue(project, text).run(new UploadDocProcess(project, text, classApiList, url));
        }
    }
}
