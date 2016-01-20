package net.vicp.lylab.lyserver.httpforward;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.tq.Task;

public class ServerChannel extends Task {
	private static final long serialVersionUID = -6990880338965237543L;
	
	protected Proxy proxy;
	protected InputStream inputStream;
	protected OutputStream outputStream;
	
	public ServerChannel(Proxy proxy) throws Exception {
		this.proxy = proxy;
		inputStream = proxy.destination.getInputStream();
		outputStream = proxy.source.getOutputStream();
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

				log.debug(proxy.uuid + "服务器发给客户端" + proxy.reader.debug + "如下数据:" + new String(Arrays.copyOf(buffer, getLen)));
				outputStream.write(buffer, 0, getLen);
			}
		} catch (Exception e) {
			log.info(proxy.uuid + "与服务器通信发生异常" + Utils.getStringFromException(e));
		} finally {
			log.info(proxy.uuid + "与服务器通信，试图关闭端口");
			proxy.close();
		}
	}
	
}
