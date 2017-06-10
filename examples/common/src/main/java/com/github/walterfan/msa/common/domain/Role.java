package com.github.walterfan.msa.common.domain;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * @author walter
 *
 */
public class Role extends BaseObject {
    private static Log logger = LogFactory.getLog(Role.class);
    
	private int roleID;
	private String roleName;
	private String description;
	private int parentRoleID = 0;
	
	private List<Module> moduleList;
	
	public int getParentRoleID() {
		return parentRoleID;
	}

	public void setParentRoleID(int parentRoleID) {
		this.parentRoleID = parentRoleID;
	}

	public Role() {
		
	}
	
	public Role(int roleID, String roleName) {
		this.roleID = roleID;
		this.roleName = roleName;
	}
	
	public int getRoleID() {
		return roleID;
	}
	public void setRoleID(int roleID) {
		this.roleID = roleID;
	}
	
	
	
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

    
    
    
    public boolean isSuperAdmin() {
    	return "SuperAdmin".equalsIgnoreCase(this.roleName);
    }
    


    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    //----------------------------- Module---------------
    /**
     * @return the moduleList
     */
    public List<Module> getModuleList() {
        return moduleList;
    }

    public void addModule(Module module) {
        if(moduleList == null) {
        	moduleList = new ArrayList<Module>();
        }
        moduleList.add(module);
    } 
    /**
     * @param moduleList the moduleList to set
     */
    public void setModuleList(List<Module> moduleList) {
        this.moduleList = Module.tidyModules(moduleList);
    }
    
    public Module getModuleByLink(String query) {
		if (CollectionUtils.isEmpty(moduleList)
				|| StringUtils.isEmpty(query)) {
		    logger.error("cannot getModuleByLink: " + query);
			return null;
		}
		Module curModule = null;
		for (Module mo : moduleList) {
			if (StringUtils.startsWith(query, mo.getModuleLink())) {
				curModule = mo;
				break;
			}
			if(CollectionUtils.isEmpty(mo.getSubModuleList())) {
				continue;
			}
			for(Module subModule: mo.getSubModuleList()) {
				if(StringUtils.startsWith(query, subModule.getModuleLink())) {
					curModule = subModule;
				}
			}
		}
		if(curModule == null) {
			return null;
		}
		
        if(curModule.getModuleStatus()<=0 && !isSuperAdmin()) {
        	return null;
        }
		return curModule;
	}

	@Override
	public String toString() {
		return "Role [description=" + description + ", moduleList="
				+ moduleList + ", parentRoleID=" + parentRoleID + ", roleID="
				+ roleID + ", roleName=" + roleName + "]";
	}
    
    
}
