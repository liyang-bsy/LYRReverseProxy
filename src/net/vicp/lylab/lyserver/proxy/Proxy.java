package net.vicp.lylab.lyserver.proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

/**
 * A simple proxy
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class Proxy extends Task {
	private static final long serialVersionUID = -2369719619896797216L;
	
	protected Socket source, destination;
	protected Transmission reader, sender;
	protected AtomicBoolean isClosed = new AtomicBoolean(false);
	
	public Proxy(Socket source) {
		this.source = source;
		this.timeout = CoreDef.MINUTE;
		log.debug("来源IP:" + source.getInetAddress().getHostAddress());
	}
	
	@Override
	public void exec() {
		try {
			destination = selectServer();
			reader = new Transmission(this, source.getInputStream(), destination.getOutputStream());
			reader.debug = source.getInetAddress().getHostAddress();
			sender = new Transmission(this, destination.getInputStream(), source.getOutputStream());
			((LYTaskQueue) CoreDef.config.getConfig("Singleton").getObject("LYTaskQueue")).addTask(reader);
			sender.begin("peer - " + reader.getTaskId());
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}
	
	private Socket selectServer() throws IOException {
		Random rd = new Random();
		int nextServer = rd.nextInt(CoreDef.config.getConfig("ServerConfig").getInteger("serverCount"));
		return new Socket(CoreDef.config.getConfig("ServerConfig").getString("serverList").split("\\,")[nextServer]
				, CoreDef.config.getConfig("ServerConfig").getInteger("forwardPort"));
	}
	
	public void close() {
		if(isClosed.compareAndSet(false, true)) {

			log.debug("断开IP:" + source.getInetAddress().getHostAddress());
			try {
				if(reader != null) reader.callStop();
				if(sender != null) sender.callStop();
				if (source != null) {
					source.close();
					source = null;
				}
				if (destination != null) {
					destination.close();
					destination = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public Transmission getReader() {
		return reader;
	}

	public Transmission getSender() {
		return sender;
	}

	public boolean isClosed() {
		return isClosed.get();
	}

}
