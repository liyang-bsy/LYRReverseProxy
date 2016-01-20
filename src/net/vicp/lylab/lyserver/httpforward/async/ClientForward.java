package net.vicp.lylab.lyserver.httpforward.async;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.lyserver.protocol.HttpRequestHead;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.tq.Task;

public class ClientForward extends Task {
	private static final long serialVersionUID = -6990880338965237543L;
	
	protected Proxy proxy;
	protected OutputStream outputStream;

	private ByteBuffer buffer = ByteBuffer.allocate(CoreDef.SOCKET_MAX_BUFFER);
	
	public String debug=null;
	
	public ClientForward(Proxy proxy) throws Exception {
		this.proxy = proxy;
		outputStream = proxy.destination.getOutputStream();
	}

	static byte[] test = new byte[] {
			  0x4e ,0x4f ,0x53 ,0x0d ,0x0a, 0x4e ,0x4f ,0x54 ,0x0d ,0x0a };
	
	public static void main(String[] args) {
		int next = 0, start = 0;
		System.out.println(next=Algorithm.KMPSearch(test, HttpRequestHead.spliter, start));
		System.out.println(new String(test, start, next));
		System.out.println(start+=(next+HttpRequestHead.spliter.length));
		
		System.out.println(next=Algorithm.KMPSearch(test, HttpRequestHead.spliter, start));
		System.out.println();
		System.out.println(start+=(next+HttpRequestHead.spliter.length));
		
		System.out.println(test.length);
	}
	
	public long getChunkedContentSize(byte[] bytes, int offset, int len)
	{
		int next = 0, start = offset;
		long total = 0;
		while((next = Algorithm.KMPSearch(bytes, HttpRequestHead.spliter, start))!=-1)
		{
			System.out.println("next:" + next);
			System.out.println("data:\t\t"+new String(bytes, start, next));
			String hex = new String(test, start, next);
			total += Long.valueOf(hex, 16);
			start+=(next+HttpRequestHead.spliter.length);
			System.out.println("start:" + start);
		}
		return total;
	}
	
	@Override
	public void exec() {
		// Content_Length = 1, chunked = 2;
		try {
			int getLen = 0;
			int attempts = 0;
			int readTailLen = 0;
			while (!proxy.isClosed.get()) {
				buffer.clear();
				getLen = proxy.source.read(buffer);
				if (getLen == 0) {
					if (attempts > 3) {
						try {
							proxy.close();
						} catch (Exception e) {
							throw new LYException("Lost connection to client, and close socket channel failed", e);
						}
						throw new LYException("Lost connection to client");
					}
					attempts++;
					continue;
				}
				if (getLen == -1)
					break;
				getLen += readTailLen;
				outputStream.write(buffer.array());
			}				
		} catch (Exception e) {
			log.error(Utils.getStringFromException(e));
		} finally {
			proxy.close();
		}
	}
	
}
