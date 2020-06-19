package com.cloud.properties;

import com.cloud.coder.javadoc.info.ClassApi;
import com.google.common.collect.Lists;
import com.intellij.designer.model.PropertyContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 * @author Xps13
 * @date 2019/5/13 13:44
 */
public class PropertiesComponentUtil implements PropertyContext {

    public static Map<String, Module> moduleMapping = new HashMap<>();
    public static Map<String, List<ClassApi>> apiList = new HashMap<>();

    public static List<String> getProjectList(Project project){
        List<String> projectList = Lists.newArrayList();

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            String name = module.getName();
            projectList.add(name);
            moduleMapping.put(name, module);
        }
        return projectList;
    }
}
