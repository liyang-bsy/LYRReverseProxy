package net.vicp.lylab.lyserver;

import java.net.ServerSocket;
import java.net.Socket;

import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;
//import net.vicp.lylab.lyserver.proxy.Proxy;
import net.vicp.lylab.lyserver.httpforward.Proxy;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * Actually, this class is not as useful as I thought
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class ProxyServer extends Task implements LifeCycle {
	private static final long serialVersionUID = -7606248773065595690L;
	
	protected AtomicBoolean isClosed = new AtomicBoolean(true);
	protected ServerSocket serverSocket;
	protected Integer port = null;
	protected Filter[] filters = null;

	protected LYTaskQueue lyTaskQueue = null;

	@Override
	public void initialize() {
		if(!isClosed.compareAndSet(true, false))
			return;
		this.begin("Sync Server - Main Thread");
	}
	
	@Override
	public void close() throws Exception {
		if(!isClosed.compareAndSet(false, true))
			return;
		this.callStop();
		serverSocket.close();
	}

	@Override
	public void exec() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			throw new LYException("Server start failed", e);
		}
		while (!isClosed.get()) {
			Socket client = null;
			try {
				client = serverSocket.accept();
				if(filters!=null)
					for(Filter filter:filters)
						filter.doFilter(client, null);
				Proxy proxy = new Proxy(client);
				proxy.exec();
			} catch (Exception e) {
				try {
					client.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				log.error(Utils.getStringFromException(e));
			}
		}
	}

	public boolean isClosed() {
		return isClosed.get();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public LYTaskQueue getLyTaskQueue() {
		return lyTaskQueue;
	}

	public void setLyTaskQueue(LYTaskQueue lyTaskQueue) {
		this.lyTaskQueue = lyTaskQueue;
	}

	public Filter[] getFilters() {
		return filters;
	}

	public void setFilters(Filter[] filters) {
		this.filters = filters;
	}

}
