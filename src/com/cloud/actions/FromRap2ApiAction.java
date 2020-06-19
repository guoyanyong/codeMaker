package com.cloud.actions;

import com.cloud.coder.javadoc.info.ClassApi;
import com.cloud.coder.javadoc.info.MethodApi;
import com.cloud.coder.javadoc.info.Response;
import com.cloud.properties.PropertiesComponentUtil;
import com.cloud.ui.HttpConfigDialog;
import com.cloud.util.HttpsUtils;
import com.cloud.util.MD5Util;
import com.cloud.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.kitt.util.SqlUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FromRap2ApiAction extends AnAction {

    private JTabbedPane tabbedPane;

    public FromRap2ApiAction(JTabbedPane projectTabbedPane) {
        super("从Rap2中回写Api描述", "from rap2 download", AllIcons.Actions.Download);
        this.tabbedPane = projectTabbedPane;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        int isOk = Messages.showOkCancelDialog("是否要从Rap2中同步Api信息\n该操作会覆盖当前源码中的注释信息", "请谨慎操作", AllIcons.General.Information);
        if (isOk==0){
            Project project = e.getProject();
            int index = tabbedPane.getSelectedIndex();
            if (index == -1) {
                return;
            }
            String moduleName = tabbedPane.getTitleAt(index);

//        Module module = PropertiesComponentUtil.moduleMapping.get(moduleName);
            List<ClassApi> classApis = PropertiesComponentUtil.apiList.get(moduleName);

            new BackgroundTaskQueue(project, "从rap2中同步api").run(new UploadPress(project, classApis, moduleName));
        }



    }

    public class UploadPress extends Task.Backgroundable {

        private List<ClassApi> classApiList;
        private Project project;
        private String moduleName;

        public UploadPress(Project project, List<ClassApi> classApiList, String moduleName) {
            super(project, "从rap2中同步api");
            this.project = project;
            this.classApiList = classApiList;
            this.moduleName = moduleName;
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            //连接rap2数据库
            SqlUtil sqlUtil = SqlUtil.getConnect(SqlUtil.Connect.Rap2);
            List<Map<String, Object>> list = sqlUtil.queryListBySql("SELECT id, name, url, path, method FROM Interfaces WHERE ISNULL(deletedAt) AND repositoryId>16 GROUP BY path,method HAVING COUNT(id)=1");
            for (Map<String, Object> map : list) {
                String id = map.get("id").toString();
                String path = map.get("path").toString();
                String method = map.get("method").toString();
                String name = map.get("name").toString();
                UploadPress uploadPress = this;
                for (ClassApi classApi : classApiList) {
                    for (MethodApi methodApi : classApi.getMethodApis()) {
                        if (methodApi.getUrl().equals(path) && methodApi.getMethod().equalsIgnoreCase(method)) {
                            List<Map<String, Object>> params = sqlUtil.queryListBySql(String.format("SELECT id, scope, `type`, name, value, description FROM Properties WHERE interfaceId=%s AND  scope='request' AND ISNULL(deletedAt)", id));
                            List<Map<String, Object>> response = sqlUtil.queryListBySql(String.format("SELECT id, type, name, description, parentId FROM Properties WHERE interfaceId=%s AND scope='response' AND ISNULL(deletedAt) GROUP BY NAME", id));
                            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                                @Override
                                public void run() {

                                    uploadPress.setTitle(String.format("正在从rap2中回写 《%s》 接口信息...", name));

                                    VirtualFile file = methodApi.getVirtualFile();
                                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;

                                    for (PsiClass aClass : psiJavaFile.getClasses()) {
                                        for (PsiMethod psiMethod : aClass.getMethods()) {
                                            String qualifiedName = psiMethod.getContainingClass().getQualifiedName();

                                            String encrypt = MD5Util.encrypt(qualifiedName + "." + psiMethod.getHierarchicalMethodSignature());
                                            if (methodApi.getSignature().equals(encrypt)) {
                                                //组装注释信息
                                                StringBuffer sb = new StringBuffer();
                                                sb.append("/**\n").append("* ").append(name).append("\n");
                                                for (Map<String, Object> param : params) {
                                                    String name1 = param.get("name").toString();
                                                    boolean present = methodApi.getParamList().stream().filter(o -> o.getName().equals(name1)).findAny().isPresent();
                                                    if (present) {
                                                        Object description = param.get("description");
                                                        if (Optional.ofNullable(description).isPresent()) {
                                                            sb.append("* @param ").append(param.get("name")).append(" ").append(description).append("\n");
                                                        }
                                                    }
                                                }

                                                List<Response> repsonses = Lists.newArrayList();
                                                changeToResponse(repsonses, response, -1, "");
                                                methodApi.setResponseList(repsonses);
                                                MethodApi m = ((MethodApi) ObjectUtil.deeplyCopy(methodApi));
                                                m.getClassApi().setMethodApis(null);
//                                                methodApi.getClassApi().setMethodApis(null);
                                                PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
                                                String projectTag = propertiesComponent.getValue(HttpConfigDialog.prefix + "projectTag");
                                                HttpsUtils.doPost(HttpsUtils.serverUrl + "/apiInfo/upload/"+projectTag+"/" + moduleName, new Gson().toJson(m));

//                                                sb.append("* @example ").append(new Gson().toJson(repsonses)).append("\n");

                                                sb.append("* @return \n").append("**/");


                                                //开始更新
                                                PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
                                                PsiComment comment = factory.createCommentFromText(sb.toString(), null);

                                                PsiDocComment docComment = psiMethod.getDocComment();
                                                boolean present = Optional.ofNullable(docComment).isPresent();
                                                if (present) {
                                                    docComment.replace(comment);
                                                } else {
                                                    try {
                                                        PsiModifierList modifierList = psiMethod.getModifierList();
                                                        modifierList.addBefore(comment, modifierList.getFirstChild());
                                                    } catch (Exception e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }

            }
        }

        private void changeToResponse(List<Response> repsonses, List<Map<String, Object>> response, Integer parentId, String parentPath) {
            response.forEach(m -> {
                if (m.get("parentId").equals(parentId)) {
                    Response resp = new Response();
                    resp.setDataType(m.get("type").toString());
                    if (Optional.ofNullable(m.getOrDefault("description","")).isPresent()) {
                        resp.setComment(m.getOrDefault("description","").toString());
                    }

                    String name = m.get("name").toString();
                    name = name.replaceFirst("\\|.*", "");
                    String fullPath = name;
                    if (!parentPath.equalsIgnoreCase("")) {
                        fullPath = String.format("%s.%s",parentPath, name);
                    }
                    resp.setFullPath(fullPath);

                    String replace = fullPath.replace(".", "");
                    StringBuffer indentSb  = new StringBuffer();
                    int length = fullPath.length() - replace.length();
                    for (int i = 0; i< length; i++){
                        indentSb.append("    ");
                    }
                    indentSb.append(name);
                    resp.setName(indentSb.toString());

                    int id = Integer.parseInt(m.get("id").toString());
                    List<Map<String, Object>> child = response.stream().filter(o -> o.get("parentId").equals(id)).collect(Collectors.toList());

                    if (child.size() > 0) {
                        changeToResponse(repsonses, child, id, resp.getFullPath());
                    }
                    repsonses.add(resp);
                }
            });
        }
    }
}
