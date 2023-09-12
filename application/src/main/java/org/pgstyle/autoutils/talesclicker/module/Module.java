package org.pgstyle.autoutils.talesclicker.module;

/**
 * Class implementing the {@code Module} interface will be handled by the
 * {@link ModuleManager}.
 *
 * @since 1.0
 * @author PGKan
 */
public interface Module {

    /**
     * Calculate millisecond timeout from a frequency per minute.
     *
     * @param frequency count per minute (RPM)
     * @return millisecond timeout
     */
    public static long calculateTimeout(double frequency) {
        return (long) (60000 / frequency);
    }

    /**
     * Initialise this module, this method will be invoked after the creation of
     * this module but before first invocation of the method {@code execute()}.
     * Or being re-invoke if a reload signal is issued by the method
     * {@code execute()}.
     *
     * @param env the shared environment of the application
     * @param args the argument of module initialisation
     * @return {@code true} if the initialisation is finished successfully; or
     *         {@code false} otherwise
     */
    boolean initialise(Environment env, String[] args);

    /**
     * Execute this module, this method will be invoked repeatedly with
     * the frequency controlled by the returned {@code ModuleControl}; until a
     * termination signal is issued by the returned {@code ModuleControl}, or
     * from other module via the {@link ModuleManager.ManagerAPI}.
     *
     * @return the module control object
     */
    ModuleControl execute();

    /**
     * Finalise this module, this method will be invoked after the module runner
     * received a termination signal. Or being re-invoke if the first invocation
     * is failed.
     *
     * @param control the module control object
     * @return {@code true} if the initialisation is finished successfully; or
     *         {@code false} otherwise
     */
    boolean finalise(ModuleControl control);

}
