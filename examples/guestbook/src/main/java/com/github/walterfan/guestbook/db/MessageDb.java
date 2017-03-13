package com.github.walterfan.guestbook.db;

import com.github.walterfan.guestbook.domain.Message;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import static java.lang.System.out;

/**
 * Created by walter on 07/11/2016.
 */
public class MessageDb {

    private final DbConn dbConn;

    private static String CHECK_SQL = "SELECT * FROM sqlite_master WHERE type='table' and name='%s'";

    public MessageDb() throws Exception {
        dbConn = new DbConn("jdbc.properties");
        dbConn.setDebug(true);
        dbConn.createConnection();
    }

    public void init() throws Exception {
        int ret = initTable();
        if(ret > 0) {
            initData();

        }
    }

    public int initTable() throws Exception {
        int ret = check(Message.class);
        if(ret > 0) {
            out.println("found table and drop it firstly ");
            dropTable(Message.class);
        }

        createTable(Message.class);
        return check(Message.class);



    }

    private int initData() throws Exception {
        String id = UUID.randomUUID().toString();
        Message msg = new Message();
        msg.setId(id);
        msg.setTitle("hello guest");
        msg.setContent("this is a test message");
        msg.setTags("test tag");
        msg.setCreateTime(new Date());
        String sql = DbHelper.makeInsertSql(msg);
        out.println("execute " + sql);
        dbConn.execute(sql);

        sql = DbHelper.makeQuerySql(msg.getClass(), String.format("id = '%s'", id));
        out.println("execute " + sql);
        return dbConn.execute(sql);
    }

    public int createTable(Class<?> clazz) throws Exception {

        String sql = DbHelper.makeCreateTableSql(clazz);
        out.println("execute " + sql);
        return dbConn.execute(sql);
    }

    public int dropTable(Class<?> clazz) throws Exception {

        String sql = DbHelper.makeDropTableSql(clazz);
        out.println("execute " + sql);
        return dbConn.execute(sql);
    }

    public void clean() throws SQLException {
        dbConn.commit();
        dbConn.closeConnection();
    }

    public int check(Class<?> clazz) throws Exception {
        String sql = String.format(CHECK_SQL, clazz.getSimpleName().toLowerCase());
        out.println("execute " + sql);
        return dbConn.execute(sql);

    }

    public static void main(String[] argv) throws Exception {
        MessageDb db = new MessageDb();
        db.init();
        db.clean();
    }

 }
