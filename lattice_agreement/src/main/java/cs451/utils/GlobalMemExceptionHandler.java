package cs451.utils;

import java.io.IOException;

public class GlobalMemExceptionHandler implements Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        int count = 10;
        while(count-- > 0){
            try {
                System.gc();
                Runtime.getRuntime().exec("kill -SIGINT " +
                        ProcessHandle.current().pid());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (OutOfMemoryError err){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        try {
            Runtime.getRuntime().exec("kill -SIGKILL " +
                    ProcessHandle.current().pid());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (OutOfMemoryError err){
            Runtime.getRuntime().halt(1);
        }

    }
}
