package com.cloud.actions;

import com.cloud.properties.PropertiesComponentUtil;
import com.cloud.ui.AddProjectConfig;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AddProjectTabAction extends AnAction {

    private JTabbedPane tabbedPane;

    public AddProjectTabAction(JTabbedPane tabbedPane){
        super("添加项目", "添加基础项目配置", AllIcons.General.Add);
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getData(PlatformDataKeys.PROJECT);
//        PropertiesComponentUtil.project = project;
        new AddProjectConfig(this.tabbedPane, project);
    }
}
