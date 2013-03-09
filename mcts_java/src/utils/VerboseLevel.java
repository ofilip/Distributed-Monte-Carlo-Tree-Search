package utils;

public enum VerboseLevel {
    QUIET(0),
    VERBOSE(1),
    DEBUGGING(2);
    
    public final int level;
    public boolean check(VerboseLevel v) { return this.level>=v.level; }
    VerboseLevel(int level) { this.level = level; }
}
