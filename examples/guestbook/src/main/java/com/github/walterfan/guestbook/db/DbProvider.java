package com.github.walterfan.guestbook.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by walter on 16/11/2016.
 */
public class DbProvider  {

    private static boolean initiated = false;

    private DbConfig dbConfig;

    public DbProvider(DbConfig dbCfg) {
        this.dbConfig = dbCfg;
    }


    public Connection getConnection() throws SQLException {
        try {
            initiate();
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
        Connection conn = DriverManager.getConnection(dbConfig.getUrl(),
                dbConfig.getUserName(), dbConfig.getPassword());
        if(conn!=null) {
            conn.setAutoCommit(false);
            /*conn = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                    new Class[]{Connection.class},
                    new ConnectionProxy(conn));*/
        }
        return conn;
    }


 /*   public int getConnectionCount() {
        return ConnectionProxy.getConnNum();
    }*/


    private synchronized void initiate() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if(!initiated) {
            Class.forName(dbConfig.getDriverClass()).newInstance();
        }
    }


    public String toString() {
        return this.dbConfig.toString();
    }

}
