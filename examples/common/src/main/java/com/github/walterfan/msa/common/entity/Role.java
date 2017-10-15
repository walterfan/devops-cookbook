package com.github.walterfan.msa.common.entity;


import com.github.walterfan.msa.common.domain.BaseObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * @author walter
 *
 */
@Entity
@Table(name = "role")
public class Role extends BaseObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int roleID;
	private String roleName;
	private String description;
	private int parentRoleID = 0;


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

    
}
