package com.cloud.util;

import javax.swing.table.DefaultTableModel;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/21 18:22
 */
public class MyAbstractTableModel extends DefaultTableModel {
    public MyAbstractTableModel(String[] head){
        super(null, head);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return this.getValueAt(0,columnIndex).getClass();
    }
}
