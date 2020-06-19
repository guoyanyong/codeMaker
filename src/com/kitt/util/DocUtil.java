package com.kitt.util;

import com.cloud.coder.javadoc.info.ClassApi;
import com.cloud.coder.javadoc.info.MethodApi;
import com.cloud.coder.javadoc.info.Param;
import com.cloud.properties.PropertiesComponentUtil;
import com.cloud.tasks.GenerateJavaDocProcess;
import com.cloud.util.MD5Util;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.javadoc.PsiDocCommentImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtil;
import org.apache.http.util.Asserts;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/6/13 12:23
 */
public class DocUtil {

    private JTree tree;
    private JTabbedPane versionPane;

    public static HashSet<String> baseType = Sets.newHashSet(
            "int", "biginteger", "float", "integer", "long", "number", "double", "float", "bigdecimal",
            "string", "boolean",
            "string[]", "integer[]", "string...",
            "date", "localdate", "localdatetime",
            "list", "arraylist", "list", "linkedhashmap", "hashmap", "map",
            "multipartfile", "multipartfile[]");

    private static HashSet<String> exclude = Sets.newHashSet("HttpServletResponse", "HttpServletRequest", "HttpSession", "BindingResult");

    private Map<String, PsiClass> psiClassMap = Maps.newHashMap();

//    public void renderApiByGroupVersion(Project project, String moduleName) {
//        List<ClassApi> classApiList = getAllApiDocByModuleName(project, moduleName);
//
//        List<Map.Entry<String, List<ClassApi>>> versionGroupMap = groupingApi(classApiList);
//
//        versionPane.removeAll();
//        for (Map.Entry<String, List<ClassApi>> entry : versionGroupMap) {
//            VersionPanel versionPanel = new VersionPanel(entry.getValue(), project, moduleName);
//            versionPane.add(entry.getKey(), versionPanel.$$$getRootComponent$$$());
//        }
//    }
//
//    public void renderApi(Project project, String moduleName) {
//
////        List<ClassApi> classApiList = getAllApiDocByModuleName(project, moduleName);
////
////        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("正在解析API文档，请稍等...");
////        TreeModel treeModel = new DefaultTreeModel(rootNode);
////        tree.setModel(treeModel);
////        classApiList.stream().forEach(classApi -> {
////            DefaultMutableTreeNode apiNode = new DefaultMutableTreeNode(classApi);
////            rootNode.add(apiNode);
////            List<MethodApi> methodApi = classApi.getMethodApis();
////            if (methodApi != null) {
////                methodApi.stream().forEach(a -> {
////                    DefaultMutableTreeNode apiComment = new DefaultMutableTreeNode(a);
////                    apiNode.add(apiComment);
////                });
////            }
////        });
////        rootNode.setUserObject("所有接口列表");
////        TreePath path = new TreePath(rootNode.getPath());
////        tree.expandPath(path);
////        tree.updateUI();
//    }


    public List<Map.Entry<String, List<ClassApi>>> groupingApi(List<ClassApi> classApiList) {
        Map<String, List<ClassApi>> versionApi = Maps.newHashMap();
        for (ClassApi classApi : classApiList) {
            Optional.ofNullable(classApi.getMethodApis()).ifPresent(apis -> {
                Map<String, List<MethodApi>> versionApiMapping = apis.stream().collect(Collectors.groupingBy(o -> o.getVersion()));
                for (Map.Entry<String, List<MethodApi>> versionMethodApiMaping : versionApiMapping.entrySet()) {
                    String version = versionMethodApiMaping.getKey();
                    ClassApi temp = new ClassApi();
                    temp.setCreateTime(classApi.getCreateTime());
                    temp.setAuthor(classApi.getAuthor());
                    temp.setName(classApi.getName());
                    temp.setPosition(classApi.getPosition());
                    temp.setUrl(classApi.getUrl());
//                    temp.setPackageName(classApi.getPackageName());
                    temp.setMethodApis(versionMethodApiMaping.getValue());
//                    temp.setClassName(classApi.getClassName());
                    temp.setSignature(classApi.getSignature());

                    List<ClassApi> classApis = versionApi.get(version);
                    if (!Optional.ofNullable(classApis).isPresent()) {
                        classApis = new ArrayList<>();
                        versionApi.put(version, classApis);
                    }

                    classApis.add(classApi);
                }
            });
        }

        //这里将map.entrySet()转换成list
        List<Map.Entry<String, List<ClassApi>>> list = new ArrayList<Map.Entry<String, List<ClassApi>>>(versionApi.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));

        return list;
    }

    /**
     * 获取所有API文档列表
     *
     * @param project
     * @param moduleName
     * @return void
     * @author Xps13
     * @createTime 2019/6/13 12:27
     */
    public List<ClassApi> getAllApiDocByModuleName(Project project, String moduleName, GenerateJavaDocProcess generateJavaDocProcess) {
        Collection<VirtualFile> javaFileList = FilenameIndex.getAllFilesByExt(project, "java", GlobalSearchScope.moduleScope(PropertiesComponentUtil.moduleMapping.get(moduleName)));
        Asserts.notNull(javaFileList, "未搜索到任何文件列表");
        List<ClassApi> classApiList = Lists.newArrayList();

        HashMap<PsiClass, VirtualFile> virtualFileMap = Maps.newHashMap();
        List<PsiClass> psiClassList = Lists.newArrayList();
        for (VirtualFile virtualFile : javaFileList) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            generateJavaDocProcess.setTitle(String.format("正在分析文件【%s】...", psiFile.getName()));
//            try {
//                Thread.sleep(1000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            final PsiClass[] classes = psiJavaFile.getClasses();
            for (PsiClass aClass : classes) {
                boolean isHas = aClass.hasAnnotation("org.springframework.stereotype.Controller")
//                        || aClass.hasAnnotation("org.springframework.stereotype.Component")
                        || aClass.hasAnnotation("org.springframework.web.bind.annotation.ControllerAdvice")
                        || aClass.hasAnnotation("org.springframework.web.bind.annotation.RestControllerAdvice")
                        || aClass.hasAnnotation("org.springframework.web.bind.annotation.RestController");
                if (isHas) {
                    psiClassList.add(aClass);
                    virtualFileMap.put(aClass, virtualFile);
                } else {
                    psiClassMap.put(aClass.getQualifiedName(), aClass);
                }
            }
        }

        for (PsiClass psiClass : psiClassList) {
            ClassApi classApi = getClassApi(psiClass, virtualFileMap.get(psiClass));
            classApi.setProject(project);
            classApiList.add(classApi);
        }

        PropertiesComponentUtil.apiList.put(moduleName, classApiList);

        return classApiList;
    }

    /**
     * 获取controller类的注释api
     *
     * @param aClass
     * @return com.cloud.coder.javadoc.info.ClassApi
     * @author Xps13
     * @createTime 2019/6/13 12:40
     */
    private ClassApi getClassApi(PsiClass aClass, VirtualFile virtualFile) {
        ClassApi classApi = new ClassApi();
        PsiDocComment docComment = aClass.getDocComment();

        String comment = getComment(docComment);
        classApi.setName(comment);
        if (Strings.isNullOrEmpty(comment)) {
            classApi.setName(aClass.getName());
        }

        classApi.setAuthor(getTagValueByName(docComment, "author"));
        classApi.setCreateTime(getTagValueByName(docComment, "createtime"));
        classApi.setSignature(MD5Util.encrypt(aClass.getQualifiedName()));
//        classApi.setClassName(aClass.getName());
        classApi.setVirtualFile(virtualFile);
        classApi.setTextOffset(aClass.getTextOffset());

        if (aClass.hasAnnotation("org.springframework.web.bind.annotation.RequestMapping")) {
            PsiAnnotation annotation = aClass.getAnnotation("org.springframework.web.bind.annotation.RequestMapping");
            Map<String, String> annotationValues = getAnnotationValues(annotation);
            classApi.setUrl(annotationValues.get("value"));
        }

        List<MethodApi> methodApiList = getMehtodApi(aClass, classApi);
        classApi.setMethodApis(methodApiList);
        return classApi;
    }

    private Map<String, String> getAnnotationValues(PsiAnnotation annotation) {
        Map<String, String> map = Maps.newHashMap();
        for (JvmAnnotationAttribute attribute : annotation.getAttributes()) {
            String attributeName = attribute.getAttributeName();
            String attributeValue = attribute.getAttributeValue().getSourceElement().getText().replace("\"", "");
            map.put(attributeName, attributeValue);
        }
        return map;
    }

    private List<MethodApi> getMehtodApi(PsiClass aClass, ClassApi classApi) {
        List<MethodApi> methodApiList = Lists.newArrayList();

        MethodApi methodApi = null;
        for (PsiMethod method : aClass.getMethods()) {
            methodApi = makeMehtodApi(aClass, classApi, method);
            Optional.ofNullable(methodApi).ifPresent(m -> {
                methodApiList.add(m);
            });
        }
        return methodApiList;
    }

    public MethodApi makeMehtodApi(PsiClass aClass, ClassApi classApi, PsiMethod method) {
        MethodApi methodApi;
        PsiDocComment docComment = method.getDocComment();
//            if (!Optional.ofNullable(docComment).isPresent()) {
//                continue;
//            }
        methodApi = new MethodApi();

        //解析URL 及 访问的method
        for (PsiAnnotation annotation : method.getAnnotations()) {
            Map<String, String> map = getAnnotationValues(annotation);
            //判断注解类型
            String typeName = annotation.getQualifiedName();
            if (typeName.equals("org.springframework.web.bind.annotation.GetMapping")) {
                methodApi.setMethod("Get");
            } else if (typeName.equals("org.springframework.web.bind.annotation.PostMapping")) {
                methodApi.setMethod("Post");
            } else if (typeName.equals("org.springframework.web.bind.annotation.RequestMapping")) {
                methodApi.setMethod("All");
            } else if (typeName.equals("org.springframework.web.bind.annotation.DeleteMapping")) {
                methodApi.setMethod("Delete");
            } else if (typeName.equals("org.springframework.web.bind.annotation.PutMapping")) {
                methodApi.setMethod("Put");
            } else if (typeName.equals("com.cloud.core.config.version.ApiVersion") && !map.isEmpty()) {
                String version = map.get("version");
                if (Optional.ofNullable(version).isPresent()) {
                    version = version.substring(version.lastIndexOf(".") + 1).replaceAll("_", ".");
                    methodApi.setVersion(version);
                }
            } else if (typeName.equals("com.cloud.core.annotation.Authorization") && map.get("ROLES") != null) {
                methodApi.setAuthorization(map.get("ROLES").replaceAll("com.cloud.core.enums.Role.", ""));
            }

            if (null != methodApi.getMethod() && typeName.contains("Mapping")) {
                String baseUrl = "";
                if (classApi.getUrl() != null) {
                    baseUrl = classApi.getUrl() + "/";
                }
                methodApi.setUrl((baseUrl + map.get("value")).replace("\"", "").replaceAll("/+", "/"));
            }
        }
        boolean present = Optional.ofNullable(methodApi.getUrl()).isPresent();
        boolean onlyOneInClass = aClass.getMethods().length == 1;
        boolean classUrl = Optional.ofNullable(classApi.getUrl()).isPresent();
        if (present || (onlyOneInClass && classUrl)) {

            methodApi.setClassApi(classApi);
            methodApi.setSourceCategorySignature(classApi.getSignature());
            methodApi.setName(getComment(docComment));
            method.getSignature(PsiSubstitutor.EMPTY);
            methodApi.setAuthor(getTagValueByName(docComment, "author"));
            methodApi.setCreateTime(getTagValueByName(docComment, "createtime"));
            methodApi.setVirtualFile(classApi.getVirtualFile());
            methodApi.setTextOffset(method.getTextOffset());
            methodApi.setSignature(MD5Util.encrypt(method.getContainingClass().getQualifiedName() + "." + method.getHierarchicalMethodSignature()));

            Map<String, String> paramMap = getTagsValueByName(docComment, "param");
            PsiParameterList parameterList = method.getParameterList();

            for (PsiParameter parameter : parameterList.getParameters()) {

                String type = parameter.getType().toString().split(":")[1];
                type = type.replaceAll("<.*>", "");
                String name = parameter.getName();

                if (baseType.contains(type.toLowerCase())) {
                    Optional<Param> any = methodApi.getParamList().stream().filter(o -> o.getName().equals(name)).findAny();
                    if (!any.isPresent()) {
                        Param param = new Param();
                        param.setName(name);
                        param.setDataType(type);
                        param.setComment(paramMap.get(name));
                        param.setParamType(Param.ParamType.Form);
                        param.setRequired(false);

                        getAnntationForParam(parameter.getAnnotations(), param);
                        methodApi.getParamList().add(param);
                    }
                } else if (!exclude.contains(type)) {
                    String qualifiedName = parameter.getTypeElement().getInnermostComponentReferenceElement().getQualifiedName();

                    // todo 参数加了“@valid”注解后，解析对象属性为参数时，需要标注是否是必需参数。 parameter.getAnnotations()
                    PsiClass psiClass = psiClassMap.get(qualifiedName);

                    if (!Optional.ofNullable(psiClass).isPresent()) {
                        psiClass = PsiUtil.resolveClassInType(parameter.getType());
                    }

                    for (PsiField field : psiClass.getAllFields()) {
                        Optional<Param> any = methodApi.getParamList().stream().filter(o -> o.getName().equals(field.getName())).findAny();
                        if (!any.isPresent()) {
                            Param param = new Param();
                            param.setName(field.getName());
                            param.setDataType(field.getType().getPresentableText());

                            String comment = paramMap.get(param.getName()) == null ? "" : paramMap.get(param.getName());
                            if (!comment.trim().equals("")) {
                                param.setComment(comment);
                            } else {
                                PsiDocComment paramPsiDoc = field.getDocComment();
                                Optional.ofNullable(paramPsiDoc).ifPresent(doc -> {
                                    ASTNode[] children = ((PsiDocCommentImpl) doc).getChildren(TokenSet.create(JavaDocTokenType.DOC_COMMENT_DATA));
                                    StringBuffer commentSb = new StringBuffer();
                                    for (ASTNode child : children) {
                                        String text = child.getText();
                                        commentSb.append(text.trim());
                                    }
                                    param.setComment(commentSb.toString());
                                });
                            }
                            param.setParamType(Param.ParamType.Form);
                            param.setRequired(false);
                            getAnntationForParam(field.getAnnotations(), param);

                            methodApi.getParamList().add(param);
                        }
                    }
                }
            }

            return methodApi;
        }
        return null;
    }

    private void getAnntationForParam(PsiAnnotation[] anntations, Param param) {

        for (PsiAnnotation annotation : anntations) {
            Map<String, String> map = getAnnotationValues(annotation);
            String typeName = annotation.getQualifiedName();
            if (typeName.equals("org.springframework.web.bind.annotation.RequestBody")) {
                param.setParamType(Param.ParamType.Body);
                param.setDataType("String");
            } else if (typeName.equals("org.springframework.web.bind.annotation.RequestHeader")) {
                param.setDefaultValue(map.get("defaultValue"));
                param.setParamType(Param.ParamType.Header);
            } else if (typeName.equals("org.springframework.web.bind.annotation.PathVariable")) {
                param.setParamType(Param.ParamType.Path);
            } else if (typeName.equals("org.springframework.web.bind.annotation.RequestParam")) {
                param.setDefaultValue(map.get("defaultValue"));
                param.setParamType(Param.ParamType.Form);
            } else if (typeName.equals("javax.validation.constraints.NotNull")
                    || typeName.equals("org.hibernate.validator.constraints.NotBlank")
                    || typeName.equals("org.hibernate.validator.constraints.NotEmpty")) {
                param.setRequired(true);
            }

            String paramNam = map.getOrDefault("value", param.getName());
            param.setName(paramNam);

            if (!param.getRequired()) {
                Object required = map.getOrDefault("required", "true");
                param.setRequired(Boolean.valueOf(required.toString()));
            }

        }
    }

    private String getTagValueByName(PsiDocComment docComment, String tagName) {
        if (!Optional.ofNullable(docComment).isPresent()) {
            return null;
        }
        PsiDocTag tag = docComment.findTagByName(tagName);
        if (!Optional.ofNullable(tag).isPresent()) {
            return null;
        }
        PsiDocTagValue valueElement = tag.getValueElement();
        StringBuffer value = new StringBuffer(valueElement.getFirstChild().getText());
        if (Optional.ofNullable(valueElement.getNextSibling()).isPresent()) {
            value.append(valueElement.getNextSibling().getText());
        }
        return value.toString();
    }

    private Map<String, String> getTagsValueByName(PsiDocComment docComment, String tagName) {
        Map<String, String> map = Maps.newHashMap();
        if (Optional.ofNullable(docComment).isPresent()) {
            PsiDocTag[] params = docComment.findTagsByName("param");
            for (PsiDocTag param : params) {
                Optional.ofNullable(param.getValueElement()).ifPresent(e -> {
                    String name = param.getValueElement().getText();
                    StringBuffer psb = new StringBuffer();
                    for (PsiElement dataElement : param.getDataElements()) {
                        if (dataElement.getNode().getElementType().equals(JavaDocTokenType.DOC_COMMENT_DATA)) {
                            psb.append(dataElement.getText());
                        }
                    }
                    map.put(name, psb.toString());
                });
            }
        }
        return map;
    }

    /**
     * 获取基本文本注释
     *
     * @param docComment
     * @return java.lang.String
     * @author Xps13
     * @createTime 2019/6/13 12:49
     */
    private String getComment(PsiDocComment docComment) {
        StringBuffer csb = new StringBuffer("");
        Optional.ofNullable(docComment).ifPresent(doc -> {
            PsiElement[] children = docComment.getChildren();
            for (PsiElement child : children) {
                String text = child.getText();
                if (child.getNode().getElementType().equals(JavaDocTokenType.DOC_COMMENT_DATA)) {
                    csb.append(text);
                }
            }
        });

        return csb.toString().trim();
    }

    public DocUtil setTree(JTree tree) {
        this.tree = tree;
        return this;
    }

    public DocUtil setVersionPane(JTabbedPane versionPane) {
        this.versionPane = versionPane;
        return this;
    }

    public JTree getTree() {
        return tree;
    }

    public JTabbedPane getVersionPane() {
        return versionPane;
    }
}
