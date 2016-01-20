package net.vicp.lylab.lyserver.httpforward.async;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import net.vicp.lylab.utils.tq.Task;

public class ServerChannel extends Task {
	private static final long serialVersionUID = -6990880338965237543L;
	
	protected Proxy proxy;
	protected InputStream inputStream;
	
	public ServerChannel(Proxy proxy) throws Exception {
		this.proxy = proxy;
		inputStream = proxy.destination.getInputStream();
	}
	
	@Override
	public void exec() {
		try {
			byte[] buffer = new byte[2048];
			Arrays.fill(buffer, (byte) 0);
			int getLen = 0;
			while (!proxy.isClosed.get()) {
				getLen = inputStream.read(buffer, 0, buffer.length);
				if (getLen == -1) {
					break;
				}

				log.debug("服务器发给客户端" + proxy.reader.debug + "如下数据:" + new String(Arrays.copyOf(buffer, getLen)));
				proxy.source.write(ByteBuffer.wrap(buffer));
			}
		} catch (Exception e) {
		} finally {
			proxy.close();
		}
	}
	
}
