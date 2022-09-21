package org.pgstyle.talesclicker.module.notifier;

public final class NullDetector implements Detector {

    @Override
    public boolean detect() {
        // NOP
        return false;
    }

    @Override
    public String message() {
        return "null detected";
    }
    
}
