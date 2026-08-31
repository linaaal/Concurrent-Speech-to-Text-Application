package comp3011.assignment1.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class GracefulShutdownService {
	
	private final ConfigurableApplicationContext applicationContext;
	
	public GracefulShutdownService(
			ConfigurableApplication applicationContext) {
		
		this.applicationContext = applicationContext;
	}
	
	public boolean requestShutdown() {
		
		/*
		 * platform thread is used so shutdown can still run if the
		 * virtual-thread schedular is busy or restricted to one carrier.
		 */
		
		Thread.ofPlatform()
				.name("graceful-shutdown")
				.start(this::closeApplication);
		
		return true;
	}
	
	public void closeApplication()
	
		try {
			// gives the controller enough time to send its HTTP response
			Thread.sleep(250);
		} catch (InterruptedExcpection exception) {
			Thread.currentThread().interrupt();
		}
	
		applicationContext.close();
}

