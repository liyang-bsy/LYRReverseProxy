package net.vicp.lylab.lyserver.httpforward;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.lyserver.protocol.HttpHeadProtocol;
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
	
	public static final Protocol protocol = new HttpHeadProtocol();
	public static final Random rd = new Random();
	
	public final String uuid = "[" + UUID.randomUUID().toString().replaceAll("-", "") + "]";
	
	protected Socket source, destination;
	protected ClientForward reader;
	protected ServerChannel sender;
	protected AtomicBoolean isClosed = new AtomicBoolean(false);
	
	public Proxy(Socket source) {
		this.source = source;
		this.timeout = CoreDef.MINUTE;
		log.debug(uuid + "连接来源IP:" + source.getInetAddress().getHostAddress());
	}
	
	@Override
	public void exec() {
		try {
			destination = selectServer();
			reader = new ClientForward(this);
			reader.debug = source.getInetAddress().getHostAddress();
			((LYTaskQueue) CoreDef.config.getConfig("Singleton").getObject("LYTaskQueue")).addTask(reader);
			sender = new ServerChannel(this);
			sender.begin("peer - " + reader.getTaskId());
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}
	
	private Socket selectServer() throws IOException {
		int nextServer = rd.nextInt(CoreDef.config.getConfig("ServerConfig").getInteger("serverCount"));
		return new Socket(CoreDef.config.getConfig("ServerConfig").getString("serverList").split("\\,")[nextServer]
				, CoreDef.config.getConfig("ServerConfig").getInteger("forwardPort"));
	}
	
	public void close() {
		if(isClosed.compareAndSet(false, true)) {

			log.debug(uuid + "断开IP:" + source.getInetAddress().getHostAddress());
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

	public ClientForward getReader() {
		return reader;
	}

	public ServerChannel getSender() {
		return sender;
	}

	public boolean isClosed() {
		return isClosed.get();
	}

}
