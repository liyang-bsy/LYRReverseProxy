package net.vicp.lylab.lyserver.timer;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.lyserver.filter.LoggerFilter;
import net.vicp.lylab.utils.timer.TimerJob;

public class AnalysisLogicCleaner extends TimerJob {
	
	@Override
	public Date getStartTime() {
		Calendar cl = Calendar.getInstance();
		cl.add(Calendar.SECOND, 30);
		return cl.getTime();
	}

	@Override
	public Integer getInterval() {
		return 2*MINUTE;
	}

	@Override
	public void exec() {
		List<Pair<InetAddress, Date>> list = LoggerFilter.getAnalysisLogic();
		synchronized (list) {
			Iterator<Pair<InetAddress, Date>> it = list.iterator();
			while(it.hasNext())
				if(new Date().getTime() - it.next().getRight().getTime() > 2*CoreDef.MINUTE)
					it.remove();
		}
	}

}
