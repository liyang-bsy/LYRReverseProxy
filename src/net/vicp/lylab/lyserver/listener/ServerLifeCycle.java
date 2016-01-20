package net.vicp.lylab.lyserver.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.lyserver.ServerRuntime;

/**
 * Application life cycle listener
 */
public class ServerLifeCycle implements ServletContextListener {

	ServerRuntime serverRuntime;
	
	/**
	 * Default constructor.
	 */
	public ServerLifeCycle() {
		
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0) {
		// 设置服务部署的绝对文件地址
		CoreDef.rootPath = arg0.getServletContext().getRealPath("/");
		
		serverRuntime = new ServerRuntime();
		serverRuntime.initialize();
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		serverRuntime.close();
	}

}
