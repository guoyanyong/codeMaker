package com.cloud.coder.db;

/**
 * 数据库表字段信息
 * 
 * @author ethan
 *
 */
public class ColumnVar {

	private boolean primaryKey;
	
	private String property;

	private String typeName;

	private String column;

	private String isNullable;

	private String isAutoincrement;

	private String ordinalPosition;

	private String desc;

	
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	public String getProperty() {
		return property;
	}

	/**
	 * 设置变量名称
	 * 
	 * @param property
	 */
	public void setProperty(String property) {
		this.property = property;
	}

	public String getTypeName() {
		return typeName;
	}

	/**
	 * 设置字段类型
	 * 
	 * @param typeName
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getColumn() {
		return column;
	}

	/**
	 * 设置数据库列名
	 * 
	 * @param column
	 */
	public void setColumn(String column) {
		this.column = column;
	}

	public String getDesc() {
		return desc.replace("\"","'");
	}

	/**
	 * 设置字段描述信息
	 * 
	 * @param desc
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getIsAutoincrement() {
		return isAutoincrement;
	}

	public void setIsAutoincrement(String isAutoincrement) {
		this.isAutoincrement = isAutoincrement;
	}

	public String getIsNullable() {
		return isNullable;
	}

	public void setIsNullable(String isNullable) {
		this.isNullable = isNullable;
	}

	public String getOrdinalPosition() {
		return ordinalPosition;
	}

	public void setOrdinalPosition(String ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}
}
