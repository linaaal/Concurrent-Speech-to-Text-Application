package comp3011.assignment1.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class GracefulShutdownService {
	
	private final ConfigurableApplicationContext applicationContext;
	
	// safely records if shutdown has already been requested
	private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
	
	
	public GracefulShutdownService(
			ConfigurableApplicationContext applicationContext) {
		
		this.applicationContext = applicationContext;
	}
	
	public boolean requestShutdown() {
		
		// only the first shutdown request can change false to true
		if (!shutdownRequested.compareAndSet(false, true) ) {
			return false;
		}
		
		try {
		/*
		 * platform thread is used so shutdown can still run if the
		 * virtual-thread schedular is busy or restricted to one carrier.
		 */
		
			Thread.ofPlatform()
					.name("graceful-shutdown")
					.start(this::closeApplication);
			
			return true;
			
		} catch (RuntimeException exception) {
			shutdownRequested.set(false);
			throw exception;
		}
	}
	
	public void closeApplication() {
	
		try {
			// gives the controller enough time to send its HTTP response
			Thread.sleep(250);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }

	
		applicationContext.close();
	}
}

