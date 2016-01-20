package net.vicp.lylab.lyserver;

import java.io.File;
import java.io.IOException;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

public class ServerRuntime extends Task implements LifeCycle {
	private static final long serialVersionUID = 7479759144833205546L;

	public static void main(String[] arg) throws InterruptedException, IOException {
		CoreDef.config = new Config(CoreDef.rootPath + File.separator + "config" + File.separator + "config.txt");
//		GlobalInitializer.createInstance();

		int interval = 5;
		for(int j = 0;j<Integer.MAX_VALUE;j+=interval)
		{
			Thread.sleep(interval*1000);
			System.out.println("Sc:" + j + "\tST:" + Thread.activeCount()
					 + "\tTQ:" + (((LYTaskQueue) CoreDef.config.getConfig("Singleton").getObject("LYTaskQueue")).getWaitingTaskCount())
					 + "\tThQ:" + (((LYTaskQueue) CoreDef.config.getConfig("Singleton").getObject("LYTaskQueue")).getRunningThreadCount()));
		}
	}

	@Override
	public void exec() {
		int interval = 5;
		for(int j = 0;j<Integer.MAX_VALUE;j+=interval) {
			try {
				Thread.sleep(interval*1000);
			} catch (InterruptedException e) {
				break;
			}
			System.out.println("Sc:" + j + "\tST:" + Thread.activeCount()
					 + "\tTQ:" + (((LYTaskQueue) CoreDef.config.getConfig("Singleton").getObject("LYTaskQueue")).getWaitingTaskCount())
					 + "\tThQ:" + (((LYTaskQueue) CoreDef.config.getConfig("Singleton").getObject("LYTaskQueue")).getRunningThreadCount()));
		}
	}

	@Override
	public void initialize() {
		CoreDef.config = new Config(CoreDef.rootPath + "WEB-INF" + File.separator + "classes" + File.separator + "config.txt");
//		GlobalInitializer.createInstance();
	}

	@Override
	public void close() {
		CoreDef.config.deepClose();
//		GlobalInitializer.destroyInstance();
	}

}
