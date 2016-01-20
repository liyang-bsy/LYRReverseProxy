package net.vicp.lylab.lyserver;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.lyserver.httpforward.async.Proxy;
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
public class AsyncProxyServer extends Task implements LifeCycle {
	private static final long serialVersionUID = -7606248773065595690L;
	
	protected AtomicBoolean isClosed = new AtomicBoolean(true);
	
	protected Selector selector = null;
	// Not null unless isServer() == false
	protected SocketChannel socketChannel = null;

	// IP mapping
	protected Map<String, SocketChannel> ipMap = new ConcurrentHashMap<String, SocketChannel>();
	
	protected Integer port = null;
	protected Filter[] filters = null;

	protected LYTaskQueue lyTaskQueue = null;

	@Override
	public void initialize() {
		if(!isClosed.compareAndSet(true, false))
			return;
		try {
			selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (Exception e) {
			log.error(Utils.getStringFromException(e));
		}
		
		this.begin("Sync Server - Main Thread");
	}
	
	@Override
	public void close() throws Exception {
		if(!isClosed.compareAndSet(false, true))
			return;
		if (selector != null) {
			selector.close();
			selector = null;
		}
		if (thread != null)
			callStop();
	}

	private void selectionKeyHandler(SelectionKey selectionKey)
	{
		if (selectionKey.isAcceptable()) {
			try {
				ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
				SocketChannel socketChannel = serverSocketChannel.accept();
				socketChannel.configureBlocking(false);
				Socket socket = socketChannel.socket();
				ipMap.put(socket.getInetAddress().getHostAddress(), socketChannel);
				socketChannel.register(selector, SelectionKey.OP_READ);
				
				if(filters!=null)
					for(Filter filter:filters)
						filter.doFilter(socket, null);
			} catch (Exception e) {
				throw new LYException("Close failed", e);
			}
		} else if (selectionKey.isReadable()) {
			SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
			try {
				Proxy proxy = new Proxy(socketChannel);
				proxy.exec();
			} catch (Throwable t) {
				if (socketChannel != null) {
					try {
						socketChannel.close();
					} catch (Exception ex) {
						log.error("Close failed"
								+ Utils.getStringFromException(ex));
					}
					socketChannel = null;
				}
				log.error(Utils.getStringFromThrowable(t));
			}
		} else if (selectionKey.isWritable()) {
			System.out.println("TODO: isWritable()");
		} else if (selectionKey.isConnectable()) {
			System.out.println("TODO: isConnectable()");
		} else {
			System.out.println("TODO: else");
		}
		
	}
	
	@Override
	public void exec() {
		// Will be block here
		try {
			while (selector.select() > 0) {
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey selectionKey = iterator.next();
					iterator.remove();
					selectionKeyHandler(selectionKey);
				}
			}
		} catch (Exception e) {
			log.error(Utils.getStringFromException(e));
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
