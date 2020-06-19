package com.cloud.util;

import com.cloud.coder.db.ColumnMeta;
import com.cloud.coder.db.TableMeta;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.*;

public class DBTool {

	private static final Log log = LogFactory.getLog(DBTool.class);

	private static Connection conn = null;

	public DBTool(String dbType, String driverClass, String url,
			String user, String password) {
		try {
			/** 使用Class.forName()方法自动创建这个驱动程序的实例且自动调用DriverManager来注册它 */
			Class.forName(driverClass);
			/** 通过DriverManager的getConnection()方法获取数据库连接 */
			Properties props =new Properties();
			props.setProperty("user", user);
			props.setProperty("password", password);
			props.setProperty("remarks", "true"); //设置可以获取remarks信息
			props.setProperty("useInformationSchema", "true");//设置可以获取tables remarks信息
			conn = DriverManager.getConnection(url, props);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 获取所有表表名
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static List<TableMeta> getAllTableName() throws SQLException {
		List<TableMeta> tables = new ArrayList<TableMeta>();
		DatabaseMetaData dbMetaData = conn.getMetaData();
		// 可为:"TABLE", "VIEW", "SYSTEM   TABLE", "GLOBAL   TEMPORARY", "LOCAL   TEMPORARY", "ALIAS", "SYNONYM"
		String[] types = { "TABLE" };
		ResultSet tabs = dbMetaData.getTables(null, null, null, types);
		while (tabs.next()) {
			// 只要表名这一列
			// TABLE_CAT TABLE_SCHEM TABLE_NAME TABLE_TYPE EMARKS
			String TABLE_CAT = tabs.getString("TABLE_CAT");
			String TABLE_SCHEM = tabs.getString("TABLE_SCHEM");
			String TABLE_NAME = tabs.getString("TABLE_NAME");
			String TABLE_TYPE = tabs.getString("TABLE_TYPE");
			String REMARKS = tabs.getString("REMARKS");
			
			TableMeta tableMeta = new TableMeta();
			tableMeta.setTableCat(TABLE_CAT);
			tableMeta.setTableSchem(TABLE_SCHEM);
			tableMeta.setTableName(TABLE_NAME);
			tableMeta.setTableType(TABLE_TYPE);
			tableMeta.setRemarks(REMARKS);
//			int i = 0;
//			System.out.println(++i + ".   TABLE_CAT			:  "+TABLE_CAT);
//			System.out.println(++i + ".   TABLE_SCHEM		:  "+TABLE_SCHEM);
//			System.out.println(++i + ".   TABLE_NAME		:  "+TABLE_NAME);
//			System.out.println(++i + ".   TABLE_TYPE		:  "+TABLE_TYPE);
//			System.out.println(++i + ".   REMARKS		:  "+REMARKS);
//			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			tables.add(tableMeta);
		}
		return tables;
	}

	/**
	 * 获取某表下所有字段
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static List<ColumnMeta> getAllColumnInfo(String tableName) throws SQLException {
		List<ColumnMeta> columns = new ArrayList<ColumnMeta>();
		DatabaseMetaData databaseMetaData = conn.getMetaData();

		ResultSet columnSet = databaseMetaData.getColumns(null, "%", tableName, "%");
		if (columnSet != null) {
			ColumnMeta cm = null;
			while (columnSet.next()) {
				String TABLE_CAT = columnSet.getString("TABLE_CAT");
				String TABLE_SCHEM = columnSet.getString("TABLE_SCHEM");
				String SOURCE_DATA_TYPE = columnSet.getString("SOURCE_DATA_TYPE");
				String TABLE_NAME = columnSet.getString("TABLE_NAME");
				String COLUMN_NAME = columnSet.getString("COLUMN_NAME");
				String DATA_TYPE = columnSet.getString("DATA_TYPE");
				String TYPE_NAME = columnSet.getString("TYPE_NAME");
				String COLUMN_SIZE = columnSet.getString("COLUMN_SIZE");
				String BUFFER_LENGTH = columnSet.getString("BUFFER_LENGTH");
				String DECIMAL_DIGITS = columnSet.getString("DECIMAL_DIGITS");
				String NUM_PREC_RADIX = columnSet.getString("NUM_PREC_RADIX");
				String NULLABLE = columnSet.getString("NULLABLE");
				String REMARKS = columnSet.getString("REMARKS");
				String COLUMN_DEF = columnSet.getString("COLUMN_DEF");
				String SQL_DATA_TYPE = columnSet.getString("SQL_DATA_TYPE");
				String SQL_DATETIME_SUB = columnSet.getString("SQL_DATETIME_SUB");
				String CHAR_OCTET_LENGTH = columnSet.getString("CHAR_OCTET_LENGTH");
				String ORDINAL_POSITION = columnSet.getString("ORDINAL_POSITION");
				String IS_NULLABLE = columnSet.getString("IS_NULLABLE");
				String SCOPE_CATALOG = columnSet.getString("SCOPE_CATALOG");
				String SCOPE_SCHEMA = columnSet.getString("SCOPE_SCHEMA");
				String SCOPE_TABLE = columnSet.getString("SCOPE_TABLE");
				String IS_AUTOINCREMENT = columnSet.getString("IS_AUTOINCREMENT");
				
//				int i = 0;
//				System.out.println(++i + ".   TABLE_CAT			:  "+TABLE_CAT);
//				System.out.println(++i + ".   TABLE_SCHEM		:  "+TABLE_SCHEM);
//				System.out.println(++i + ".   SOURCE_DATA_TYPE		:  "+SOURCE_DATA_TYPE);
//				System.out.println(++i + ".   TABLE_NAME			:  "+TABLE_NAME);
//				System.out.println(++i + ".   COLUMN_NAME:		:  "+COLUMN_NAME);
//				System.out.println(++i + ".   DATA_TYPE			:  "+DATA_TYPE);
//				System.out.println(++i + ".   TYPE_NAME			:  "+TYPE_NAME);
//				System.out.println(++i + ".   COLUMN_SIZE		:  "+COLUMN_SIZE);
//				System.out.println(++i + ".   BUFFER_LENGTH		:  "+BUFFER_LENGTH);
//				System.out.println(++i + ".   DECIMAL_DIGITS		:  "+DECIMAL_DIGITS);
//				System.out.println(++i + ".   NUM_PREC_RADIX		:  "+NUM_PREC_RADIX);
//				System.out.println(++i + ".   NULLABLE			:  "+NULLABLE);
//				System.out.println(++i + ".   REMARKS			:  "+REMARKS);
//				System.out.println(++i + ".   COLUMN_DEF		:  "+COLUMN_DEF);
//				System.out.println(++i + ".   SQL_DATA_TYPE		:  "+SQL_DATA_TYPE);
//				System.out.println(++i + ".   SQL_DATETIME_SUB		:  "+SQL_DATETIME_SUB);
//				System.out.println(++i + ".   CHAR_OCTET_LENGTH		:  "+CHAR_OCTET_LENGTH);
//				System.out.println(++i + ".   ORDINAL_POSITION		:  "+ORDINAL_POSITION);
//				System.out.println(++i + ".   IS_NULLABLE		:  "+IS_NULLABLE);
//				System.out.println(++i + ".   SCOPE_CATALOG		:  "+SCOPE_CATALOG);
//				System.out.println(++i + ".   SCOPE_SCHEMA		:  "+SCOPE_SCHEMA);
//				System.out.println(++i + ".   SCOPE_TABLE		:  "+SCOPE_TABLE);
//				System.out.println(++i + ".   IS_AUTOINCREMENT		:  "+IS_AUTOINCREMENT);
//				
//				System.out.println("===============================================================================================");
				

				cm = new ColumnMeta();
				cm.setBufferLength(BUFFER_LENGTH);
				cm.setCharOctetLength(CHAR_OCTET_LENGTH);
				cm.setColumnDef(COLUMN_DEF);
				cm.setColumnName(COLUMN_NAME);
				cm.setColumnSize(COLUMN_SIZE);
				cm.setDataType(DATA_TYPE);
				cm.setDecimalDigits(DECIMAL_DIGITS);
				cm.setIsAutoincrement(IS_AUTOINCREMENT);
				cm.setIsNullable(IS_NULLABLE);
				cm.setNullable(NULLABLE);
				cm.setNumPrecRadix(NUM_PREC_RADIX);
				cm.setOrdinalPosition(ORDINAL_POSITION);
				cm.setRemarks(REMARKS);
				cm.setScopeCatalog(SCOPE_CATALOG);
				cm.setScopeSchema(SCOPE_SCHEMA);
				cm.setScopeTable(SCOPE_TABLE);
				cm.setSourceDataType(SOURCE_DATA_TYPE);
				cm.setSqlDataType(SQL_DATA_TYPE);
				cm.setSqlDatetimeSub(SQL_DATETIME_SUB);
				cm.setTableCat(TABLE_CAT);
				cm.setTableName(TABLE_NAME);
				cm.setTableSchem(TABLE_SCHEM);
				cm.setTypeName(TYPE_NAME);
				
				columns.add(cm);
			}
		}
		return columns;
	}

	/**
	 * 判断某表中的字段是否是主键
	 * @param tableName
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	public static boolean isPrimaryKey(String tableName, String columnName)
			throws SQLException {
		boolean flag = false;
		DatabaseMetaData dbMeta = conn.getMetaData();
		ResultSet primaryKey = dbMeta.getPrimaryKeys(null, null, tableName);
		while (primaryKey.next()) {
			//log.info("表名:" + primaryKey.getString(3));

			if (columnName.equals(primaryKey.getString(4))) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	/**
	 * 获取某表中的主键字段
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static String getPrimaryKeys(String tableName) throws SQLException {
		String primaryKeys = "";
		DatabaseMetaData dbMeta = conn.getMetaData();
		ResultSet primaryKey = dbMeta.getPrimaryKeys(null, null, tableName);
		while (primaryKey.next()) {
			//log.info("获取表 " + primaryKey.getString(3) + " 中的  " + primaryKey.getString(6) + " 类型主键 -> 列名：" + primaryKey.getString(4));
			if (primaryKeys != "") {
				primaryKeys += ",";
			}
			primaryKeys += primaryKey.getString(4);
		}
		return primaryKeys;
	}

	public static Connection getConn() {
		return conn;
	}
}
