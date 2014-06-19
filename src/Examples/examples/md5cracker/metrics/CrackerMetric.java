package examples.md5cracker.metrics;

import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

import examples.md5cracker.cracker.CCST;


/**
 * Designed to be used on Cracker composite component
 * @author mnip91
 *
 */
public class CrackerMetric extends Metric<Double> {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_NAME = "cracker-metric";

	private double total = 0;


	@Override
	public Double calculate() {
			total = 0;
			Wrapper<Double> mv = this.metricStore.calculate(SolverMetric.DEFAULT_NAME, "/" + CCST.CRACKER_ITF + "/" + CCST.SOLVER_C1);
			if (mv.isValid()) {
				total += (double) mv.getValue();
			}

			mv = this.metricStore.calculate(SolverMetric.DEFAULT_NAME, "/" + CCST.CRACKER_ITF + "/" + CCST.SOLVER_C2);
			if (mv.isValid()) {
				total += (double) mv.getValue();
			}

			mv = this.metricStore.calculate(SolverMetric.DEFAULT_NAME, "/" + CCST.CRACKER_ITF + "/" + CCST.SOLVER_C3);
			if (mv.isValid()) {
				total += (double) mv.getValue();
			}

			return total;
	}

	@Override
	public Double getValue() {
		return this.total;
	}

	@Override
	public void setValue(Double value) {
		this.total = value;
	}

}
