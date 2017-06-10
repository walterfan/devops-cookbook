package com.github.walterfan.msa.common.domain;


import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Module extends BaseObject implements Comparable<Module> {
    private int moduleID;
    
    private String moduleName;
    
    private String description;
    
    private int parentModuleID;
    
    private int moduleStatus=0;
    
    private String moduleLink;

    private List<Module> subModuleList = new ArrayList<Module>(5);
    
    private List<Function> functionList = new ArrayList<Function>(5);
    
    
    public void setSubModuleList(List<Module> subModuleList) {
		this.subModuleList = subModuleList;
	}

	public List<Function> getFunctionList() {
		return functionList;
	}

	public void setFunctionList(List<Function> functionList) {
		this.functionList = functionList;
	}

	public String getModuleLink() {
		return moduleLink;
	}

	public void setModuleLink(String moduleLink) {
		this.moduleLink = moduleLink;
	}
    
    public Module() {
        
    }
   
    public Module(String moduleName, String description) {
    	this.moduleName = moduleName;
    	this.description = description;
    }
    /**
     * @return the moduleID
     */
    public int getModuleID() {
        return moduleID;
    }
    
    /**
     * @param moduleID the moduleID to set
     */
    public void setModuleID(int moduleID) {
        this.moduleID = moduleID;
    }
    
    /**
     * @return the moduleName
     */
    public String getModuleName() {
        return moduleName;
    }
    
    /**
     * @param moduleName the moduleName to set
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
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
    
    /**
     * @return the parentModuleID
     */
    public int getParentModuleID() {
        return parentModuleID;
    }
    
    /**
     * @param parentModuleID the parentModuleID to set
     */
    public void setParentModuleID(int parentModuleID) {
        this.parentModuleID = parentModuleID;
    }
    
      
    public void addSubModule(Module module) {
    	this.subModuleList.add(module);
    }
    
    public List<Module> getSubModuleList() {
    	return this.subModuleList;
    }

    public boolean hasSubModule() {
    	return !CollectionUtils.isEmpty(this.subModuleList);
    }
    
    public static int compare(Module o1, Module o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return 1;
        } else if (o2 == null) {
            return -1;
        } else {
            if (o1.getModuleID() > o2.getModuleID()) {
                return 1;
            } else if (o1.getModuleID() == o2.getModuleID()) {
                return 0;
            } else {
                return -1;
            }

        }

    }

    public int compareTo(Module o) {
        if(o == null || this.getModuleID() > o.getModuleID()) {
            return 1;
        } else if(this.getModuleID() == o.getModuleID()) {
            return 0;
        } else {
            return -1;
        }
    }
    
    public static List<Module> tidyModules(List<Module> rawModuleList) {
		Map<Integer, Module> parentModuleMap = new HashMap<Integer, Module>(10);
		List<Module> subModuleList = new ArrayList<Module>(20);
		for (Module module : rawModuleList) {
			if (module.getParentModuleID() == 0) {
				parentModuleMap.put(module.getModuleID(), module);
			} else {
				subModuleList.add(module);
			}
		}

		for (Module module : subModuleList) {
			Module pm = parentModuleMap.get(module.getParentModuleID());
			if (pm != null) {
				pm.addSubModule(module);
			}
		}
		List<Module> moduleList = new ArrayList<Module>(parentModuleMap.size());
		moduleList.addAll(parentModuleMap.values());
		Collections.sort(moduleList);
		return moduleList;
	}

    
    /**
     * @return the moduleStatus
     */
    public int getModuleStatus() {
        return moduleStatus;
    }

    
    /**
     * @param moduleStatus the moduleStatus to set
     */
    public void setModuleStatus(int moduleStatus) {
        this.moduleStatus = moduleStatus;
    }


}
