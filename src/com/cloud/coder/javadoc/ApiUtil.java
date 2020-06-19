package com.cloud.coder.javadoc;

import com.cloud.coder.javadoc.info.ClassApi;
import com.cloud.coder.javadoc.info.MethodApi;
import com.cloud.coder.javadoc.info.Param;
import com.cloud.properties.PropertiesComponentUtil;
import com.cloud.ui.ApiToolComponent;
import com.cloud.util.MD5Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.javadoc.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ApiUtil {
//
//    public List<Map.Entry<String,List<ClassApi>>> generateApiByVersionGroup(String moduleName) {
//
//        List<ClassApi> classApiList = this.generateApi(moduleName);
//
//        return groupingApi(classApiList);
//    }
//
//    @NotNull
//    private List<Map.Entry<String, List<ClassApi>>> groupingApi(List<ClassApi> classApiList) {
//        Map<String, List<ClassApi>> versionApi = Maps.newHashMap();
//        for (ClassApi classApi : classApiList) {
//            Optional.ofNullable(classApi.getMethodApis()).ifPresent(apis -> {
//                Map<String, List<MethodApi>> versionApiMapping = apis.stream().collect(Collectors.groupingBy(o -> o.getVersion()));
//                for (Map.Entry<String, List<MethodApi>> versionMethodApiMaping : versionApiMapping.entrySet()) {
//                    String version = versionMethodApiMaping.getKey();
//                    ClassApi temp = new ClassApi();
//                    temp.setCreateTime(classApi.getCreateTime());
//                    temp.setAuthor(classApi.getAuthor());
//                    temp.setName(classApi.getName());
//                    temp.setPosition(classApi.getPosition());
//                    temp.setUrl(classApi.getUrl());
////                    temp.setPackageName(classApi.getPackageName());
//                    temp.setMethodApis(versionMethodApiMaping.getValue());
////                    temp.setClassName(classApi.getClassName());
//                    temp.setSignature(classApi.getSignature());
//
//                    List<ClassApi> classApis = versionApi.get(version);
//                    if (!Optional.ofNullable(classApis).isPresent()) {
//                        classApis = new ArrayList<>();
//                        versionApi.put(version, classApis);
//                    }
//
//                    classApis.add(classApi);
//                }
//            });
//        }
//
//        //这里将map.entrySet()转换成list
//        List<Map.Entry<String, List<ClassApi>>> list = new ArrayList<Map.Entry<String, List<ClassApi>>>(versionApi.entrySet());
//        //然后通过比较器来实现排序
//        Collections.sort(list, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));
//
//        return list;
//    }
//
//    public List<ClassApi> generateApi(String moduleName) {
//
//        ProjectConfig projectConfig = ApiToolComponent.projectConfig.get(moduleName);
//
//        RootDoc rootDoc = JavadocReader.readDocs(projectConfig.getScanPackage(), projectConfig.getClassPath(), projectConfig.getSourcePath(), projectConfig.getSubpackage());
//        List<ClassDoc> classDocs = Arrays.stream(rootDoc.classes()).filter(o -> {
//            boolean b = Arrays.stream(o.annotations()).anyMatch(a -> {
//                boolean restController = a.annotationType().typeName().equals("RestController");
//                boolean controller = a.annotationType().typeName().equals("Controller");
////                boolean component = a.annotationType().typeName().equals("Component");
//                return (restController || controller);
//            });
//            return b;
//        }).collect(Collectors.toList());
//
//        List<ClassApi> result = Lists.newArrayList();
//
//        for (ClassDoc classDoc : classDocs) {
//            ClassApi classApi = new ClassApi();
//
//            classApi.setName(classDoc.commentText());
//            if (Strings.isEmpty(classDoc.commentText())) {
//                classApi.setName(classDoc.name());
//            }
//            classApi.setPosition(classDoc.position());
//            classApi.setSignature(MD5Util.encrypt(classDoc.qualifiedName()));
////            classApi.setPackageName(classDoc.containingPackage().name());
////            classApi.setClassName(classDoc.name());
//
//            Tag[] authorTags = classDoc.tags("@author");
//            if (authorTags.length>0){
//                classApi.setAuthor(authorTags[0].text());
//            }
//            Tag[] createTimeTags = classDoc.tags("@createTime");
//            if (createTimeTags.length>0){
//                classApi.setCreateTime(createTimeTags[0].text());
//            }
//
//            if (classDoc.methods().length > 0) {
//                //存在方法则继续
//                //查看controller上的模块定义
//                String baseUri = "";
//                for (AnnotationDesc annotation : classDoc.annotations()) {
//                    if (annotation.annotationType().typeName().equals("RequestMapping")) {
//                        //解析注解类,获取模块路径
//                        Map annotationValues = AnalysisAnnotation(annotation);
//                        baseUri = "/"+annotationValues.get("value").toString().replace("\"","");
//                        classApi.setUrl(baseUri);
//                        break;
//                    }
//                }
//                List<MethodApi> list = AnalysisMethod(classApi,classDoc.methods(), baseUri);
//                classApi.setMethodApis(list);
//            }
//            result.add(classApi);
//        }
//
//        PropertiesComponentUtil.apiList.put(moduleName,result);
//        return result;
//    }
//
//    /**
//     * 解析方法文档
//     *
//     * @param methods
//     * @param baseUri
//     * @return
//     */
//    public List<MethodApi> AnalysisMethod(ClassApi classApi, MethodDoc[] methods, String baseUri) {
//        List<MethodApi> result = Lists.newArrayList();
//        for (MethodDoc methodDoc : methods) {
//            MethodApi methodApi = new MethodApi();
//            methodApi.setSourceCategorySignature(classApi.getSignature());
//            //解析API参数
//            for (AnnotationDesc annotation : methodDoc.annotations()) {
//                Map map = AnalysisAnnotation(annotation);
//                //判断注解类型
//                String typeName = annotation.annotationType().typeName();
//                if (typeName.equals("GetMapping")) {
//                    methodApi.setMethod("Get");
//                } else if (typeName.equals("PostMapping")) {
//                    methodApi.setMethod("Post");
//                } else if (typeName.equals("RequestMapping")) {
//                    methodApi.setMethod("All");
//                } else if (typeName.equals("DeleteMapping")) {
//                    methodApi.setMethod("Delete");
//                } else if (typeName.equals("PutMapping")) {
//                    methodApi.setMethod("Put");
//                } else if (typeName.equals("ApiVersion") && !map.isEmpty()) {
//                    String version = map.get("version").toString();
//                    version = version.substring(version.lastIndexOf(".") + 1).replaceAll("_", ".");
//                    methodApi.setVersion(version);
//                } else if (typeName.equals("Authorization") && map.get("ROLES")!=null) {
//                    methodApi.setAuthorization(map.get("ROLES").toString().replaceAll("com.cloud.core.enums.Role.",""));
//                }
//
//                if (null != methodApi.getMethod() && typeName.contains("Mapping")) {
//                    methodApi.setUrl((baseUri + "/" + map.get("value")).replace("\"", "").replaceAll("/+", "/"));
//                }
//            }
//
//            methodApi.setPosition(methodDoc.position());
//            //解析文档注释rawComment = null
//            AnalysisMethodComment(methodDoc, methodApi);
//
//            if (null != methodApi.getMethod()){
//                result.add(methodApi);
//            }
//        }
//        return result;
//    }
//
//    public void AnalysisMethodComment(MethodDoc methodDoc, MethodApi methodApi) {
//
//        //api接口描述
//        String commentText = methodDoc.commentText();
//        methodApi.setName(commentText);
//        methodApi.setMethodName(methodDoc.name());
//        String signatureSrc = String.format("%s%s%s", methodDoc.qualifiedName(), methodDoc.name(), methodDoc.flatSignature());
//        methodApi.setSignature(MD5Util.encrypt(signatureSrc));
//
//        Tag[] authorTags = methodDoc.tags("@author");
//        if (authorTags.length>0){
//            methodApi.setAuthor(authorTags[0].text());
//        }
//        Tag[] returnTags = methodDoc.tags("@return");
//        if (returnTags.length>0){
//            methodApi.setReturnName(returnTags[0].text());
//        }
//        Tag[] createTimeTags = methodDoc.tags("@createTime");
//        if (createTimeTags.length>0){
//            methodApi.setCreateTime(createTimeTags[0].text());
//        }
//
//        Map<String, String> paramMap = Maps.newConcurrentMap();
//
//        ParamTag[] paramTags = methodDoc.paramTags();
//        for (ParamTag paramTag : paramTags) {
//            paramMap.put(paramTag.parameterName(), paramTag.parameterComment());
//        }
//
//        //api参数描述
//        List<Param> parmas = Lists.newArrayList();
//        for (Parameter parameter : methodDoc.parameters()) {
//            HashSet<String> baseType = Sets.newHashSet(
//                    "int", "BigInteger", "float", "Integer", "Long", "Number","Double", "Float", "BigDecimal",
//                    "String", "Boolean",
//                    "java.lang.String[]", "java.lang.Integer[]",
//                    "Date","LocalDate","LocalDateTime",
//                    "List", "ArrayList", "java.util.ArrayList", "java.util.List","LinkedHashMap","java.util.LinkedHashMap","HashMap","java.util.HashMap","Map","java.util.Map");
//
//            HashSet<String> exclude = Sets.newHashSet("HttpServletResponse", "HttpServletRequest", "HttpSession", "BindingResult");
//            if (baseType.contains(parameter.type().simpleTypeName())) {
//                Param param = new Param();
//                param.setName(parameter.name());
//                param.setDataType(parameter.type().simpleTypeName());
//                param.setComment(paramMap.get(parameter.name()));
//                param.setParamType(Param.ParamType.Form);
//                param.setRequired(false);
//                AnalysisParamAnnotation(parameter.annotations(), param);
//                parmas.add(param);
//            } else if (!exclude.contains(parameter.typeName())){
//                ClassDoc classDoc = parameter.type().asClassDoc();
//                if (null != classDoc) {
//                    for (FieldDoc field : classDoc.fields(false)) {
//                        Param param = new Param();
//                        param.setName(field.name());
//                        param.setDataType(field.type().typeName());
//                        param.setComment(field.getRawCommentText());
//                        param.setParamType(Param.ParamType.Form);
//                        param.setRequired(false);
//                        AnalysisParamAnnotation(field.annotations(), param);
//                        parmas.add(param);
//                    }
//                }
//            }
//        }
//
//        methodApi.setParamList(parmas);
//    }
//
//    /**
//     * @description TODO
//     * @author Xps13
//     * @date 2019/5/15 9:50
//     */
//    public void AnalysisParamAnnotation(AnnotationDesc[] annotations, Param param){
//
//        for (AnnotationDesc annotationDesc : annotations) {
//            String typeName = annotationDesc.annotationType().typeName();
//            Map<String, String> map = AnalysisAnnotation(annotationDesc);
//            if (typeName.equals("RequestBody")){
//                param.setParamType(Param.ParamType.Body);
//                param.setDataType("String");
//            }else if(typeName.equals("RequestHeader")){
//                param.setDefaultValue(map.get("defaultValue"));
//                param.setParamType(Param.ParamType.Header);
//            }else if (typeName.equals("PathVariable")){
//                param.setParamType(Param.ParamType.Path);
//            }else if (typeName.equals("RequestParam")){
//                param.setDefaultValue(map.get("defaultValue"));
//                param.setParamType(Param.ParamType.Form);
//            }
//
//            String name =  map.getOrDefault("value", param.getName()).toString().replace("\"","");
//            param.setName(name);
//
//            Object required = map.getOrDefault("required","false");
//            param.setRequired(Boolean.valueOf(required.toString().replace("\"", "")));
//        }
//    }
//
//    public Map<String, String> AnalysisAnnotation(AnnotationDesc annotation) {
//        HashMap<String, String> map = Maps.newHashMap();
//        //获取注解参数值
//        AnnotationDesc.ElementValuePair[] elementValuePairs = annotation.elementValues();
//        for (AnnotationDesc.ElementValuePair valuePair : elementValuePairs) {
//            String name = valuePair.element().name();
//            String value = valuePair.value().toString();
//            map.put(name, value);
//        }
//        return map;
//    }
}
