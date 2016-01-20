package net.vicp.lylab.lyserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.vicp.lylab.core.CloneableBaseObject;

public class UserResource extends CloneableBaseObject {
	@JsonIgnoreProperties
	protected Integer user_role_id;
	protected Integer user_id;
	protected Integer role_id;

	public Integer getUser_role_id() {
		return user_role_id;
	}

	public void setUser_role_id(Integer user_role_id) {
		this.user_role_id = user_role_id;
	}

	public Integer getUser_id() {
		return user_id;
	}

	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}

	public Integer getRole_id() {
		return role_id;
	}

	public void setRole_id(Integer role_id) {
		this.role_id = role_id;
	}

}
