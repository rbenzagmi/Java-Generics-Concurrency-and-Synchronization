package bgu.spl.mics;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */

public class MessageBusImpl implements MessageBus {
	ConcurrentHashMap<MicroService, BlockingQueue<Message>> microsQueue = new ConcurrentHashMap<>();
	ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<MicroService>> microTypes = new ConcurrentHashMap<>();
	@SuppressWarnings({ "rawtypes" })
	ConcurrentHashMap<Event, Future> fut = new ConcurrentHashMap<>();
	
	private static class SingletonHolder {
		private static final MessageBus instance = new MessageBusImpl();
	}
	
	public static MessageBus getInstance() {
		return SingletonHolder.instance;
	}
	
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		subscribeMessage(type, m);
	}
	
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		subscribeMessage(type, m);
	}
	
	public void subscribeMessage(Class<? extends Message> type, MicroService m) {
		microTypes.putIfAbsent(type, new ConcurrentLinkedQueue<>());
		ConcurrentLinkedQueue<MicroService> new_q = microTypes.get(type);
		new_q.add(m);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		Future<T> f = fut.get(e);
		assert f != null;
		f.resolve(result);
	}
	
	@Override
	public void sendBroadcast(Broadcast b) {
		//the microservices that subscribe to this broadcast
		ConcurrentLinkedQueue<MicroService> listeners = microTypes.get(b.getClass());
		for (MicroService m : listeners) {
			BlockingQueue<Message> q = microsQueue.get(m);
			//adding this broadcast to each queue of the microservices that subscribe to this broadcast
			if ( q != null )
				q.add(b);
		}
	}
	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		ConcurrentLinkedQueue<MicroService> listeners;
		MicroService m;
		synchronized (e.getClass()) {
			listeners = microTypes.get(e.getClass());
			if ( listeners == null || listeners.isEmpty() )
				return null;
			//region round robin
			m = listeners.poll(); //this is the microservice that we need to send him the event
			listeners.add(m);
			//endregion
		}
		
		Future<T> future = new Future<>();
		fut.put(e, future);
		BlockingQueue<Message> q;
		synchronized (m) {
			q = microsQueue.get(m);
			if ( q != null )
				q.add(e); //adding the event to the queue of the microservice
		}

		if ( q == null && !future.isDone() )
			future.resolve(null);

		return future;
	}
	
	@Override
	public void register(MicroService m) {
		microsQueue.put(m, new LinkedBlockingDeque<>());
	}
	
	@Override
	public void unregister(MicroService m) {
		synchronized (m) {
			BlockingQueue<Message> todo_list = microsQueue.remove(m);
			//for the general case - complete with null if there is events that didnt complete until the unregistering
			for (Message msg : todo_list) {
				if ( msg instanceof Event )
					complete((Event<?>) msg, null);
			}
		}
		
		for (Map.Entry<Class<? extends Message>, ConcurrentLinkedQueue<MicroService>> entry : this.microTypes.entrySet()) {
			Class<? extends Message> clazz = entry.getKey();
			ConcurrentLinkedQueue<MicroService> listeners = entry.getValue();
			synchronized (clazz) {
				listeners.remove(m);
			}
		}
	}
	
	// micro service calls me to get his next message to handle
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		Message next = null;

		BlockingQueue<Message> todo_list_of_m = microsQueue.get(m);
		if ( todo_list_of_m != null )
			next = todo_list_of_m.take();
		
		return next;
	}
}
