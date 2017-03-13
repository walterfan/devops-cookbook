package com.github.walterfan.guestbook.db;

import com.github.walterfan.guestbook.domain.BaseObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * db help class
 *
 * @author Walter Fan
 * @version 1.0 10/28/2008
 */
public final class DbHelper {

    public static final int DEFAULT_COLUMN_WIDTH = 36;
    private static String dropTableStatement = "DROP TABLE %s";
    private static String createTableStatement = "CREATE TABLE %s (%s)";

    private static Map<String, String> fieldTypeMap = new HashMap<String, String>() {{
        put("int", "INT");
        put("long", "INT");
        put("double", "DOUBLE");
        put("float", "FLOAT");
        put("boolean", "BOOLEAN");

        put("java.lang.Integer", "INT");
        put("java.lang.Long", "INT");
        put("java.lang.Double", "DOUBLE");
        put("java.lang.Float", "FLOAT");
        put("java.lang.Boolean", "BOOLEAN");

        put("java.lang.String", "TEXT");
        put("java.util.Date", "DATETIME");
    }};


    private DbHelper() {

    }


    public static String makeCreateTableSql(Class<?> clazz) {

        Field[] fields = clazz.getDeclaredFields();
        //System.out.println("fields: " + fields);

        List<String> declarations = new ArrayList<String>();
        for (Field f : fields) {
            String fieldType = fieldTypeMap.get(f.getType().getName());
            if (fieldType == null) declarations.add(f.getName() + " TEXT");
            else declarations.add(f.getName() + " " + fieldType);
        }
        String fieldDeclaration = list2String(declarations);

        return String.format(
                createTableStatement,
                clazz.getSimpleName().toLowerCase(),
                fieldDeclaration
        );

    }

    public static String makeDropTableSql(Class<?> clazz) {
        return String.format(dropTableStatement, clazz.getSimpleName().toLowerCase());
    }

    public static String makeInsertSql(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<String>();
        List<String> placeholders = new ArrayList<String>();

        for (Field f : fields) {
            fieldNames.add(f.getName());
            placeholders.add("?");
        }

        String insertStatement = String.format(
                "insert into %s(%s) values(%s)",
                clazz.getSimpleName().toLowerCase(),
                list2String(fieldNames),
                list2String(placeholders)
        );
        return insertStatement;
    }


    public static String makeInsertSql(BaseObject object) throws Exception {
        Class clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<String>();
        List<String> placeholders = new ArrayList<String>();

        for (Field f : fields) {
            fieldNames.add(f.getName());
            Object fieldValue = object.getField(f.getName());
            if(fieldValue == null) {
                placeholders.add("null");
            } else if(fieldValue instanceof String) {
                placeholders.add("'" + fieldValue + "'");
            } else if(fieldValue instanceof Date) {
                placeholders.add("'" + DateFormatUtils.format((Date)fieldValue, "yyyy-MM-dd HH:mm:ss.SSS") + "'");
            } else {
                placeholders.add(fieldValue.toString());
            }
        }

        String insertStatement = String.format(
                "insert into %s(%s) values(%s)",
                clazz.getSimpleName().toLowerCase(),
                list2String(fieldNames),
                list2String(placeholders)
        );
        return insertStatement;
    }

    public static String makeUpdateSql(Class<?> clazz, String condition) {
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<String>();

        List<String> placeholders = new ArrayList<String>();

        for (Field f : fields) {
            fieldNames.add(f.getName());
            placeholders.add(String.format("%s = ?", f.getName()));
        }

        String updateSql = String.format(
                "update %s set %s",
                clazz.getSimpleName().toLowerCase(),
                list2String(placeholders));

        if (StringUtils.isNotBlank(condition)) {
            updateSql += " where " + condition;
        }

        return updateSql;


    }

    public static String makeQuerySql(Class<?> clazz, String condition) {
        String querySql = String.format(
                "select * from %s",
                clazz.getSimpleName().toLowerCase()
        );
        if (StringUtils.isNotBlank(condition)) {
            querySql += " where " + condition;
        }
        return querySql;
    }

    public static String makeDeleteSql(Class<?> clazz, String condition) {

        String querySql = String.format(
                "delete * from %s",
                clazz.getSimpleName().toLowerCase()
        );
        if (StringUtils.isNotBlank(condition)) {
            querySql += " where " + condition;
        }
        return querySql;
    }

    /**
     * @param <T>       any collection
     * @param list      such as id list {11,222,333}
     * @param separator such as ,
     * @return 11, 222, 3s33
     */
    public static <T> String list2String(final Collection<T> list, final String separator) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer("");
        Iterator<T> iter = list.iterator();
        sb.append(iter.next());
        for (; iter.hasNext(); ) {
            T t = iter.next();
            sb.append(separator);
            sb.append(t);
        }
        return sb.toString();
    }

    /**
     * @param list confid list
     * @return confid list string separated with ,
     */
    public static <T> String list2String(final Collection<T> list) {
        return list2String(list, ",");
    }

    /**
     * @param rs     rsultset
     * @param output output stream
     * @throws SQLException if resultset read error
     */
    public static int printResultsTable(ResultSet rs, OutputStream output) throws SQLException {
        PrintWriter out = new PrintWriter(output);
        //if(!rs.next()) return;

        ResultSetMetaData metadata = rs.getMetaData();
        int numcols = metadata.getColumnCount(); // how many columns
        String[] labels = new String[numcols]; // the column labels
        int[] colwidths = new int[numcols]; // the width of each
        int[] colpos = new int[numcols]; // start position of each
        int linewidth; // total width of table
        linewidth = 1; // for the initial '|'.
        for (int i = 0; i < numcols; i++) { // for each column
            colpos[i] = linewidth; // save its position
            labels[i] = metadata.getColumnLabel(i + 1); // get its label
            int size = metadata.getColumnDisplaySize(i + 1);
            if (size == -1) {
                size = DEFAULT_COLUMN_WIDTH; // Some driver return -1...
            }
            if (size > 500) {
                size = DEFAULT_COLUMN_WIDTH; // Don't allow unreasonable sizes
            }
            int labelsize = labels[i].length();
            if (labelsize > size) {
                size = labelsize;
            }
            colwidths[i] = size + 1; // save the column the size
            linewidth += colwidths[i] + 2; // increment total size
        }
        StringBuffer divider = new StringBuffer(linewidth);
        StringBuffer blankline = new StringBuffer(linewidth);
        for (int i = 0; i < linewidth; i++) {
            divider.insert(i, '-');
            blankline.insert(i, " ");
        }
        // Put special marks in the divider line at the column positions
        for (int i = 0; i < numcols; i++) {
            divider.setCharAt(colpos[i] - 1, '|');
        }
        divider.setCharAt(linewidth - 1, '|');
        out.println(divider);
        StringBuffer line = new StringBuffer(blankline.toString());
        line.setCharAt(0, '|');
        for (int i = 0; i < numcols; i++) {
            int pos = colpos[i] + 1 + (colwidths[i] - labels[i].length()) / 2;
            overwrite(line, pos, labels[i]);
            overwrite(line, colpos[i] + colwidths[i], " |");
        }
        out.println(line);
        out.println(divider);
        int totalCount = 0;
        while (rs.next()) {
            line = new StringBuffer(blankline.toString());
            totalCount++;
            line.setCharAt(0, '|');
            for (int i = 0; i < numcols; i++) {
                Object value = rs.getObject(i + 1);
                if (value != null) {
                    overwrite(line, colpos[i] + 1, value.toString().trim());
                }
                overwrite(line, colpos[i] + colwidths[i], " |");
            }
            out.println(line);
        }
        out.println(divider);
        out.flush();
        return totalCount;
    }

    /**
     * @param b   String buffer
     * @param pos position
     * @param s   string
     */
    private static void overwrite(StringBuffer b, int pos, String s) {
        int slen = s.length(); // string length
        int blen = b.length(); // buffer length
        if (pos + slen > blen) {
            slen = blen - pos; // does it fit?
        }
        for (int i = 0; i < slen; i++) {
            // copy string into buffer
            b.setCharAt(pos + i, s.charAt(i));
        }
    }

    /**
     * @param args none
     */
    public static void main(String[] args) {
        List<Long> list = new java.util.ArrayList<Long>();
        for (long i = 0; i < 10; i++) {
            list.add(i);
        }
        System.out.println(list2String(list));

    }

}
