package com.github.walterfan.kanban.dao;

import org.springframework.dao.DataAccessException;

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * Thrown if a record didnot exist by the search condition
 */
public class NoResultsFoundException extends DataAccessException {
    /**
     * DOCUMENT ME!
     */
    private Throwable nestedThrowable = null;

    /**
     * Creates a new NoResultsFoundException object.
     *
     * @param msg DOCUMENT ME!
     */
    public NoResultsFoundException(String msg) {
        super(msg);
    }

    /**
     * Creates a new NoResultsFoundException object.
     *
     * @param msg DOCUMENT ME!
     * @param nestedThrowable DOCUMENT ME!
     */
    public NoResultsFoundException(String msg, Throwable nestedThrowable) {
        super(msg);
        this.nestedThrowable = nestedThrowable;
    }

    /**
     * DOCUMENT ME!
     */
    public void printStackTrace() {
        super.printStackTrace();

        if (nestedThrowable != null) {
            nestedThrowable.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param ps DOCUMENT ME!
     */
    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);

        if (nestedThrowable != null) {
            nestedThrowable.printStackTrace(ps);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param pw DOCUMENT ME!
     */
    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
    }
}
