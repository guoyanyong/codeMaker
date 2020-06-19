package com.cloud.actions;

import com.cloud.ui.PluginUsageDialog;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class PluginUsageTabAction extends AnAction {


    public PluginUsageTabAction(){
        super("插件使用说明", "插件使用方法介绍", AllIcons.Actions.Help);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        new PluginUsageDialog();
    }
}
