package com.github.walterfan.guestbook.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static java.lang.System.out;

/**
 * Created by walter on 16/11/2016.
 */
public class DbConn {

    /**
     * logger of the class
     */
    private static Logger logger =  LoggerFactory.getLogger(DbConn.class);

    private Properties cfgProp = new Properties();

    private DbConfig dbCfg;
    /**
     * conneciton provider
     */
    private DbProvider provider = null;

    /**
     * db connection it hold
     */
    private Connection conn = null;

    /**
     * debug status
     */
    private boolean debug = false;
    private String jdbcConfigFile;


    public DbConn(String jdbcConfigFile) throws IOException {
        this.provider = new DbProvider(readDbConfig(jdbcConfigFile));
    }

    public DbConn(Connection conn) {
        this.conn = conn;
    }
    /**
     * @param provider DbProvider
     */
    public DbConn(DbProvider provider) {
        this.provider = provider;
    }


    public DbConfig readDbConfig(String jdbcConfigFile) throws IOException {

        InputStream in = ClassLoader.getSystemResourceAsStream(jdbcConfigFile);
        try {
            cfgProp.load(in);

            dbCfg = new DbConfig(cfgProp.getProperty(DbConfig.DRIVER_CLASS),
                    cfgProp.getProperty(DbConfig.URL),
                    cfgProp.getProperty(DbConfig.USERNAME),
                    cfgProp.getProperty(DbConfig.PASSWORD));

            return dbCfg;
        } finally {
            closeQuietly(in);
        }

    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

/*    public void setConnection(Connection conn) {
        this.conn = conn;
    }*/

    public Connection createConnection() throws SQLException {
        if (provider == null) {
            throw new RuntimeException("Please set connection provider firstly");
        }

        if (this.conn != null && !this.conn.isClosed()) {
            conn.close();
        }
        this.conn = provider.getConnection();
        if (this.conn != null) {
            this.conn.setAutoCommit(false);
        }

        return this.conn;
    }


    public void closeQuietly(AutoCloseable handler) {
        if(null != handler) {
            try {
                handler.close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    public void closeConnection() {
        closeQuietly(this.conn);
        this.conn = null;
    }

    public boolean isClosed() {
        if (this.conn == null ) {
            return true;
        }

        try {
            return this.conn.isClosed();
        } catch (SQLException e) {
            logger.error("isClosed error" , e);
            return true;
        }
    }

    public Connection getConnection() {
        return this.conn;
    }


    /**
     * @throws SQLException sql exception
     */
    public void commit() throws SQLException {
        if (conn != null) {
            conn.commit();
        }
    }

    /**
     * @throws SQLException sql exception
     */
    public void rollback() throws SQLException {
        if (conn != null) {
            conn.rollback();
        }
    }



    /**
     * @param sql
     *            input sql
     * @throws Exception
     *             if db exception
     */
    public int execute(String sql) throws Exception {
        int numUpdates = 0;
        if (conn == null) {
            return 0;
        }
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            //setCurStmt(stmt,sql);
            boolean status = stmt.execute(sql);
            do {
                if (status) { // it was a query and returns a ResultSet
                    rs = stmt.getResultSet(); // Get results

                    if (this.debug) {
                        return DbHelper.printResultsTable(rs, out); // Display them
                    }

                } else {
                    numUpdates = stmt.getUpdateCount();
                    if (this.debug) {
                        logger.info("Updated, " + numUpdates + " rows affected.");
                    }
                    if(numUpdates > 0) {
                        //conn.commit();
                    }
                }
                status = stmt.getMoreResults();
            } while (status || stmt.getUpdateCount() != -1);
        } finally { // print out any warnings that occurred
            closeQuietly(rs);
            closeQuietly(stmt);
        }
        return numUpdates;
    }

    public void execute() {

        try {
            if (this.createConnection() == null) {
                logger.info("getConnection error, please check the specified jdbc parameters");
                return;
            }

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            out.print("SQL> "); // prompt the user
            out.flush(); // make the prompt appear now.
            String strsql = in.readLine(); // get a line of input from user
            if ((strsql == null) || strsql.equals("")) {
                strsql = "SELECT to_char(sysdate,'mm/dd/yy hh24:mi:ss') as now from dual";
            }
            this.execute(strsql);
            this.commit();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(cfgProp.get(DbConfig.DRIVER_CLASS) + "," + cfgProp.get(DbConfig.URL)
                    + "," + cfgProp.get(DbConfig.USERNAME) + "," + cfgProp.get(DbConfig.PASSWORD));
        } finally {
            this.closeConnection();
        }
    }

    /**
     * @param args
     *            none
     */
    public static void main(String[] args) throws IOException {

        DbConn dbConn = new DbConn("jdbc.properties");
        dbConn.setDebug(true);
        dbConn.execute();
    }
}

