package net.nanopool;


public interface Log {
    public void debug(CharSequence mesg, Object... args);
    
    public void debug(Throwable th, CharSequence mesg, Object... args);
    
    public void info(CharSequence mesg, Object... args);
    
    public void info(Throwable th, CharSequence mesg, Object... args);
    
    public void warn(CharSequence mesg, Object... args);
    
    public void warn(Throwable th, CharSequence mesg, Object... args);
    
    public void error(CharSequence mesg, Object... args);
    
    public void error(Throwable th, CharSequence mesg, Object... args);
    
    public Log subLoggerFor(CharSequence objectName);
}
