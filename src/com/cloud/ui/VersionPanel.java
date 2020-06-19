package com.cloud.ui;

import com.cloud.coder.javadoc.info.ApiBaseInfo;
import com.cloud.coder.javadoc.info.ClassApi;
import com.cloud.coder.javadoc.info.MethodApi;
import com.cloud.tasks.GenerateJavaDocProcess;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.kitt.util.DocUtil;
import com.sun.javadoc.SourcePosition;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VersionPanel {
    private JPanel panel1;
    private JTree tree1;
    private JScrollPane scrollPanelApi;
    private JTabbedPane apiDetailTab;
    private JSplitPane apiPanel;

    private String moduleName;
    private Project project;

    public VersionPanel(String moduleName, Project project) {
        this.project = project;
        this.moduleName = moduleName;


        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("正在解析API文档，请稍等...");
        tree1.setModel(new DefaultTreeModel(rootNode));

        scrollPanelApi.getViewport().setView(tree1);

        new BackgroundTaskQueue(project, "正在分析文档...").run(new GenerateJavaDocProcess(project, "正在分析文档...", moduleName, new DocUtil().setTree(tree1)));

        registerListener();
    }

    public VersionPanel(List<ClassApi> classApiList, Project project, String moduleName) {
        this.project = project;
        this.moduleName = moduleName;

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("正在加载API文档，请稍等...");

        TreeModel treeModel = new DefaultTreeModel(rootNode);
        tree1.setModel(treeModel);
        scrollPanelApi.getViewport().setView(tree1);

        classApiList.stream().forEach(o -> {
            DefaultMutableTreeNode apiNode = new DefaultMutableTreeNode(o);
            rootNode.add(apiNode);
            rootNode.setUserObject(o);
            List<MethodApi> methodApiList = o.getMethodApis();
            if (methodApiList != null) {
                methodApiList.stream().forEach(a -> {
                    DefaultMutableTreeNode apiComment = new DefaultMutableTreeNode(a);
                    apiComment.setUserObject(a);
                    apiNode.add(apiComment);
                });
            }
        });
        rootNode.setUserObject("所有接口列表");
        TreePath path = new TreePath(rootNode.getPath());
        tree1.expandPath(path);
        tree1.updateUI();

        registerListener();
    }

    private void registerListener() {
        apiDetailTab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = apiDetailTab.indexAtLocation(e.getX(), e.getY());
                    if (index != -1) {
                        apiDetailTab.remove(index);
                    }
                }
            }
        });

        tree1.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getPath();
                openApiDetailTab(path);
            }
        });

        tree1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switch (e.getButton()) {
                    case 1: //单击
                        TreePath pathForLocation = tree1.getPathForLocation(e.getX(), e.getY());
                        openApiDetailTab(pathForLocation);
                        break;
                    case 3: //右击
//                        openApiSource(e);
                        openApiSourceForPsi(e);
                        break;
                }
            }
        });
    }

    private void openApiDetailTab(TreePath treePath) {

        boolean present = Optional.ofNullable(treePath).isPresent();
        if (present) {
            DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            Object userObject = lastPathComponent.getUserObject();
            String className = userObject.getClass().getSimpleName();
            if (!className.equalsIgnoreCase("methodApi")) {
                return;
            }
            MethodApi mApi = (MethodApi) userObject;
            if (mApi.getMethod() == null || mApi.getUrl() == null) {
                return;
            }
            String comment = mApi.getName();


            //环境变量选择
            Map<String, String> varMap = Maps.newHashMap();
            PropertiesComponent properties = PropertiesComponent.getInstance(project);
            String value = properties.getValue(HttpConfigDialog.prefix + moduleName);
            if (Optional.ofNullable(value).isPresent()) {
                varMap = new Gson().fromJson(value, HashMap.class);
            }

            mApi.setProject(project);
            ApiDetail apiDetail = new ApiDetail(moduleName, mApi, varMap, lastPathComponent, tree1);
            ImageIcon imageIcon = mApi.getIcon();
            int i = apiDetailTab.indexOfTab(imageIcon);
            if (i > -1) {
                apiDetailTab.setSelectedIndex(i);
            } else {

                if (StringUtils.isEmpty(comment)) {
                    comment = mApi.getUrl();
                }

                String title = comment.length() > 10 ? comment.substring(0, 8) + "..." : comment;
                apiDetailTab.addTab(title, imageIcon, apiDetail.getPanel1(), comment);
                //默认选中
                int index = apiDetailTab.indexOfTab(imageIcon);
                apiDetailTab.setSelectedIndex(index);
                PanelTabWithClose tabWithClose = new PanelTabWithClose(title, apiDetailTab, apiDetail.getPanel1());
                apiDetailTab.setTabComponentAt(index, tabWithClose.getUi());
            }
        }
    }


    private void openApiSource(MouseEvent e) {
        TreePath pathForLocation = tree1.getPathForLocation(e.getX(), e.getY());
        boolean present = Optional.ofNullable(pathForLocation).isPresent();
        if (present) {
            DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
            ApiBaseInfo baseInfo = (ApiBaseInfo) lastPathComponent.getUserObject();
            SourcePosition position = baseInfo.getPosition();

            VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(position.file());
            OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, file, position.line(), 0);
            openFileDescriptor.navigate(true);
        }
    }

    private void openApiSourceForPsi(MouseEvent e) {
        TreePath pathForLocation = tree1.getPathForLocation(e.getX(), e.getY());
        boolean present = Optional.ofNullable(pathForLocation).isPresent();
        if (present) {
            DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
            ApiBaseInfo baseInfo = (ApiBaseInfo) lastPathComponent.getUserObject();
            int startOffset = baseInfo.getTextOffset();

            OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(baseInfo.getProject(), baseInfo.getVirtualFile(), startOffset);
            openFileDescriptor.navigate(true);

//            Collection<VirtualFile> virtualFilesByName = FilenameIndex.getVirtualFilesByName(project, baseInfo.getQualifiedName(), GlobalSearchScope.EMPTY_SCOPE);
//            VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(position.file());
//            PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(baseInfo.getQualifiedName(), GlobalSearchScope.moduleScope(PropertiesComponentUtil.moduleMapping.get(moduleName)));
//            PsiFile[] filesByName = FilenameIndex.getFilesByName(project, baseInfo.getQualifiedName(), GlobalSearchScope.moduleScope(PropertiesComponentUtil.moduleMapping.get(moduleName)));
//            PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, ((ClassApi) baseInfo).getClassName() + ".java", GlobalSearchScope.moduleScope(PropertiesComponentUtil.moduleMapping.get(moduleName)));

//            for (PsiFile psiFile : psiFiles) {
//
//            }
//
//
//            Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
//
//            Document document = selectedTextEditor.getDocument();
//            CaretModel caretModel = selectedTextEditor.getCaretModel();
//
////            caretModel.moveToOffset(openFileDescriptor.getLine());
//            //计算插入字符的位置
//            int lineStartOffset = document.getLineStartOffset(caretModel.getLogicalPosition().line - 2);
//            System.out.println(caretModel.getLogicalPosition().line);
//            System.out.println(lineStartOffset);
//            int lineEndOffset = document.getLineEndOffset(caretModel.getLogicalPosition().line);
//            System.out.println(lineEndOffset);
//            caretModel.moveToOffset(lineEndOffset);

//            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
//            PsiElement elementAt = psiFile.findElementAt(lineStartOffset);
//            PsiMethod method = PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class);
//
//            String javadoc = "/**\n" +
//                    "     * Add or replaces the javadoc name to the given method.\n" +
//                    "     *\n" +
//                    "     * @param method           the method the javadoc should be added/set to.\n" +
//                    "     * @param javadoc          the javadoc name.\n" +
//                    "     * @param replace          true if any existing javadoc should be replaced. false will not replace any existing javadoc and thus leave the javadoc untouched.\n" +
//                    "     * @return the added/replace javadoc name, null if the was an existing javadoc and it should <b>not</b> be replaced.\n" +
//                    "     * @throws IncorrectOperationException is thrown if error adding/replacing the javadoc name.\n" +
//                    "     */";
//            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
//            PsiComment name = factory.createCommentFromText(javadoc, null);
//
//            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
//                @Override
//                public void run() {
//                    method.getDocComment().replace(name);
////                    document.insertString(lineStartOffset, "//dddddddddddddddddddddddddddddddddddddddd\n");
////                    openFileDescriptor.dispose();
//                }
//            });


        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        apiPanel = new JSplitPane();
        panel1.add(apiPanel, BorderLayout.CENTER);
        scrollPanelApi = new JScrollPane();
        scrollPanelApi.setAutoscrolls(true);
        scrollPanelApi.setEnabled(true);
        scrollPanelApi.setMinimumSize(new Dimension(100, 15));
        scrollPanelApi.setPreferredSize(new Dimension(400, 360));
        scrollPanelApi.setVerticalScrollBarPolicy(20);
        apiPanel.setLeftComponent(scrollPanelApi);
        tree1 = new JTree();
        tree1.setAutoscrolls(true);
        tree1.setMinimumSize(new Dimension(200, 0));
        scrollPanelApi.setViewportView(tree1);
        apiDetailTab = new JTabbedPane();
        apiPanel.setRightComponent(apiDetailTab);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
