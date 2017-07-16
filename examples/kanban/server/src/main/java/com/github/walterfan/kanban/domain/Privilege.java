/**
 * 
 */
package com.github.walterfan.kanban.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;


/**
 * 
 * @author walter
 */
public class Privilege extends BaseObject {
    /**
     * DOCUMENT ME!
     */
    private static Log logger = LogFactory.getLog(Privilege.class);

    /**
     * DOCUMENT ME!
     */
    private Map< Integer, Integer > userPrivilege;

    /**
     * Creates a new Privilege object.
     */
    public Privilege() {
        userPrivilege = new HashMap< Integer, Integer >();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Map< Integer, Integer > getUserPrivilege() {
        return userPrivilege;
    }

    /**
     * DOCUMENT ME!
     *
     * @param userprivilege DOCUMENT ME!
     */
    public void setUserPrivilege(HashMap< Integer, Integer > userprivilege) {
        this.userPrivilege = userprivilege;
    }

    /**
     * DOCUMENT ME!
     *
     * @param moduleid DOCUMENT ME!
     * @param modulestaus DOCUMENT ME!
     */
    public void addPrivilege(int moduleid, int modulestaus) {
        this.userPrivilege.put(new Integer(moduleid), new Integer(modulestaus));
    }

    /**
     * DOCUMENT ME!
     *
     * @param moduleid DOCUMENT ME!
     */
    public void removePrivilege(int moduleid) {
        this.userPrivilege.remove(new Integer(moduleid));
    }

    /**
     * DOCUMENT ME!
     */
    public void clearPrivilege() {
        this.userPrivilege.clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPrivilegeNum() {
        return this.userPrivilege.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param moduleList DOCUMENT ME!
     */
    public void setUserPrivilegeByModuleList(List< Module > moduleList) {
        if ((moduleList == null) || moduleList.isEmpty()) {
            return;
        }

        ListIterator< Module > it = moduleList.listIterator();

        while (it.hasNext()) {
            Module m = (Module) it.next();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param moduleid DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int hasPrivilege(int moduleid) {
        Integer result = this.userPrivilege.get(new Integer(moduleid));

        if (result == null) {
            return -1;
        } else {
            return result.intValue();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param moduleids DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int hasPrivilege(String moduleids) {
        Integer result = null;

        try {
            String[] arrModule = moduleids.split(",");
            Integer moduleID = null;

            for (String strModule : arrModule) {
                moduleID = Integer.parseInt(strModule);
                result = this.userPrivilege.get(moduleID);

                if ((result != null) && (result  > 0)) {
                    return result;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());

            return -1;
        }

        if (result == null) {
            return -1;
        } else {
            return result.intValue();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        //result = (prime * result) + ((userPrivilege == null) ? 0 : userPrivilege.hashCode());
        if (userPrivilege == null) {
            result = prime * result;
        } else {
            result = (prime * result) + userPrivilege.hashCode();
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param obj DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Privilege other = (Privilege) obj;

        if (userPrivilege == null) {
            if (other.userPrivilege != null) {
                return false;
            }
        } else if (!userPrivilege.equals(other.userPrivilege)) {
            return false;
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        String result = getClass().getName();

        for (Iterator it = userPrivilege.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            result += ("moduleID=" + entry.getKey());
            result += (", moduleStatus=" + entry.getValue());
        }

        result += "]";

        return result;
    }
}