package tests;

import static org.junit.Assert.*;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.extensions.autonomic.adl.AFactory;
import org.objectweb.proactive.extensions.autonomic.adl.AFactoryFactory;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ObjectWrapper;

import tests.components.Slave;

public class TestAGCMNewAction extends CommonSetup {

	AFactory adlFactory;
	Component composite;
	
    @Before
    public void setUp() throws Exception {
   		super.setUp();
   		adlFactory = (AFactory) AFactoryFactory.getAFactory();
   		composite = (Component) adlFactory.newAutonomicComponent("tests.components.Composite", null);
   		Remmos.enableMonitoring(composite);
    }
    
    @Test
    public void TestAComponentInstantiaton() {
    	ExecutorController executor;
		try {
			executor = Remmos.getExecutorController(composite);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		String appDescriptor = this.getClass().getResource("GCMALocal.xml").getPath();
    	System.out.println("-- " + executor.execute("gcma = deploy-gcma(\"" + appDescriptor + "\");").getObjectOrNull());
    	System.out.println("-- " + executor.execute("slave = gcm-new-autonomic(\"tests.components.Slave\", $gcma);").getObjectOrNull());
    	System.out.println("-- " + executor.execute("set-name($slave, \"Slave2\");").getObjectOrNull());
    	System.out.println("-- " + executor.execute("stop($this);").getObjectOrNull());
    	System.out.println("-- " + executor.execute("add($this, $slave);").getObjectOrNull());
    	System.out.println("-- " + executor.execute("start($this);").getObjectOrNull());

    	System.out.println("-- " + executor.execute("$this/child::*;").getObjectOrNull());
	

    	
    	Component slave = null;
    	try {
			for (Component subComp : Utils.getPAContentController(composite).getFcSubComponents()) {
				String name = GCM.getNameController(subComp).getFcName();
				System.out.println("subcomp: " + name);
				if (name.equals("Slave2")) {
					slave = subComp;
					break;
				}
			}
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			
			return;
		}
    	
    	assert (slave != null);
    	
    	try {
    		Slave itf = (Slave) slave.getFcInterface("slave");
    		itf.run2();
			System.out.println(Remmos.getExecutorController(slave).execute("true();").getObjectOrNull());
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
}