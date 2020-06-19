package com.cloud.ui;

import com.cloud.properties.PropertiesComponentUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddProjectConfig extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField moduleName;
    private JTextField scanPackage;
    private JComboBox selectProject;
    private JTextField subpackages;

    private JTabbedPane projectTab;

    private Project project;

    public AddProjectConfig(JTabbedPane projectTab, Project project) {
        this.project = project;
        this.projectTab = projectTab;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);


        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        initSelectProject();

        setSize(1000, 400);
        setTitle("添加项目配置");
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }

    private void initSelectProject() {
        List<String> projectList = PropertiesComponentUtil.getProjectList(project);
        selectProject.addItem("请选择...");
        projectList.stream().forEach(o -> selectProject.addItem(o));

        selectProject.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    initForm();
                }
            }
        });
    }

    private void initForm() {
        String selectedMoudleName = selectProject.getSelectedItem().toString();
        moduleName.setText(selectedMoudleName);//设置项目名称
    }

    private void onOK() {

        Module module = PropertiesComponentUtil.moduleMapping.get(selectProject.getSelectedItem().toString());
        ModuleRootManager manager = ModuleRootManager.getInstance(module);

        //初始化“源代码路径”
        List<String> sourceList = new ArrayList<>(Arrays.asList(manager.getSourceRootUrls(false)));

        //初始化“字节码路径”
        List<String> classList = new ArrayList<>(Arrays.asList(manager.getExcludeRootUrls()));

        //添加依赖Jar包的路径 到 classPath 中
        manager.orderEntries().forEachLibrary(library -> {
            VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
            for (VirtualFile file : files) {
                classList.add(file.getPath().replace("!/", ""));
            }
            return true;
        });

        for (Module dependency : manager.getDependencies(false)) {
            ModuleRootManager instance = ModuleRootManager.getInstance(dependency);
            //添加依赖 模块 的 “字节码路径”
            classList.addAll(Arrays.asList(instance.getExcludeRootUrls()));
            //添加依赖 模块 的 “源代码路径”
            sourceList.addAll(Arrays.asList(instance.getSourceRootUrls()));
        }

        // 获取 project-components “ApiToolComponent”
        ApiToolComponent component = project.getComponent(ApiToolComponent.class);
        //调用添加项目方法
        String classpath = StringUtil.join(classList, ";").replaceAll("file://", "");
        String sourcepath = StringUtils.join(sourceList, ";").replaceAll("file://", "");

        component.addProjectTab(moduleName.getText(), scanPackage.getText(), classpath, sourcepath, subpackages.getText(), project);

        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
//        AddProjectConfig dialog = new AddProjectConfig();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
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
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("确定");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("取消");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(800, 200), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("项目名称：");
        CellConstraints cc = new CellConstraints();
        panel3.add(label1, cc.xy(1, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        moduleName = new JTextField();
        panel3.add(moduleName, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label2 = new JLabel();
        label2.setText("扫描源码包名：");
        label2.setVisible(false);
        panel3.add(label2, cc.xy(1, 5));
        scanPackage = new JTextField();
        scanPackage.setText("com.cloud");
        scanPackage.setVisible(false);
        panel3.add(scanPackage, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        selectProject = new JComboBox();
        panel3.add(selectProject, cc.xy(3, 1));
        final JLabel label3 = new JLabel();
        label3.setText("选择已有项目：");
        panel3.add(label3, cc.xy(1, 1));
        final JLabel label4 = new JLabel();
        label4.setText("扫描源码子包：");
        label4.setVisible(false);
        panel3.add(label4, cc.xy(1, 7));
        subpackages = new JTextField();
        subpackages.setText("com.cloud.modules");
        subpackages.setVisible(false);
        panel3.add(subpackages, cc.xy(3, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
