package com.doxla.testing.transaction;

import com.atomikos.icatch.jta.UserTransactionManager;

public class AtomikosTransactionManagerTestUtil {

    private final UserTransactionManager userTransactionManager;
    private Thread shutdownHook;

    public AtomikosTransactionManagerTestUtil(UserTransactionManager userTransactionManager) {
        this.userTransactionManager = userTransactionManager;
    }

    public void forceTransactionManagerShutdown() {
        userTransactionManager.setForceShutdown(true);
        userTransactionManager.close();
    }

    public void registerShutdownHook(){
        if(shutdownHook == null){
            this.shutdownHook = new Thread() {
                @Override
                public void run() {
                    System.out.println("\n\n\n\n\nShutting down TM\n\n\n\n\n\n\n\n");
                    forceTransactionManagerShutdown();
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }
}
