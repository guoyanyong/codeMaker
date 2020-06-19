package com.cloud.util;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/14 18:44
 */
public class CheckBoxRenderer implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) return null;
        return (Component) value;
    }
}
