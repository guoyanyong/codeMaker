package com.cloud.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpConfigDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable valuesTable;
    private JButton addVar;
    private JList moduleList;
    private JCheckBox isGroupApi;
    private JCheckBox packageGroupApiTree;
    private JTextField projectTag;

    public static String prefix = "code.api.http.prefix.";
    private PropertiesComponent properties;

    public HttpConfigDialog(Project project) {
        this.properties = PropertiesComponent.getInstance(project);
        String value = properties.getValue(HttpConfigDialog.prefix + "isGroupApi");
        if (Optional.ofNullable(value).isPresent()) {
            if (value.equalsIgnoreCase("true")) {
                isGroupApi.setSelected(true);
            }
        }

        String projectTageValue = properties.getValue(prefix + "projectTag");
        projectTag.setText(projectTageValue);


        Module[] modules = ModuleManager.getInstance(project).getModules();
        DefaultListModel listModel = new DefaultListModel();
        for (Module module : modules) {
            listModel.addElement(module.getName());
        }
        moduleList.setModel(listModel);

        //渲染参数列表
        String[] titles = {"参数名称", "参数值"};
        DefaultTableModel tableModel = new DefaultTableModel(null, titles);
        renderTable(tableModel);
        valuesTable.setRowHeight(25);
        valuesTable.setModel(tableModel);


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
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        addVar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Optional.ofNullable(moduleList.getSelectedValue()).isPresent()) {
                    Messages.showWarningDialog("请选中左侧列表项", "操作提醒");
                } else {
                    tableModel.addRow(new String[]{});
                }
            }
        });

        moduleList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    renderTable(tableModel);
                }
            }
        });

        setTitle("访问环境配置");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void renderTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        Object selectedValue = moduleList.getSelectedValue();
        Optional.ofNullable(selectedValue).ifPresent(v -> {
            String value = properties.getValue(prefix + v.toString());
            Optional.ofNullable(value).ifPresent(o -> {
                Map<String, String> jsonObject = new Gson().fromJson(value, HashMap.class);
                for (String s : jsonObject.keySet()) {
                    tableModel.addRow(new String[]{s, jsonObject.get(s)});
                }
                valuesTable.setModel(tableModel);
            });
        });
    }

    private void onOK() {
        // add your code here
        TableCellEditor cellEditor = valuesTable.getCellEditor();
        Optional.ofNullable(cellEditor).ifPresent(o -> {
            cellEditor.stopCellEditing();
        });
        int rowCount = valuesTable.getRowCount();
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < rowCount; i++) {
            Object name = valuesTable.getValueAt(i, 0);
            Object value = valuesTable.getValueAt(i, 1);
            Optional.ofNullable(name).ifPresent(o -> {
                String v = Optional.ofNullable(value).isPresent() ? value.toString() : "";
                jsonObject.addProperty(o.toString(), v);
            });
        }
        properties.setValue(prefix + moduleList.getSelectedValue(), jsonObject.toString());
        properties.setValue(prefix + "isGroupApi", isGroupApi.isSelected());
        properties.setValue(prefix + "projectTag", projectTag.getText());
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
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
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setPreferredSize(new Dimension(1000, 500));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, BorderLayout.WEST);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, -1), null, 0, false));
        moduleList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        moduleList.setModel(defaultListModel1);
        moduleList.setSelectionMode(0);
        scrollPane1.setViewportView(moduleList);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, BorderLayout.SOUTH);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel4.add(panel5, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel5.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel5.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("配置远程服务器的project标识：");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        projectTag = new JTextField();
        panel4.add(projectTag, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel4.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(0, 0));
        panel1.add(panel6, BorderLayout.CENTER);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel7, BorderLayout.NORTH);
        addVar = new JButton();
        addVar.setText("添加环境变量");
        panel7.add(addVar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel7.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        isGroupApi = new JCheckBox();
        isGroupApi.setText("按版本分组API");
        panel7.add(isGroupApi, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        packageGroupApiTree = new JCheckBox();
        packageGroupApiTree.setText("按包结构显示API树");
        packageGroupApiTree.setToolTipText("按照源代码包的结构路径进行API树的分组显示");
        panel7.add(packageGroupApiTree, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setPreferredSize(new Dimension(500, 420));
        panel6.add(scrollPane2, BorderLayout.CENTER);
        valuesTable = new JTable();
        valuesTable.setAutoResizeMode(3);
        scrollPane2.setViewportView(valuesTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
