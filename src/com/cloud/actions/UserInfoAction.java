package com.cloud.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class UserInfoAction extends AnAction {

    public UserInfoAction() {
        super("当前登录用户信息", "userinfo panel", AllIcons.General.User);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {


    }
}
