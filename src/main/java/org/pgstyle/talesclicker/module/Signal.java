package org.pgstyle.talesclicker.module;

public enum Signal {
    // normal
    SUCCESS          (0b00000000),
    // module termination
    TERMINATE        (0b10000000),
    STOP             (0b10000001),
    INTERRUPTED      (0b10000010),
    KILL             (0b10000011),
    // module error
    FATAL_MAIN       (0b10000100),
    FATAL_CONTROL    (0b10000101),
    FAILED_INITIALISE(0b10001001),
    FAILED_RELOAD    (0b10001010),
    FAILED_FINALISE  (0b10001011)
    ;

    private Signal(int code) {
        this.code = code;
    }

    private final int code;

    public int getCode() {
        return this.code;
    }

    public String toString() {
        return this.code + "-" + this.name();
    }


}
