package com.cloud.actions;

import com.cloud.ui.ApiToolComponent;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RefreshApiAction extends AnAction {

    private JTabbedPane tabbedPane;

    public RefreshApiAction(JTabbedPane tabbedPane) {
        super("刷新接口文档列表", "刷新", AllIcons.Actions.Refresh);
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e){

        int index = tabbedPane.getSelectedIndex();
        if (index==-1){
            return;
        }

        String title = tabbedPane.getTitleAt(index);

        ApiToolComponent component = ApiToolComponent.getInstance(e.getProject());
        component.fillApi(title, e.getProject(),"refresh");
    }
}
