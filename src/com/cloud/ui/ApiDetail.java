package com.cloud.ui;

import com.cloud.coder.javadoc.info.MethodApi;
import com.cloud.coder.javadoc.info.Param;
import com.cloud.coder.javadoc.info.Response;
import com.cloud.util.HttpsUtils;
import com.cloud.util.MD5Util;
import com.cloud.util.MyAbstractTableModel;
import com.cloud.util.StringUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.kitt.util.DocUtil;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/14 9:57
 */
public class ApiDetail {
    private JPanel panel1;
    private JTextField url;
    private JButton sendButton;
    private JTable paramsTable;
    private JLabel methodPanel;
    private JTextArea resultText;
    private JTable respTable;
    private JTextField authorization;
    private JTextField author;
    private JTextField createTime;
    private JComboBox varSelect;
    private JLabel version;
    private JTabbedPane resultTab;
    private JButton uploadResultBtn;
    private JButton refreshBtn;


    ApiDetail(String moduleName, MethodApi api, Map<String, String> varMap, DefaultMutableTreeNode treeNode, JTree jTree) {

        //设置环境变量
        for (String s : varMap.keySet()) {
            varSelect.addItem(s);
        }

        //渲染参数列表
        String[] titles = {"参数类型", "参数名称", "数据类型", "参数描述", "必需", "随机取值", ""};
        MyAbstractTableModel paramTableModel = new MyAbstractTableModel(titles);
//        DefaultTableModel paramTableModel = new DefaultTableModel(null, titles);
        JTableHeader head = paramsTable.getTableHeader(); // 创建表格标题对象
        head.setPreferredSize(new Dimension(head.getWidth(), 25));// 设置表头大小
        paramsTable.setModel(paramTableModel);
        paramsTable.setRowHeight(25);
        paramsTable.setTableHeader(head);
        paramsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        paramsTable.getColumnModel().getColumn(0).setMaxWidth(100);
        paramsTable.getColumnModel().getColumn(2).setMaxWidth(100);
        paramsTable.getColumnModel().getColumn(4).setMaxWidth(50);
        paramsTable.getColumnModel().getColumn(6).setMaxWidth(50);
        //填充数据
        renderFormData(api, paramTableModel);

        //渲染结果列表
        String[] responseTableTitles = {"参数全路径", "参数名称", "参数类型", "参数描述"};
        DefaultTableModel responseTableModel = new DefaultTableModel(null, responseTableTitles);
        JTableHeader responseTableHeader = respTable.getTableHeader();
        responseTableHeader.setPreferredSize(new Dimension(head.getWidth(), 25));
        respTable.setModel(responseTableModel);
        respTable.setTableHeader(responseTableHeader);
        respTable.setRowHeight(25);
        respTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        respTable.getColumnModel().getColumn(0).setMaxWidth(0);
        respTable.getColumnModel().getColumn(2).setMaxWidth(120);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional.ofNullable(paramsTable.getCellEditor()).ifPresent(o -> {
                    o.stopCellEditing();
                });
                resultTab.setSelectedIndex(0);
                resultText.setText("正在发送请求，请稍后...");
                responseTableModel.setRowCount(0);
                //通过表格选中请求参数
                Map<String, Object> params = Maps.newHashMap();
                Map<String, Object> headerParams = Maps.newHashMap();
                Map<String, Object> pathParams = Maps.newHashMap();
                for (int i = 0; i < paramsTable.getRowCount(); i++) {
                    Object selected = paramsTable.getValueAt(i, 6);
                    if (!Boolean.valueOf(selected.toString())) {
                        continue;
                    }

                    String paramType = paramsTable.getValueAt(i, 0).toString();
                    String name = paramsTable.getValueAt(i, 1).toString();
                    Object value = paramsTable.getValueAt(i, 5);

                    if (paramType.equals("Header")) {
                        headerParams.put(name, value);
                    } else if (paramType.equals("Form")) {
                        params.put(name, value);
                    } else if (paramType.equals("Path")) {
                        pathParams.put(name, value);
                    } else if (paramType.equals("Body")) {
                        //TODO body访问方法
                    }
                }

                //加入版本号
                Optional.ofNullable(version.getText()).ifPresent(v -> {
                    headerParams.put("version", v);
                });

                //替换Path参数
                String url = api.getUrl();
                for (Map.Entry<String, Object> entry : pathParams.entrySet()) {
                    url = url.replace("{" + entry.getKey() + "}", entry.getValue().toString());
                }
                String finalUrl = url;

                new Thread() {
                    @Override
                    public void run() {
                        Object selectedItem = varSelect.getSelectedItem();
                        Optional.ofNullable(selectedItem).ifPresent(s -> {
                            String host = varMap.get(s.toString());
                            if (!host.endsWith("/")) {
                                host += "/";
                            }
                            //apiInfo中获取接口信息
                            JSONObject result = new JSONObject();
                            if (api.getMethod().equalsIgnoreCase("get")) {
                                result = HttpsUtils.doGet(host + finalUrl, params, headerParams);
                            } else if (api.getMethod().equalsIgnoreCase("post")) {
                                result = HttpsUtils.doPost(host + finalUrl, params, headerParams);
                            }
                            Optional<JSONObject> rs = Optional.ofNullable(result);
                            if (rs.isPresent()) {
                                String rsStr = StringUtil.formatJson(result.toString());
                                resultText.setText(rsStr);

                                renderResponseTableModel(api.getSignature(), rs.get(), responseTableModel);

                                //启用上传按钮
                                uploadResultBtn.setEnabled(true);
                            } else {
                                resultText.setText("请求失败");
                            }
                        });
                    }
                }.start();
            }
        });
        resultText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 3) {
                    resultText.setText("");
                }
            }
        });
        uploadResultBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadResultBtn.setEnabled(false);
                Optional.ofNullable(respTable.getCellEditor()).ifPresent(o -> {
                    o.stopCellEditing();
                });
                List<Response> responseList = Lists.newArrayList();
                for (Vector o : (Vector<Vector>) responseTableModel.getDataVector()) {
                    Response response = new Response();
                    response.setFullPath(String.valueOf(o.get(0)));
                    response.setName(String.valueOf(o.get(1)));
                    response.setDataType(String.valueOf(o.get(2)));
                    response.setComment(String.valueOf(o.get(3)));
                    responseList.add(response);
                }

                MethodApi userObject = (MethodApi) treeNode.getUserObject();
                userObject.setResponseList(responseList);
                userObject.getClassApi().setMethodApis(null);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(api.getProject());
                        String projectTag = propertiesComponent.getValue(HttpConfigDialog.prefix + "projectTag");
                        HttpsUtils.doPost(HttpsUtils.serverUrl + "/apiInfo/upload/" + projectTag + "/" + moduleName, new Gson().toJson(userObject));
                        uploadResultBtn.setEnabled(true);
                    }
                }).start();
            }
        });
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VirtualFile virtualFile = api.getVirtualFile();
                PsiFile psiFile = PsiManager.getInstance(api.getProject()).findFile(virtualFile);
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;

                for (PsiClass aClass : psiJavaFile.getClasses()) {
                    for (PsiMethod method : aClass.getMethods()) {
                        String qualifiedName = method.getContainingClass().getQualifiedName();
                        String encrypt = MD5Util.encrypt(qualifiedName + "." + method.getHierarchicalMethodSignature());
                        if (api.getSignature().equals(encrypt)) {
                            MethodApi methodApi = new DocUtil().makeMehtodApi(aClass, api.getClassApi(), method);
                            methodApi.setIcon(api.getIcon());
                            treeNode.setUserObject(methodApi);
                            jTree.updateUI();
                            JTabbedPane parent = (JTabbedPane) panel1.getParent();
                            int selectedIndex = parent.getSelectedIndex();
                            String comment = methodApi.toString();
                            if (StringUtils.isEmpty(comment)) {
                                comment = methodApi.getUrl();
                            }
                            String title = comment.length() > 10 ? comment.substring(0, 8) + "..." : comment;
                            PanelTabWithClose tabWithClose = new PanelTabWithClose(title, parent, panel1);
                            parent.setTabComponentAt(selectedIndex, tabWithClose.getUi());
                            renderFormData(methodApi, paramTableModel);
                        }
                    }
                }
            }
        });
    }

    /**
     * 渲染数据
     *
     * @param api
     * @return javax.swing.table.DefaultTableModel
     * @author Xps13
     * @createTime 2019/6/30 12:21
     */
    @NotNull
    private void renderFormData(MethodApi api, MyAbstractTableModel paramTableModel) {

        methodPanel.setText(api.getMethod());
        url.setText(api.getUrl());
        version.setText(api.getVersion());
        authorization.setText(api.getAuthorization());
        author.setText(api.getAuthor());
        createTime.setText(api.getCreateTime());

        //清空表格
        paramTableModel.setRowCount(0);

        List<Param> parmaList = api.getParamList();
        parmaList.forEach(param -> {
            //生成mock默认值
            Object defaultValue = param.getDefaultValue();
            if (!Optional.ofNullable(defaultValue).isPresent()) {
                //"int", "float", "Integer", "java.lang.Integer[]", "String", "java.lang.String[]","Date", "Long", "Number", "Double", "Float", "Boolean", "List"
                String type = param.getDataType();
                switch (type) {
                    case "String":
                        defaultValue = "随机字符串";
                        break;
                    case "int":
                    case "Integer":
                    case "Long":
                    case "Number":
                        defaultValue = 1;
                        break;
                    case "Float":
                    case "Double":
                        defaultValue = 1.00;
                        break;
                    case "Date":
                    case "LocalDate":
                        defaultValue = LocalDate.now();
                        break;
                    case "LocalDateTime":
                        defaultValue = LocalDateTime.now();
                        break;
                    case "Boolean":
                        defaultValue = true;
                        break;
                }
            }
            String comment = param.getComment() == null ? "" : param.getComment();
            Object[] rowData = {
                    String.valueOf(param.getParamType()),
                    String.valueOf(param.getName()),
                    String.valueOf(param.getDataType()),
                    comment,
                    String.valueOf(param.getRequired() ? "√" : ""),
                    String.valueOf(defaultValue),
                    Boolean.valueOf(param.getRequired().equals(true))};
            paramTableModel.addRow(rowData);
        });
    }

    /**
     * 渲染response表格
     *
     * @param result
     * @param responseTableModel
     * @return void
     * @author Xps13
     * @createTime 2019/6/10 14:34
     */
    public void renderResponseTableModel(String apiSignature, JSONObject result, DefaultTableModel responseTableModel) {

        //首先获取线上已经存在结果描述
        try {
            JSONObject jsonObject = HttpsUtils.doGet(HttpsUtils.serverUrl + "/apiInfo/getResponseBySignature?signature=" + apiSignature);
            Map<String, String> commentMap = Maps.newHashMap();
            if (Optional.ofNullable(jsonObject).isPresent()) {
                int state = jsonObject.getInt("state");
                JSONArray data = new JSONArray();
                if (0 == state) {
                    data = jsonObject.getJSONArray("data");
                }

                List<LinkedTreeMap> list = new Gson().fromJson(data.toString(), List.class);
                for (LinkedTreeMap o : list) {
                    String fullPath = o.get("fullPath").toString();
                    String comment = "";
                    if (Optional.ofNullable(o.get("comment")).isPresent()) {
                        comment = o.get("comment").toString();
                    }
                    commentMap.put(fullPath, comment);
                }
            }

            TreeMap<String, Object> keyMap = Maps.newTreeMap();
            renderTable(result, keyMap, Maps.newConcurrentMap(), "", false);
            for (Map.Entry<String, Object> keyEntry : keyMap.entrySet()) {
                String key = keyEntry.getKey();
                Object value = keyEntry.getValue();
                String[] split = key.split("\\.");
                //计算缩进
                StringBuffer indentSb = new StringBuffer();
                for (int i = 0; i < split.length - 1; i++) {
                    indentSb.append("    ");
                }
                String name = indentSb.append(split[split.length - 1]).toString();

                String simpleName = (value == null || value.toString() == null) ? "" : value.getClass().getSimpleName();
                responseTableModel.addRow(new String[]{key, name, simpleName, commentMap.get(key)});
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分析返回结果中包含的字段树
     *
     * @param jsonObject 源数据
     * @param keyMap     所有的结果key （用全路径表示）
     * @param listMap    当处理数据为列表时，需要实例化该参数
     * @param parentKey  上级的 Key ， 用于计算全路径
     * @param isArray    明确当前处理的数据是否是处于数组中的数据
     * @return void
     * @author Xps13
     * @createTime 2019/6/6 19:08
     */
    private void renderTable(JSONObject jsonObject, TreeMap<String, Object> keyMap, @NotNull Map<String, Object> listMap, String parentKey, boolean isArray) {

        //开始遍历key值
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            //定义key的全路径
            String fullPath = Strings.isNullOrEmpty(parentKey) ? key : parentKey + "." + key;
            try {
                //获取当前对象的key所对应的对象值
                Object value = jsonObject.get(key);

                if (!isArray) {
                    boolean contains = keyMap.containsKey(fullPath);
                    if (!contains || value != null) {
                        keyMap.put(fullPath, value);
                    }
                } else {
                    //如果当遍历对象是list，则不进行输出，待列表内所有对象遍历完成后，统计覆盖全量的key
                    listMap.put(key, value);
                }

                if (value instanceof JSONObject) {
                    //如查对象属于简单对象， 继续遍历对象中的key
                    JSONObject json = ((JSONObject) value);
                    renderTable(json, keyMap, Maps.newConcurrentMap(), fullPath, false);
                } else if (value instanceof JSONArray) {
                    //如果对象属于数组对象，则行循环为简单对象后，再进行Key 的遍历
                    JSONArray jsonArray = ((JSONArray) value);

                    //利用map 的 key 不能重复的特性，统计列表中所有对象包含的全部key值
                    listMap = Maps.newConcurrentMap();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        renderTable(jsonObject1, keyMap, listMap, fullPath, true);
                    }
                    renderTable(new JSONObject(listMap), keyMap, Maps.newConcurrentMap(), fullPath, false);
                } else {
                    //TODO 分析该属性的 数据类型


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public JPanel getPanel1() {
        return panel1;
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
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.setDoubleBuffered(true);
        panel1.setEnabled(true);
        panel1.setFocusable(true);
        panel1.setOpaque(true);
        panel1.setRequestFocusEnabled(true);
        panel1.setVerifyInputWhenFocusTarget(true);
        panel1.setVisible(true);
        panel1.putClientProperty("html.disable", Boolean.FALSE);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 6, new Insets(0, 5, 0, 0), -1, -1));
        panel2.add(panel3, BorderLayout.NORTH);
        methodPanel = new JLabel();
        methodPanel.setText("POST");
        panel3.add(methodPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        url = new JTextField();
        panel3.add(url, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sendButton = new JButton();
        sendButton.setText("发送");
        panel3.add(sendButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("接口权限");
        panel4.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        authorization = new JTextField();
        authorization.setEditable(false);
        panel4.add(authorization, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("创建时间");
        panel4.add(label2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        author = new JTextField();
        author.setEditable(false);
        panel4.add(author, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        createTime = new JTextField();
        createTime.setEditable(false);
        panel4.add(createTime, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        varSelect = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        varSelect.setModel(defaultComboBoxModel1);
        panel3.add(varSelect, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        version = new JLabel();
        version.setText("版本号");
        panel3.add(version, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("作者");
        panel3.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uploadResultBtn = new JButton();
        uploadResultBtn.setEnabled(false);
        uploadResultBtn.setText("保存接口信息");
        panel3.add(uploadResultBtn, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        refreshBtn = new JButton();
        refreshBtn.setText("刷新接口信息");
        panel3.add(refreshBtn, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel5, BorderLayout.CENTER);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel5.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        paramsTable = new JTable();
        scrollPane1.setViewportView(paramsTable);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(0, 0));
        panel6.setPreferredSize(new Dimension(400, 204));
        panel1.add(panel6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        resultTab = new JTabbedPane();
        panel6.add(resultTab, BorderLayout.CENTER);
        final JScrollPane scrollPane2 = new JScrollPane();
        resultTab.addTab("response", new ImageIcon(getClass().getResource("/icons/icons8-text-color-16.png")), scrollPane2);
        resultText = new JTextArea();
        resultText.setEditable(false);
        resultText.setLineWrap(true);
        resultText.setText("");
        scrollPane2.setViewportView(resultText);
        final JScrollPane scrollPane3 = new JScrollPane();
        resultTab.addTab("respnseTable", new ImageIcon(getClass().getResource("/icons/icons8-data-sheet-16.png")), scrollPane3);
        respTable = new JTable();
        scrollPane3.setViewportView(respTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
