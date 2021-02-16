package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.Future;


/**
 * C3POMicroservices is in charge of the handling {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends MicroService {
	private final Ewoks ewoks = Ewoks.getInstance();
	private final Diary d = Diary.getInstance();
	private Future<CountDownLatch> cdl;
	
	public C3POMicroservice() {
		super("C3PO");
	}
	
	public C3POMicroservice(Future<CountDownLatch> cdl, int ewoks_amount) {
		super("C3PO");
		List<Integer> lst = new LinkedList<>();
		//create the list of the ewoks that we need
		for (int i = 0; i < ewoks_amount; i++)
			lst.add(i + 1);
		ewoks.releaseEwoks(lst);
		this.cdl = cdl;
	}
	
	@Override
	protected void initialize() {
		subscribeEvent(AttackEvent.class, (AttackEvent c) ->
		{
			List<Integer> requiredEwoks = c.getSerials();
			ewoks.acquireEwoks(requiredEwoks); //this microservice acquire this ewoks
			int duration = c.getDuration();
			Thread.sleep(duration); //the action is to sleep
			d.settotalAttacks(); //here we add one to the totalAttacks
			ewoks.releaseEwoks(requiredEwoks); //we finish with the ewoks and we can release them
			complete(c, true);
			d.setC3POFinish(System.currentTimeMillis());
		});

		//each microservice subscribe to this broadcast to terminate itself in the end
		subscribeBroadcast(TerminateBroadcast.class, tick -> {
			terminate();
			d.setC3POTerminate(System.currentTimeMillis());
		});
		
		this.cdl.get().countDown();
	}
	
	
}
