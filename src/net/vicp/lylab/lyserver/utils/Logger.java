package net.vicp.lylab.lyserver.utils;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.utils.permanent.AsyncDiskPermanent;

/**
 * 必须在LYCache初始化后才能使用
 * 
 * @author Young
 *
 */
public class Logger extends NonCloneableBaseObject implements LifeCycle {

	AsyncDiskPermanent adp = null;
	private String filePath;
	private String fileSuffix;
	private String caller = "Logger";
	
	@Override
	public void initialize() {
		adp = new AsyncDiskPermanent(filePath, fileSuffix, caller);
	}

	@Override
	public void close() throws Exception {
		adp.close();
	}

	public boolean appendLine(String entry) {
		return adp.appendLine(entry);
	}

	public final void join() throws InterruptedException {
		adp.join();
	}

	public final boolean join(Long millis) throws InterruptedException {
		return adp.join(millis);
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

}
