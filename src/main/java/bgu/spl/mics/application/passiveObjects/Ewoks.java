package bgu.spl.mics.application.passiveObjects;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive object representing the resource manager.
 * <p>
 * This class must be implemented as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class Ewoks {
	private final ConcurrentHashMap<Integer, Ewok> ewoks = new ConcurrentHashMap<>();
	
	private static class SingletonHolder {
		private static final Ewoks instance = new Ewoks();
	}
	
	public static Ewoks getInstance() {
		return SingletonHolder.instance;
	}
	
	public void acquireEwoks(List<Integer> requiredEwoks) {
		requiredEwoks.sort(Integer::compareTo);
		try {
			for (int i : requiredEwoks) {
				Ewok ewok = ewoks.get(i);
				ewok.acquire();
			}
		}
		catch (InterruptedException ignored) {}
	}
	
	public void releaseEwoks(List<Integer> requiredEwoks) {
		for (Integer i : requiredEwoks) {
			//we put in the hashmap this ewok as not available ewok
			ewoks.putIfAbsent(i, new Ewok(i, false));
			//release all the requiredEwoks
			ewoks.get(i).release();
		}
	}
	
}
