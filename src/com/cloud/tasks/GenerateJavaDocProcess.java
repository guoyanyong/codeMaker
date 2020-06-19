package com.cloud.tasks;

import com.cloud.coder.javadoc.info.ClassApi;
import com.cloud.coder.javadoc.info.MethodApi;
import com.cloud.ui.ApiToolComponent;
import com.cloud.ui.VersionPanel;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.kitt.util.DocUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;
import java.util.Map;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/7/5 10:50
 */
public class GenerateJavaDocProcess extends Task.Backgroundable{

    private Project project;
    private String moduleName;

    private DocUtil docUtil;

    public GenerateJavaDocProcess(@Nullable Project project, String title, String moduleName, DocUtil docUtil){
        super(project,title);
        this.moduleName = moduleName;
        this.project = project;
        this.docUtil = docUtil;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        GenerateJavaDocProcess generateJavaDocProcess = this;
        generateJavaDocProcess.setTitle(String.format("正在检索文件..."));
        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                //判断是否按版本号分组api
                boolean isGroup = ApiToolComponent.isGroupApi(project);

                if (isGroup) {
                    List<ClassApi> classApiList = docUtil.getAllApiDocByModuleName(project, moduleName, generateJavaDocProcess);
                    List<Map.Entry<String, List<ClassApi>>> versionGroupMap = docUtil.groupingApi(classApiList);

                    docUtil.getVersionPane().removeAll();
                    for (Map.Entry<String, List<ClassApi>> entry : versionGroupMap) {
                        VersionPanel versionPanel = new VersionPanel(entry.getValue(), project, moduleName);
                        docUtil.getVersionPane().add(entry.getKey(), versionPanel.$$$getRootComponent$$$());
                    }
                }else {
                    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("正在解析API文档，请稍等...");
                    TreeModel treeModel = new DefaultTreeModel(rootNode);
                    List<ClassApi> classApiList = docUtil.getAllApiDocByModuleName(project, moduleName, generateJavaDocProcess);
                    docUtil.getTree().setModel(treeModel);
                    classApiList.stream().forEach(classApi -> {
                        DefaultMutableTreeNode apiNode = new DefaultMutableTreeNode(classApi);
                        rootNode.add(apiNode);
                        List<MethodApi> methodApi = classApi.getMethodApis();
                        if (methodApi != null) {
                            methodApi.stream().forEach(a -> {
                                DefaultMutableTreeNode apiComment = new DefaultMutableTreeNode(a);
                                apiNode.add(apiComment);
                            });
                        }
                    });
                    rootNode.setUserObject("所有接口列表");
                    TreePath path = new TreePath(rootNode.getPath());
                    docUtil.getTree().expandPath(path);
                    docUtil.getTree().updateUI();
                }
            }
        });

    }
}
