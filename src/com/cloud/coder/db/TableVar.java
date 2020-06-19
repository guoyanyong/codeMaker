package com.cloud.coder.db;

/**
 * 数据库表信息
 * 
 * @author ethan
 *
 */
public class TableVar {

	private String objectName;
	private String tableName;
	private String remarks;

	public String getObjectName() {
		return objectName;
	}
	
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
