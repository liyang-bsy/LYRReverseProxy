package net.vicp.lylab.lyserver.model;

import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.model.CacheValue;

public class CacheValueEntry extends CloneableBaseObject {
	String key;
	CacheValue cv;
	
	public CacheValueEntry() { }
	
	public CacheValueEntry(String key, CacheValue cv) {
		super();
		this.key = key;
		this.cv = cv;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public CacheValue getCv() {
		return cv;
	}

	public void setCv(CacheValue cv) {
		this.cv = cv;
	}
	
}
