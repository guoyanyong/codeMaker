package com.cloud.util;

import com.cloud.coder.db.ColumnMeta;
import com.cloud.coder.db.ColumnVar;
import com.cloud.coder.db.TableMeta;
import com.cloud.coder.db.TableVar;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DataConversion {
	
	private static ResourceBundle bundle = ResourceBundle.getBundle("config/datatype");

	/**
	 * 将数据库表信息转换成模板需要的数据
	 * @param tableMetas
	 * @return
	 */
	public static List<TableVar> MataToVarForTable(List<TableMeta> tableMetas){
		return DataConversion.MataToVarForTable(tableMetas, "");
	}
	
	/**
	 * 将数据库表信息转换成模板需要的数据
	 * @param tableMetas
	 * @param prifix	数据表的前缀
	 * @return
	 */
	public static List<TableVar> MataToVarForTable(List<TableMeta> tableMetas, String prifix){
		return DataConversion.MataToVarForTable(tableMetas, new String[]{prifix});
	}
	
	
	/**
	 * 将数据库表信息转换成模板需要的数据
	 * @param tableMetas
	 * @param prifixs	数据表的前缀(可以设置多个前缀)
	 * @return
	 */
	public static List<TableVar> MataToVarForTable(List<TableMeta> tableMetas, String[] prifixs){
		
		List<TableVar> variables = new ArrayList<TableVar>();
		
		TableVar tv = null;
		for(TableMeta tm : tableMetas){
			String tableName = tm.getTableName();

			if (tm.isCutPrefix()) {
				tableName = tableName.substring(tableName.indexOf("_")+1);
			}
//
//			for(String prifix : prifixs){
//				if(prifix!=null && prifix.startsWith(prifix)) tableName = tableName.replaceFirst(prifix, "");
//			}
			
			tv = new TableVar();
			
			tv.setObjectName(StringUtil.convertUnderLine(tableName));
			tv.setTableName(tm.getTableName());
			tv.setRemarks(tm.getRemarks());
			variables.add(tv);
		}
		
		return variables;
	}
	
	/**
	 * 将数据库表中的字段信息转换为模板需要的数据
	 * @param columnMetas
	 * @return
	 */
	public static List<ColumnVar> MataToVarForColumn(List<ColumnMeta> columnMetas){
	
		List<ColumnVar> variables = new ArrayList<ColumnVar>();
		
		ColumnVar cv = null;
		String primaryKey = null;
		try {
			primaryKey = DBTool.getPrimaryKeys(columnMetas.get(0).getTableName());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for(ColumnMeta cm : columnMetas){
			cv = new ColumnVar();
			cv.setProperty(StringUtil.convertUnderLine(cm.getColumnName()));
			cv.setColumn(cm.getColumnName());
			cv.setDesc(cm.getRemarks());
			cv.setIsAutoincrement(cm.getIsAutoincrement());
			cv.setIsNullable(cm.getIsNullable());
			cv.setOrdinalPosition(cm.getOrdinalPosition());
			String typeName = bundle.getString("mysql."+cm.getTypeName().replace(" ", "."));
			cv.setTypeName(typeName);
			cv.setPrimaryKey(cm.getColumnName().equals(primaryKey)?true:false);
			variables.add(cv);
		}
		
		return variables;
	}
}
