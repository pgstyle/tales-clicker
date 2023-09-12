package org.pgstyle.autoutils.talesclicker.module;

/**
 * Application signals.
 */
public enum Signal {
    // normal
    /** General:NoError (0) */
    SUCCESS          (0b00000000),
    // module termination
    /** Module:TerminateNoError (128) */
    TERMINATE        (0b10000000),
    /** Module:TerminateError (129) */
    STOP             (0b10000001),
    /** Module:TerminateInterrupted (130) */
    INTERRUPTED      (0b10000010),
    /** Module:TerminateForced (131) */
    KILL             (0b10000011),
    // module error
    /** Module:FatalErrorGeneral (132) */
    FATAL_MAIN       (0b10000100),
    /** Module:FatalErrorModuleControl (133) */
    FATAL_CONTROL    (0b10000101),
    /** Module:ErrorInitialise (137) */
    FAILED_INITIALISE(0b10001001),
    /** Module:ErrorReinitialise (138) */
    FAILED_RELOAD    (0b10001010),
    /** Module:ErrorFinalise (139) */
    FAILED_FINALISE  (0b10001011)
    ;

    private Signal(int code) {
        this.code = code;
    }

    private final int code;

    /**
     * Get the underlying status code.
     *
     * @return the status code
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Return {@code this.code + "-" + this.name()}.
     * 
     * @return the signal code and signal name
     */
    @Override
    public String toString() {
        return this.code + "-" + this.name();
    }


}
