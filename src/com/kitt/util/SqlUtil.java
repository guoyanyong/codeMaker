package com.kitt.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/6/13 12:24
 */
public class SqlUtil {

    private Logger logger = LoggerFactory.getLogger(SqlUtil.class);

    private static Cache<Connect, Connection> connectCache = CacheBuilder.newBuilder().maximumSize(1).build();

    private Connection connection;

    private Connect currentConnect;

    public enum Connect{

        Rap2("com.mysql.jdbc.Driver","root", "RYHbyt7V*n", "jdbc:mysql://10.10.1.35:3306/RAP2_DELOS_APP?autoReconnect=true&autoReconnectForPools=true&useUnicode=true&characterEncoding=UTF-8");

        private String driverClass;
        private String user;
        private String password;
        private String url;

        Connect(String driverClass, String user, String password, String url){
            this.driverClass = driverClass;
            this.user = user;
            this.password = password;
            this.url = url;
        }

    }

    public static SqlUtil getConnect(Connect connect){
        SqlUtil sqlUtil = new SqlUtil();
        Connection connection = connectCache.getIfPresent(connect);
        boolean present = Optional.ofNullable(connection).isPresent();

        if (present) {
            sqlUtil.connection = connection;
        }else {
            sqlUtil = createConnect(connect.driverClass, connect.user, connect.password, connect.url);
            connectCache.put(connect, sqlUtil.connection);
        }
        sqlUtil.currentConnect = connect;
        return sqlUtil;
    }


    public static SqlUtil createConnect(String driverClass, String user, String password, String url){
        SqlUtil sqlUtil = new SqlUtil();
        Connection connection = null;
        try {
            /** 使用Class.forName()方法自动创建这个驱动程序的实例且自动调用DriverManager来注册它 */
            Class.forName(driverClass);
            /** 通过DriverManager的getConnection()方法获取数据库连接 */
            Properties props =new Properties();
            props.setProperty("user", user);
            props.setProperty("password", password);
            props.setProperty("remarks", "true"); //设置可以获取remarks信息
            props.setProperty("useInformationSchema", "true");//设置可以获取tables remarks信息
            connection = DriverManager.getConnection(url, props);
            sqlUtil.connection = connection;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sqlUtil;
    }

    public Map<String, Object> queryOneBySql(String sql){
        HashMap<String, Object> result = Maps.newHashMap();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()){
                ResultSetMetaData metaData = resultSet.getMetaData();
                for (int i = 1; i < metaData.getColumnCount()+1; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    result.put(columnName, value);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if (statement!=null && !statement.isClosed()){
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public List<Map<String,Object>> queryListBySql(String sql){
        ArrayList<Map<String,Object>> arrayList = Lists.newArrayList();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            HashMap<String, Object> map = null;
            while (resultSet.next()){
                map = Maps.newHashMap();
                ResultSetMetaData metaData = resultSet.getMetaData();
                for (int i = 1; i < metaData.getColumnCount()+1; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    map.put(columnName, value);
                }
                arrayList.add(map);
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("08003")){
                if (this.currentConnect!=null){
                    return getConnect(this.currentConnect).queryListBySql(sql);
                }
            }
            e.printStackTrace();
        }finally {
            try {
                if (statement!=null && !statement.isClosed()){
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    public int executeUpdate(String updateSql){
        Statement statement = null;
        try {
            statement = connection.createStatement();
            int i = statement.executeUpdate(updateSql);
            return i;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (statement!=null && !statement.isClosed()){
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}
