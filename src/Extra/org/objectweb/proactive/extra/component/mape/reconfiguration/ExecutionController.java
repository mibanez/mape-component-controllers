package org.objectweb.proactive.extra.component.mape.reconfiguration;

import java.util.Set;

import org.objectweb.proactive.extra.component.fscript.control.PAReconfigurationController;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;

public interface ExecutionController {
    /** Controller name. */
    public static final String RECONFIGURATION_CONTROLLER = "reconfiguration-controller";

    /**
     * Instantiates a new GCMScript engine from the default GCMScript ADL file and sets it as
     * default engine for the controller.
     *
     * @throws ReconfigurationException If an error occurred during the instantiation.
     */
    public void setNewEngineFromADL() throws ReconfigurationException;

    /**
     * Instantiates a new GCMScript engine from an ADL file and sets it as default engine for the
     * controller.
     *
     * @param adlFile The ADL file name containing the GCMScript architecture to instantiate and to set
     * as default engine for the controller.
     * @throws ReconfigurationException If an error occurred during the instantiation.
     */
    public void setNewEngineFromADL(String adlFile) throws ReconfigurationException;

    /**
     * Loads procedure definitions from a file containing source code, and makes them available for later invocation
     * by name.
     *
     * @param fileName The name of the file containing the source code of the procedure definitions.
     * @return The names of all the procedures successfully loaded.
     * @throws ReconfigurationException If errors were detected in the procedure definitions.
     */
    Set<String> load(String fileName) throws ReconfigurationException;

    /**
     * Returns the names of all the currently defined global variables.
     *
     * @return The names of all the currently defined global variables.
     * @throws ReconfigurationException If an error occurred while getting global variable names.
     */
    Set<String> getGlobals() throws ReconfigurationException;

    /**
     * Executes a code fragment: either an FPath expression or a single FScript statement.
     *
     * @param source The code fragment to execute.
     * @return The value of the code fragment, if successfully executed.
     * @throws ReconfigurationException If an error occurred during the execution of the code fragment.
     */
    Object execute(String source);
   
	// DEFAULT NAME
	public static final String ITF_NAME = "execution-controller-nf";

	/**
	 * Adds a new Action. This Action will be stored and can be executed by {@link #executeAction(String)}
	 * using its name.
	 * @param name unique name to identify this action.
	 * @param action the action to be stored
	 * @return true if success
	 */
	public boolean addAction(String name, Action action);
	
	/**
	 * Adds a new Action. This Action will be stored and can be executed by {@link #executeAction(String)}
	 * using its name.
	 * @param name unique name to identify this action.
	 * @param script a script in GCMScript language.
	 * @return true if success
	 */
	public boolean addActionScript(String name, String script);


	/**
	 * Remove the action identified by the given name.
	 * @param name the name of the action to be removed
	 */
	public void removeAction(String name);

	/**
	 * Executes the stored action identified by the given name
	 * @param actionName name of the stored Action to be executed
	 */
	public Object executeAction(String actionName);
	
	/**
	 * Executes the given Action
	 * @param action Action to be executed
	 */
	public Object executeAction(Action action);

}
