package net.vicp.lylab.lyserver.servlet;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.lyserver.filter.LoggerFilter;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.Command;
import net.vicp.lylab.utils.Utils;

/**
 * 后台服务运行状态
 */
@WebServlet("/runtime")
public class Runtime extends HttpServlet {
	private static final long serialVersionUID = 3200296505570525661L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unused")
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		StringBuilder content = new StringBuilder(1024);
		int second = 30;
		// 命令模式
		if(!StringUtils.isBlank(request.getParameter("cmdMode")))
		{
			String cmd = request.getParameter("cmdMode");
			if(false);
			else if(cmd.equals("setSecond"))
			{
				String sSec = request.getParameter("second");
				if(!StringUtils.isBlank(sSec))
					second = Integer.valueOf(sSec);
			}
		}
		content.append("<!DOCTYPE html>");
		content.append("<html>");
		content.append("<meta charset=\"UTF-8\">");
		content.append("<body>");
		content.append("当前时间：&nbsp;&nbsp;&nbsp;&nbsp;" + System.currentTimeMillis() + "<br/>");
		content.append("访问者IP：&nbsp;&nbsp;&nbsp;&nbsp;" + net.ebaolife.core.utils.Utils.getIpAddr(request) + "<br/>");
		content.append("时间模式：&nbsp;&nbsp;&nbsp;&nbsp;" + second + "秒<br/>");
		
		// TQ
		LYTaskQueue tq = (LYTaskQueue) CoreDef.config.getConfig("Singleton").getObject("LYTaskQueue");
		content.append("任务队列-队列：&nbsp;&nbsp;&nbsp;&nbsp;" + tq.getWaitingTaskCount() + "<br/>");
		content.append("任务队列-线程：&nbsp;&nbsp;&nbsp;&nbsp;" + tq.getRunningThreadCount() + "<br/>");
		content.append("任务队列-最大队列：&nbsp;&nbsp;&nbsp;&nbsp;" + tq.getMaxQueue() + "<br/>");
		content.append("任务队列-最大线程：&nbsp;&nbsp;&nbsp;&nbsp;" + tq.getMaxThread() + "<br/>");
		content.append("全局线程：&nbsp;&nbsp;&nbsp;&nbsp;" + Thread.activeCount() + "<br/>");

		List<Pair<InetAddress, Date>> list = LoggerFilter.getAnalysisLogic();
		synchronized (list) {
			final Map<String, Integer> result = new HashMap<>();
			List<String> ipSeq = new ArrayList<>();
			Iterator<Pair<InetAddress, Date>> it = list.iterator();
			while (it.hasNext()) {
				Pair<InetAddress, Date> pair = it.next();
				if (new Date().getTime() - pair.getRight().getTime() < second * CoreDef.SECOND) {
					String ip = pair.getLeft().getHostAddress();
					if (result.get(ip) == null) {
						result.put(ip, 1);
						ipSeq.add(ip);
					} else {
						result.put(ip, result.get(ip) + 1);
					}
				}
			}
			Collections.sort(ipSeq, new Comparator<String>() {
	            public int compare(String ip1, String ip2) {
	                return result.get(ip2).compareTo(result.get(ip1));
	            }
	        });
			content.append("<br/>");
			content.append("IP：&nbsp;&nbsp;&nbsp;&nbsp;" + "次数<br/>");
			for(String ip : ipSeq)
				content.append(ip + "&nbsp;&nbsp;&nbsp;&nbsp;" + result.get(ip) + "次<br/>");
		}

		content.append("<br/>");

		StringBuilder port = new StringBuilder();
		port.append("<table border='1'>");
		String result = Command.execute("netstat -an | findstr \":80\"").getRight();
		result = Utils.deleteCRLF(result);
		boolean trFlag = false, thFlag = false;
		for (String sentence : result.split("\n")) {
			sentence = sentence.trim();
			if (trFlag)
				port.append("</tr>");
			else
				trFlag = true;
			thFlag = false;
			port.append("<tr>");
			for (String value : sentence.split("[\\s]+")) {
				if (thFlag)
					port.append("</th>");
				else
					thFlag = true;
				port.append("<th>");
				port.append(value);
			}
		}
		port.append("</tr>");
		port.append("</table>");
		content.append("端口命令：" + port.toString() + "<br/>");
		content.append("<br/>\n");
		content.append("</body>");
		content.append("</html>");
		response.getOutputStream().write(content.toString().getBytes("UTF-8"));
	}

}
