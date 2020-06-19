package com.cloud.actions;/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/28 14:45
 */

import com.cloud.ui.HttpConfigDialog;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/28 14:45
 */
public class OpenHttpConfig extends AnAction {

    public OpenHttpConfig(){
        super("Http请求配置", "Http请求配置", AllIcons.General.Settings);
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getData(PlatformDataKeys.PROJECT);
        new HttpConfigDialog(project);
    }
}
