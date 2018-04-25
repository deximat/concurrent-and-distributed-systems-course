package risks;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SolveRisks {
	
	public static void main(String[] args) throws InterruptedException {
		
		// start with a pool with a min/max size of 3.
		ThreadPoolExecutor executor = new ThreadPoolExecutor(3,3,60, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

		for (Thread thread : Thread.getAllStackTraces().keySet()) {
			System.out.println(thread);
		}
		
		final int size = 1000;
		for (int i = 0; i < size; i++) {
			final int j = i;
			if (i > (size / 3)) {
				// set the min/max size to 4.
				executor.setCorePoolSize(4);
				System.out.println("Change size to 4.");
			} else if (i > 2* (size/3)) {
				// set the min/max size to 4.
				executor.setCorePoolSize(1);
				System.out.println("Change size to 1.");
			}
			
			executor.execute(() -> {
				System.out.println(Thread.currentThread().getName() + " \t i: " + j);
			});
			
			// Thread.sleep(100);
		}
		
		for (Thread thread : Thread.getAllStackTraces().keySet()) {
			System.out.println(thread);
		}
		
		
	}

}
