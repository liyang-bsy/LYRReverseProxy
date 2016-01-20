package net.vicp.lylab.lyserver.filter;

import java.net.Socket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Utils;

public class IPFilter extends NonCloneableBaseObject implements Filter {

	@Override
	public Message doFilter(Socket socket, Message request) throws LYException {
		String host = socket.getInetAddress().getHostAddress();
		if(Utils.inList(CoreDef.config.getConfig("ServerConfig").getString("ipBlackList").split(","), host))
			throw new LYException("IP[" + host + "] is forbidden");
		return null;
	}

	@Override
	public void initialize() {
	}

	@Override
	public void close() throws Exception {
	}

}
