package examples.services.autoadaptable.plans;

import org.objectweb.proactive.extra.component.mape.analysis.Alarm;
import org.objectweb.proactive.extra.component.mape.execution.ExecutorController;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValueException;
import org.objectweb.proactive.extra.component.mape.planning.Plan;

import examples.services.autoadaptable.AASCST;

public class AdaptationPlan extends Plan {

	private static final long serialVersionUID = 1L;

	@Override
	public void planActionFor(String ruleName, Alarm alarm, MonitorController monitor, ExecutorController executor) {
		
		if (alarm != Alarm.VIOLATION )
			return;

		try {
			String points = (String) monitor.getMetricValue(AASCST.OPTIMAL_POINTS_METRIC).getValue();
			if (points != null) {
				
				executor.execute("set-value($this/child::" + AASCST.MANAGER_COMP_NAME
					+ "/attribute::points, \"" + points + "\");");
			}
		} catch (WrongMetricValueException e) {
			e.printStackTrace();
		}
	}

}
