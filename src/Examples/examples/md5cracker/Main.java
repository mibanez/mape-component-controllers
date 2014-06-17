package examples.md5cracker;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extra.component.mape.analysis.AnalyzerController;
import org.objectweb.proactive.extra.component.mape.execution.ExecutorController;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import examples.md5cracker.actions.AddSolverAction;
import examples.md5cracker.actions.RemoveSolverAction;
import examples.md5cracker.cracker.solver.SolverAttributes;
import examples.md5cracker.metrics.CrackerMetric;
import examples.md5cracker.metrics.SolverMetric;
import examples.md5cracker.plans.QoSPlan;
import examples.md5cracker.rules.MaxPerformanceRule;
import examples.md5cracker.rules.MinPerformanceRule;


public class Main {

	static String DESCRIPTOR_PATH = "file:///user/mibanez/home/Taller/mape-component-controllers/src/Examples/examples/md5cracker/GCMApp.xml";
	static String GCMSCRIPT = "file:///user/mibanez/home/Taller/mape-component-controllers/src/Examples/examples/md5cracker/actions/QoS.fscript";
	private int maxWordLength = 4;

	private static boolean MANAGED = true;
	private static int N_OF_SOLVERS = 1;
	private static int MAX_SOLVERS = 3;

	private static int N_OF_WORKERS = 1;
	private static int MAX_WORKERS = 3;

	private static long DELAY = 120000;
	private static long CHANGE_RULE_TIME = 24;
	private static long MIN_PERFORMANCE = 250;
	private static long MAX_PERFORMANCE = 100;
	
	PAGCMTypeFactory tf;
	PAGenericFactory cf;
	Active active;

	Node N0;
	Node[] nodes;

	public Main() throws InstantiationException, NoSuchInterfaceException, NoSuchAlgorithmException, MalformedURLException, URISyntaxException, ProActiveException {
		Component boot = Utils.getBootstrapComponent();
		tf = Utils.getPAGCMTypeFactory(boot);
		cf = Utils.getPAGenericFactory(boot);

		File appDescriptor = new File((new URL(DESCRIPTOR_PATH)).toURI().getPath());
		
		GCMApplication gcmad = PAGCMDeployment.loadApplicationDescriptor(appDescriptor);
		gcmad.startDeployment();
		gcmad.waitReady();
		
		GCMVirtualNode VN0 = gcmad.getVirtualNode("VN0");
		GCMVirtualNode[] vnodes = new GCMVirtualNode[MAX_SOLVERS];
		for (int i = 0; i < MAX_SOLVERS; i++) {
			vnodes[i] = gcmad.getVirtualNode("VN" + (i+1));
		}
		VN0.waitReady();
	    for (int i = 0; i < MAX_SOLVERS; i++) {
			vnodes[i].waitReady();
	    }  

	    Node N0 = null;
		N0 = VN0.getANode();
		
		
		nodes = new Node[MAX_SOLVERS];
	    for (int i = 0; i < MAX_SOLVERS; i++) {
	    	nodes[i] = null;
	    	nodes[i] = vnodes[i].getANode();
	    	
	    }
	}

	public void run() throws Exception {

		// COMPONENTS CREATION
	    Component cracker = CrackerFactory.createCracker(N0, tf, cf);
	    Component crackerManager = CrackerFactory.createCrackerManager(N0, tf, cf);
	    Component[] solvers = createSolvers(nodes);
		Component[] solverManagers = createSolverManagers(nodes);
		Component[][] workers = createWorkers(nodes);
		
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			CrackerFactory.bindSolver(solvers[i], solverManagers[i], workers[i]);
		}
		CrackerFactory.bindCracker(cracker, crackerManager, solvers);
	
		Utils.getPAGCMLifeCycleController(cracker).startFc();

		
		// CONFIGURE ATTRIBUTES
		//((CrackerAttributes) GCM.getAttributeController(crackerManager)).setNumberOfSolvers(N_OF_SOLVERS);
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			SolverAttributes solverAttributes = (SolverAttributes) GCM.getAttributeController(solverManagers[i]);
			solverAttributes.setNumberOfWorkers(N_OF_WORKERS);
			solverAttributes.setId(i + 1); // [!] ---- represent the number of the virtual node, "VNi", for value i.
		}
		

		// CONFIGURE CONTORLLERS
		Remmos.enableMonitoring(cracker); // [!] too much important, maybe merge with MonitoController.startMontioring?
		MonitorController crackerMonitor = (MonitorController) cracker.getFcInterface(Constants.MONITOR_CONTROLLER);
		crackerMonitor.startGCMMonitoring();
		Thread.sleep(2000);

		// Metrics
		crackerMonitor.addMetric(CrackerMetric.DEFAULT_NAME, new CrackerMetric());
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			Remmos.getMonitorController(solvers[i]).addMetric(SolverMetric.DEFAULT_NAME, new SolverMetric());
		}

		// Rules
		AnalyzerController analyzer = Remmos.getAnalyzerController(cracker);
		analyzer.addRule(MinPerformanceRule.DEFAULT_NAME, new MinPerformanceRule(MIN_PERFORMANCE));
	
		// Plans
		Remmos.getPlannerController(cracker).setPlan(new QoSPlan(MAX_WORKERS, MAX_SOLVERS, DELAY));

		// Actions
		ExecutorController crackerExecutor = Remmos.getExecutorController(cracker);
		crackerExecutor.load((new URL(GCMSCRIPT)).toURI().getPath());
		crackerExecutor.addAction(AddSolverAction.DEFAULT_NAME, new AddSolverAction(nodes));
		crackerExecutor.addAction(RemoveSolverAction.DEFAULT_NAME, new RemoveSolverAction());
	
		// RUN
		printIntro();		
		(new Thread(new Client(cracker, maxWordLength, "client1"))).start();
		(new Thread(new Client(cracker, maxWordLength, "client2"))).start();
		(new Thread(new Client(cracker, maxWordLength, "client3"))).start();

		//System.out.println("----> " + 
		//Remmos.getExecutorController(cracker).execute("remove-worker($this);") );
		

		// MAPE RUN 
		
		long startTime = System.currentTimeMillis();
		while (true) {
			
			String msg = "" + ((Double) crackerMonitor.calculateMetric(CrackerMetric.DEFAULT_NAME).getValue()).doubleValue();
			double time = (System.currentTimeMillis() - startTime)/60000.0;
			System.out.println(time + "\t" + msg);
			Thread.sleep(4000);
			
			if (time > 24) {
				analyzer.removeRule(MinPerformanceRule.DEFAULT_NAME);
				analyzer.addRule(MaxPerformanceRule.DEFAULT_NAME, new MaxPerformanceRule(MAX_PERFORMANCE));
			}
		}
	}


	private Component[] createSolvers(Node[] nodes) throws Exception {
		Component[] result = new Component[N_OF_SOLVERS];
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			result[i] = CrackerFactory.createSolver(nodes[i], tf, cf);
		}
		return result;
	}

	private Component[] createSolverManagers(Node[] nodes) throws Exception {
		Component[] result = new Component[N_OF_SOLVERS];
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			result[i] = CrackerFactory.createSolverManager(nodes[i], tf, cf);
		}
		return result;
	}

	private Component[][] createWorkers(Node[] nodes) throws Exception {
		Component[][] result = new Component[N_OF_SOLVERS][N_OF_WORKERS];
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			for (int j = 0; j < N_OF_WORKERS; j++) {
				result[i][j] = CrackerFactory.createWorker(nodes[i], tf, cf);
			}
		}
		return result;
	}



	private void printIntro() {
		System.out.println("[MD5Cracker]");
		System.out.println("* * * * * * * * * * * MD5Cracker * * * * * * * * * * * * * *");
		System.out.println("DELAY = " + DELAY);
		System.out.println("CHANGE_RULE_TIME = " + CHANGE_RULE_TIME);
		System.out.println("MIN_PERFORMANCE = " + MIN_PERFORMANCE);
		System.out.println("MAX_PERFORMANCE = " + MAX_PERFORMANCE);
		System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
	}

	public static void main(String[] args) throws Exception {
		
    	if(args.length != 0) {
    		DELAY = Long.parseLong(args[0]);
    		CHANGE_RULE_TIME = Long.parseLong(args[1]);
    		MIN_PERFORMANCE = Integer.parseInt(args[2]);
    		MAX_PERFORMANCE = Integer.parseInt(args[3]);
    	}

    	(new Main()).run();
	}
}
