package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.AbstractAxis;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;
import org.objectweb.proactive.extra.component.fscript.model.GCMInterfaceNode;

/**
 * Implements the <code>rule</code> axis in FPath. This axis connects a component to its metrics (if any),
 * as defined in AnalyzerController. This axis is not modifiable.
 * 
 */
public class RuleAxis extends AbstractAxis {

	public RuleAxis(FractalModel model) {
        super(model, "rule", "component", "rule");
	}

	@Override
	public boolean isModifiable() {
		return false;
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	/**
     * Locates all the destination nodes the given source node is connected to through
     * this axis.
     * 
     * @param source
     *            the source node from which to select adjacent nodes.
     * @return all the destination nodes the given source node is connected to through
     *         this axis.
     */
    @Override
    public Set<Node> selectFrom(Node source) {
        Component comp = null;
        if (source instanceof GCMComponentNode) {
            comp = ((GCMComponentNode) source).getComponent();
        } else if (source instanceof GCMInterfaceNode) {
            comp = ((GCMInterfaceNode) source).getInterface().getFcItfOwner();
        } else {
        	throw new IllegalArgumentException("Invalid source node kind " + source.getKind());
        }

        Set<Node> result = new HashSet<Node>();
        try {
        	AnalyzerController analyzerController = Remmos.getAnalyzerController(comp);
        	Wrapper<ArrayList<String>> ruleNames = analyzerController.getRuleNames();
        	if (ruleNames.isValid()) {
				for (String ruleName : ruleNames.getValue()) {
					Node node = new RuleNode((FractalModel) model, analyzerController, ruleName);
					result.add(node);
				}
        	} else {
        		// warn making some noise
        		(new Exception("AnalyzerController detected, but failed to get the rule names.")).printStackTrace();
        	}
		} catch (NoSuchInterfaceException e) {
			// continue silently
		}

        return result;
    }

}
