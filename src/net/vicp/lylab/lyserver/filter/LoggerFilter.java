package net.vicp.lylab.lyserver.filter;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.lyserver.utils.Logger;
import net.vicp.lylab.server.filter.Filter;

public class LoggerFilter extends NonCloneableBaseObject implements Filter {

	static private List<Pair<InetAddress, Date>> analysisLogic = new LinkedList<>();;
	
	@Override
	public Message doFilter(Socket socket, Message request) throws LYException {
		InetAddress hostAddr = socket.getInetAddress();
		Date now = new Date();
		((Logger) CoreDef.config.getConfig("Singleton").getObject("Logger")).appendLine(
				hostAddr.getHostAddress() +
				"\t" +
				DateFormatUtils.format(now, "yyyy-MM-dd HH:mm:ss:SSS"));

		synchronized (analysisLogic) {
			analysisLogic.add(new Pair<>(hostAddr, now));
		}
		return null;
	}

	@Override
	public void initialize() {
	}

	@Override
	public void close() throws Exception {
	}

	public static List<Pair<InetAddress, Date>> getAnalysisLogic() {
		return analysisLogic;
	}

}
