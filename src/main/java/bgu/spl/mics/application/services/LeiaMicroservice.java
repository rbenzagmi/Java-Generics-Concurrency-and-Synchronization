package bgu.spl.mics.application.services;

import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Attack;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {
	private final Attack[] attacks;
	private final LinkedList<Future<Boolean>> futurQ = new LinkedList<>();
	private final Diary d = Diary.getInstance();
	private final Future<CountDownLatch> cdl;
	
	public LeiaMicroservice(Future<CountDownLatch> cdl, Attack[] attacks) {
		super("Leia");
		this.attacks = attacks;
		this.cdl = cdl;
	}
	
	@Override
	protected void initialize() {
		//Leia is waiting for all the other microservices to initialize
		try {
			this.cdl.get().await();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		//each microservice subscribe to this broadcast to terminate itself in the end
		subscribeBroadcast(TerminateBroadcast.class, tick -> {
			terminate();
			d.setLeiaTerminate(System.currentTimeMillis());
		});

		//Leia send all the events
		for (Attack attack : attacks) {
			futurQ.add(sendEvent(new AttackEvent(attack.getSerials(),
					attack.getDuration())));
		}

		//wait here until all the attack events resolve
		while ( !futurQ.isEmpty() ) {
			Future<Boolean> f = futurQ.remove();
			f.get();
		}

		//now R2D2 can do the deactivation event
		Future<Boolean> ftD = sendEvent(new DeactivationEvent());
		//we will wait here until he finish
		ftD.get();

		//now Lando can do the bomb destroyer event
		Future<Boolean> ftBomb = sendEvent(new BombDestroyerEvent());
		//we will wait here until he finish
		ftBomb.get();

		//now we can terminate all the microservices
		sendBroadcast(new TerminateBroadcast());
	}
}
