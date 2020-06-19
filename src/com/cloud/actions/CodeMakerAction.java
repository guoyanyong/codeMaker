package com.cloud.actions;

import com.cloud.coder.db.DBConnectInfo;
import com.cloud.ui.GenerateCodeUI;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;

import java.util.Arrays;

public class CodeMakerAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor data = e.getData(PlatformDataKeys.EDITOR);
        SelectionModel selectionModel = data.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        String[] split = selectedText.split("\n");
        final DBConnectInfo connectInfo = new DBConnectInfo();
        Arrays.stream(split).forEach(o->{
            if(o.contains("url:")){
                connectInfo.setUrl(o.substring(o.indexOf("url:") + 4).trim());
            }else if(o.contains("url=")){
                connectInfo.setUrl(o.substring(o.indexOf("url=") + 4).trim());
            }else if(o.contains("driver-class-name:")){
                connectInfo.setDriverClassName(o.substring(o.indexOf("driver-class-name:") + 18).trim());
            }else if(o.contains("driver-class-name=")){
                connectInfo.setDriverClassName(o.substring(o.indexOf("driver-class-name=") + 18).trim());
            }else if(o.contains("username:")){
                connectInfo.setUsername(o.substring(o.indexOf("username:") + 9).trim());
            }else if(o.contains("username=")){
                connectInfo.setUsername(o.substring(o.indexOf("username=") + 9).trim());
            }else if(o.contains("password:")){
                connectInfo.setPassword(o.substring(o.indexOf("password:") + 9).trim());
            }else if(o.contains("password=")){
                connectInfo.setPassword(o.substring(o.indexOf("password=") + 9).trim());
            }
        });

        new GenerateCodeUI(connectInfo, e);
    }
}
