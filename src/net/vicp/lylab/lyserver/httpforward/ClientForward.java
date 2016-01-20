package net.vicp.lylab.lyserver.httpforward;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.lyserver.protocol.HttpRequestHead;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.tq.Task;

public class ClientForward extends Task {
	private static final long serialVersionUID = -6990880338965237543L;
	
	protected Proxy proxy;
	protected InputStream inputStream;
	protected OutputStream outputStream;
	
	private byte[] buffer = new byte[4096];
	
	public String debug=null;
	
	public ClientForward(Proxy proxy) throws Exception {
		this.proxy = proxy;
		inputStream = proxy.source.getInputStream();
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
//			System.out.println("next:" + next);
//			System.out.println("data:\t\t"+new String(bytes, start, next));
			String hex = new String(test, start, next);
			total += Long.valueOf(hex, 16);
			start+=(next+HttpRequestHead.spliter.length);
//			System.out.println("start:" + start);
		}
		return total;
	}
	
	@Override
	public void exec() {
		// Content_Length = 1, chunked = 2;
		HttpRequestHead hrh = null;
		int headSize = 0;
		boolean isSentHead = false;
		try {
			Arrays.fill(buffer, (byte) 0);
			int getLen = 0;
			boolean getHead = false;
			int mode = 0;
			long contentLength = 0;
			while (!proxy.isClosed.get()) {
				int nextread = inputStream.read(buffer, getLen, buffer.length - getLen);
				if (nextread == -1) {
					break;
				}
				getLen += nextread;
				if (!getHead) {
					if ((headSize = Proxy.protocol.validate(buffer, getLen)) > 0) {
						hrh = (HttpRequestHead) Proxy.protocol.decode(buffer);
						log.debug(proxy.uuid + debug + "传入如下请求头:" + new String(Proxy.protocol.encode(hrh)));
						
//						if("keep-alive".equals(hrh.getHead("Connection")))
//							keepAlive = true;
						if ("chunked".equals(hrh.getHead("Transfer-Encoding")))
							mode = 2;
						if (!StringUtils.isBlank(hrh.getHead("Content-Length"))) {
							mode = 1;
							contentLength = Long.valueOf(hrh.getHead("Content-Length"));
						}
						
						String forward = hrh.getHead("X-Forwarded-For");
						if (!StringUtils.isBlank(forward))
							forward += ",";
						else
							forward = "";
						forward += proxy.source.getInetAddress().getHostAddress();
						
						hrh.addHead("X-Forwarded-For", forward);
						hrh.addHead("X-Forwarded-Proto", "http");
						hrh.addHead("X-Forwarded-Host", CoreDef.config.getString("serverIp"));

						getHead = true;
						
						// http访问代理服务器模式
//						InetAddress addr = InetAddress.getByName(hrh.getHead("host"));
//						proxy.destination = new Socket(addr, 80);
//						outputStream = proxy.destination.getOutputStream();
//						proxy.sender = new ServerChannel(proxy);
//						proxy.sender.begin("peer - " + this.getTaskId());
					}
					else {
						headSize = 0;
						if (getLen > buffer.length - 128)
							buffer = Arrays.copyOf(buffer, buffer.length * 10);
						continue;
					}
				}
				if(!isSentHead) {
					isSentHead = true;
					log.debug(proxy.uuid + "为" + debug + "转发了如下请求头:" + new String(Proxy.protocol.encode(hrh)));
					outputStream.write(Proxy.protocol.encode(hrh));
//					switch (mode) {
//					case 1:
//						outputStream.write(buffer, headSize, getLen - headSize);
//						contentLength -= getLen - headSize;
//						break;
//					case 2:
//						contentLength += getChunkedContentSize(buffer, headSize, getLen - headSize);
//						outputStream.write(buffer, headSize, getLen - headSize);
//						contentLength -= getLen - headSize;
//						break;
//					}
				}
				switch (mode) {
				case 1:
					outputStream.write(buffer, headSize, getLen-headSize);
					contentLength -= getLen - headSize;
					break;
				case 2:
					contentLength += getChunkedContentSize(buffer, headSize, getLen-headSize);
					outputStream.write(buffer, 0, getLen);
					contentLength -= getLen - headSize;
					break;
				}
				headSize = 0;
				if(contentLength == 0) {
					getHead = false;
					isSentHead = false;
				}
				getLen = 0;
				Arrays.fill(buffer, (byte) 0);
			}
		} catch (Exception e) {
			log.info(proxy.uuid + "与客户端通信发生异常" + Utils.getStringFromException(e));
			log.info(proxy.uuid + "最后的数据如下" + new String(buffer).trim());
			
		} finally {
			log.info(proxy.uuid + "与客户端通信，试图关闭端口");
			proxy.close();
		}
	}
	
}
