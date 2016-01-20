package net.vicp.lylab.lyserver.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import net.vicp.lylab.utils.tq.Task;

public class Transmission extends Task {
	private static final long serialVersionUID = -6990880338965237543L;
	
	protected Proxy proxy;
	protected InputStream inputStream;
	protected OutputStream outputStream;
	
	public String debug=null;
	
	public Transmission(Proxy proxy, InputStream inputStream, OutputStream outputStream) throws Exception {
		this.proxy = proxy;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}
	
	@Override
	public void exec() {
		int inputCount = 0;
		try {
			byte[] buffer = new byte[2048];
			Arrays.fill(buffer, (byte) 0);
			int getLen = 0;
			while (!proxy.isClosed.get()) {
				getLen = inputStream.read(buffer, 0, buffer.length);
				
				// TODO debug
				if(debug != null && inputCount < 100) {
					log.debug(debug + "发出如下数据:" + new String(Arrays.copyOf(buffer,getLen)));
					inputCount += getLen;
				}
				
				if (getLen == -1) {
					proxy.close();
					return;
				}
				outputStream.write(buffer, 0, getLen);
			}
		} catch (Exception e) {
//			e.printStackTrace();
			proxy.close();
		}
	}
	
}
