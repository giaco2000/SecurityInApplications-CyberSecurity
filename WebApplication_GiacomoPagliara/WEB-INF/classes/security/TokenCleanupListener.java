package security;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Listener per eseguire pulizia periodica dei token scaduti.
 */
@WebListener
public class TokenCleanupListener implements ServletContextListener {
    
    private ScheduledExecutorService scheduler;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Pianifica la pulizia dei token scaduti ogni 24 ore
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                TokenManager.cleanExpiredTokens();
            }
        }, 0, 24, TimeUnit.HOURS);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}