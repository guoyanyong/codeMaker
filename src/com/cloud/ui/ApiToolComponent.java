package com.cloud.ui;

import com.cloud.actions.*;
import com.cloud.coder.javadoc.ProjectConfig;
import com.google.common.collect.Maps;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.util.Map;
import java.util.Optional;

public class ApiToolComponent implements ProjectComponent {


    private JTabbedPane tabbedPane;

    public static Map<String, ProjectConfig> projectConfig = Maps.newConcurrentMap();

    public static ApiToolComponent getInstance(Project project){
        return project.getComponent(ApiToolComponent.class);
    }

    public void initWindow(ToolWindow toolWindow){

        ProjectPanel projectPanel = new ProjectPanel();
        Content content = ContentFactory.SERVICE.getInstance().createContent(projectPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        //获取tabbedPanel
        tabbedPane = projectPanel.getTabbedPane1();

        //设置主窗口工具栏
        projectPanel.setToolbar(createToolBar().getComponent());
    }

    public void addProjectTab(String moduleName, String scanPackage,  String classPath, String sourcePath, String subpackages, Project project){

        projectConfig.put(moduleName, new ProjectConfig(scanPackage, classPath, sourcePath, subpackages));

        fillApi(moduleName, project, "add");

//        int index = tabbedPane.indexOfTab(moduleName);
//        if (index>-1){
//            JOptionPane.showMessageDialog(null, "已经添加过该项目", "重复提醒",JOptionPane.WARNING_MESSAGE);
//        }else{
//            VersionPanel versionPanel = new VersionPanel(moduleName);
//            tabbedPane.add(moduleName, versionPanel.$$$getRootComponent$$$());
//            PanelTabWithClose tabWithClose = new PanelTabWithClose(moduleName, tabbedPane, versionPanel.$$$getRootComponent$$$());
//            index = tabbedPane.indexOfTab(moduleName);
//            tabbedPane.setTabComponentAt(index, tabWithClose.getUi());
//        }
//        tabbedPane.setSelectedIndex(index);
    }

    public void fillApi(String moduleName, Project project, String action){
        //判断是否按版本号分组api
        boolean isGroupApi = isGroupApi(project);

        int index = tabbedPane.indexOfTab(moduleName);
        JComponent jComponent = null;
        if (index>-1){
            //如果Tab标签已经存在
            if (action.equalsIgnoreCase("add")) {
                JOptionPane.showMessageDialog(null, "已经添加过该项目", "重复提醒",JOptionPane.WARNING_MESSAGE);
            }else if (action.equalsIgnoreCase("refresh")){
                //如果是更新API
                if (isGroupApi){
                    jComponent = new ApisPanel(moduleName,project).getUI();
                    tabbedPane.setComponentAt(index,jComponent);
                }else{
                    jComponent = new VersionPanel(moduleName, project).$$$getRootComponent$$$();
                    tabbedPane.setComponentAt(index,jComponent);
                }
            }
        }else{
            //如果是新增Tab标签
            if (isGroupApi){
                ApisPanel apisPanel = new ApisPanel(moduleName,project);
                jComponent = apisPanel.$$$getRootComponent$$$();
            }else{
                VersionPanel versionPanel = new VersionPanel(moduleName,project);
                jComponent = versionPanel.$$$getRootComponent$$$();
            }
            tabbedPane.add(moduleName, jComponent);

        }

        PanelTabWithClose tabWithClose = new PanelTabWithClose(moduleName, tabbedPane, jComponent);
        index = tabbedPane.indexOfTab(moduleName);
        tabbedPane.setTabComponentAt(index, tabWithClose.getUi());
        tabbedPane.setSelectedIndex(index);
    }

    public static boolean isGroupApi(Project project) {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        boolean isGroupApi = false;
        String value = properties.getValue(HttpConfigDialog.prefix + "isGroupApi");
        if (Optional.ofNullable(value).isPresent()) {
            if (value.equalsIgnoreCase("true")){
                isGroupApi = true;
            }
        }
        return isGroupApi;
    }

    private ActionToolbar createToolBar() {
        DefaultActionGroup group = new DefaultActionGroup();
        //添加项目按钮
        group.add(new AddProjectTabAction(this.tabbedPane));

        //添加刷新按钮
        group.add(new RefreshApiAction(tabbedPane));

        //环境配置按钮
        group.add(new OpenHttpConfig());

        //上传所有API 接口文档
        group.add(new UploadDocumentAction(tabbedPane));

        //从rap2同步接口描述信息
        group.add(new FromRap2ApiAction(tabbedPane));

        //插件使用介绍按钮
        group.add(new PluginUsageTabAction());

        //登录用户信息
//        group.add(new UserInfoAction());


        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, false);
        toolbar.setOrientation(SwingConstants.VERTICAL);
        return toolbar;
    }

}
