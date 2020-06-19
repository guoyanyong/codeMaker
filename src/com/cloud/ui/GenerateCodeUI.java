package com.cloud.ui;

import com.cloud.coder.db.DBConnectInfo;
import com.cloud.coder.db.Generater;
import com.cloud.coder.db.TableMeta;
import com.cloud.util.DBTool;
import com.cloud.util.DataConversion;
import com.cloud.util.MyAbstractTableModel;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class GenerateCodeUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox dbType;
    private JTextField username;
    private JTextField driverClass;
    private JTextField url;
    private JTextField password;
    private JTextField baseClassPath;
    private JComboBox template;
    private JTextField codeDirectory;
    private JRadioButton defautRadioButton;
    private JRadioButton underlineRadioButton;
    private JRadioButton pointsRadioButton;
    private JRadioButton humpRadioButton;
    private JProgressBar progressBar1;
    private JTextArea console;
    private JButton nextButton;
    private JTable databaseTables;
    private JPanel cardPanel;
    private ButtonGroup modelGenerateType = new ButtonGroup();

    public GenerateCodeUI(DBConnectInfo connectInfo, AnActionEvent e) {
        url.setText(connectInfo.getUrl());
        url.setCaretPosition(0);
        driverClass.setText("com.mysql.jdbc.Driver");
        driverClass.setCaretPosition(0);
        username.setText(connectInfo.getUsername());
        username.setCaretPosition(0);
        password.setText(connectInfo.getPassword());
        password.setCaretPosition(0);

        modelGenerateType.add(defautRadioButton);
        modelGenerateType.add(underlineRadioButton);
        modelGenerateType.add(pointsRadioButton);
        modelGenerateType.add(humpRadioButton);

        Module data = e.getDataContext().getData(DataKeys.MODULE);
        ModuleRootManager rootManager = ModuleRootManager.getInstance(data);

        String[] sourceRootUrls = rootManager.getSourceRootUrls(false);
        for (String sourceRootUrl : sourceRootUrls) {
            if (sourceRootUrl.endsWith("/java")) {
                codeDirectory.setText(sourceRootUrl.replaceFirst("file://", ""));
                codeDirectory.setCaretPosition(0);
            }
        }

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String string = progressBar1.getString();
                if (!string.equals("") && !string.startsWith("任务完成")) {
                    Messages.showInfoMessage("任务尚未完成，请稍后离开", "提示");
                    return;
                }
                onCancel();
            }
        });

        progressBar1.setVisible(false);
        progressBar1.setForeground(Color.green);
        progressBar1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JProgressBar pb = (JProgressBar) e.getSource();
                double percentComplete = pb.getPercentComplete();
                if (Double.valueOf(1.0).equals(percentComplete)) {
                    Messages.showInfoMessage("生成完成", "提示");
                    dispose();
                }
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
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadDatabase();
            }
        });

        pack();
        setTitle("代码生成器");
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadDatabase() {
        // add your code here
        Generater.baseClassPath = baseClassPath.getText().trim();
        Generater.codeDirectory = codeDirectory.getText().trim();
        Generater.template = template.getSelectedItem().toString();
        Generater.modelGenerateType = "points";

        String driverClassText = driverClass.getText().trim();
        String urlText = url.getText().trim();
        String usernameText = username.getText().trim();
        String passwordText = password.getText().trim();

        if (Strings.isNullOrEmpty(driverClassText) || Strings.isNullOrEmpty(urlText) || Strings.isNullOrEmpty(usernameText) || Strings.isNullOrEmpty(passwordText)) {
            Messages.showErrorDialog("请检查数据库连接参数！", "参数错误");
            return;
        }

        if (Strings.isNullOrEmpty(Generater.baseClassPath)) {
            Messages.showErrorDialog("请设置基础包路径后重试！", "设置错误");
            return;
        }

        new DBTool("mysql", driverClassText, urlText, usernameText, passwordText);

        try {
            List<TableMeta> tables = DBTool.getAllTableName();
            console.append("正在读取数据库...,请稍等\n");

            //渲染列表
            MyAbstractTableModel tableModel = new MyAbstractTableModel(new String[]{"表名称", "表描述", "是否截取表前缀"});
            for (TableMeta table : tables) {
                Object[] rowData = {table.getTableName(), table.getRemarks(), table.isCutPrefix()};
                tableModel.addRow(rowData);
            }
            //渲染列表
            JTableHeader head = databaseTables.getTableHeader(); // 创建表格标题对象
            head.setPreferredSize(new Dimension(head.getWidth(), 35));// 设置表头大小
            databaseTables.setModel(tableModel);
            databaseTables.setRowHeight(30);
            databaseTables.setTableHeader(head);
            databaseTables.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        CardLayout layout = (CardLayout) cardPanel.getLayout();
        layout.next(cardPanel);
    }

    private void onOK() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                List<TableMeta> selectedTables = Lists.newArrayList();
                int[] selectedRows = databaseTables.getSelectedRows();
                for (int selectedRow : selectedRows) {
                    String tableName = databaseTables.getValueAt(selectedRow, 0).toString();
                    String remarks = ((String) databaseTables.getValueAt(selectedRow, 1));
                    Boolean cutPrefix = Boolean.valueOf(databaseTables.getValueAt(selectedRow, 2).toString());
                    TableMeta tableMeta = new TableMeta();
                    tableMeta.setTableName(tableName);
                    tableMeta.setRemarks(remarks);
                    tableMeta.setCutPrefix(cutPrefix);
                    selectedTables.add(tableMeta);
                }
                progressBar1.setMinimum(0);
                progressBar1.setMaximum(selectedTables.size());
                progressBar1.setString("正在准备生成代码...");
                console.append("正在准备生成代码...\n");
                new Generater().generateAll(DataConversion.MataToVarForTable(selectedTables), progressBar1, console);
            }
        });
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
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
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.setPreferredSize(new Dimension(1370, 500));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, BorderLayout.CENTER);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        console = new JTextArea();
        console.setEditable(false);
        console.setLineWrap(true);
        console.setWrapStyleWord(false);
        scrollPane1.setViewportView(console);
        cardPanel = new JPanel();
        cardPanel.setLayout(new CardLayout(0, 0));
        contentPane.add(cardPanel, BorderLayout.WEST);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel2.setEnabled(true);
        cardPanel.add(panel2, "Card1");
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(10, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setMaximumSize(new Dimension(415, 324));
        panel2.add(panel3, BorderLayout.CENTER);
        final JLabel label1 = new JLabel();
        label1.setText("数据库类型：");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dbType = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Mysql");
        dbType.setModel(defaultComboBoxModel1);
        panel3.add(dbType, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("driver-class：");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        driverClass = new JTextField();
        driverClass.setEditable(false);
        driverClass.setText("");
        panel3.add(driverClass, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("连接Url：");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        url = new JTextField();
        url.setHorizontalAlignment(2);
        panel3.add(url, new GridConstraints(2, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("用户名：");
        panel3.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        username = new JTextField();
        panel3.add(username, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("密码：");
        panel3.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        password = new JTextField();
        panel3.add(password, new GridConstraints(4, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("基础包路径：");
        panel3.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        baseClassPath = new JTextField();
        baseClassPath.setText("com.cloud.modules");
        panel3.add(baseClassPath, new GridConstraints(5, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("选择模板：");
        panel3.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        template = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("new_u_doctor");
        defaultComboBoxModel2.addElement("demo");
        defaultComboBoxModel2.addElement("hospital");
        defaultComboBoxModel2.addElement("u_doctor");
        template.setModel(defaultComboBoxModel2);
        panel3.add(template, new GridConstraints(6, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("包路径转换模式：");
        panel3.add(label8, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        defautRadioButton = new JRadioButton();
        defautRadioButton.setText("defaut");
        panel3.add(defautRadioButton, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        underlineRadioButton = new JRadioButton();
        underlineRadioButton.setText("使用下划线");
        panel3.add(underlineRadioButton, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pointsRadioButton = new JRadioButton();
        pointsRadioButton.setText("使用圆点");
        panel3.add(pointsRadioButton, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        humpRadioButton = new JRadioButton();
        humpRadioButton.setText("驼峰");
        panel3.add(humpRadioButton, new GridConstraints(7, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(32, 28), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("代码存放目录：");
        panel3.add(label9, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        codeDirectory = new JTextField();
        panel3.add(codeDirectory, new GridConstraints(8, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(9, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel4, BorderLayout.SOUTH);
        nextButton = new JButton();
        nextButton.setText("下一步");
        panel4.add(nextButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new BorderLayout(0, 0));
        cardPanel.add(panel5, "Card2");
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, BorderLayout.SOUTH);
        buttonCancel = new JButton();
        buttonCancel.setText("离开");
        panel6.add(buttonCancel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("生成");
        panel6.add(buttonOK, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel6.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        progressBar1 = new JProgressBar();
        progressBar1.setForeground(new Color(-1900554));
        progressBar1.setIndeterminate(false);
        progressBar1.setString("");
        progressBar1.setStringPainted(true);
        panel6.add(progressBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel5.add(scrollPane2, BorderLayout.CENTER);
        databaseTables = new JTable();
        scrollPane2.setViewportView(databaseTables);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
