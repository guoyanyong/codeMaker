package com.cloud;

import com.cloud.ui.ApiToolComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class ApiToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ApiToolComponent component = ApiToolComponent.getInstance(project);
        component.initWindow(toolWindow);
    }
}
