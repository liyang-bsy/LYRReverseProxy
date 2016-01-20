package net.vicp.lylab.lyserver.timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.ebaolife.core.model.MallMessageRecv;
import net.ebaolife.core.utils.HttpUtils;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.timer.TimerJob;

public class HeartBeat extends TimerJob {
	
	private String serverName;
	
	@Override
	public Date getStartTime() {
		Calendar cl = Calendar.getInstance();
		cl.add(Calendar.SECOND, 30);
		return cl.getTime();
	}

	@Override
	public Integer getInterval() {
		return 1*MINUTE;
	}

	@Override
	public void exec() {
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		MallMessageRecv mmr = new MallMessageRecv();
		mmr.setKey("Heartbeat");
		mmr.getBody().put("hKey", serverName + "最后响应时间");
		mmr.getBody().put("hValue", now);
		mmr.getBody().put("lastTime", now);
		try {
			String url = CoreDef.config.getString("MonitorServerUrl" + CoreDef.config.getInteger("debug"));
			HttpUtils.sendPost(url, Utils.serialize(mmr));
		} catch (Exception e) { e.printStackTrace(); }
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

}
