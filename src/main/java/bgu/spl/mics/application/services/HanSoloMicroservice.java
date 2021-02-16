package bgu.spl.mics.application.services;


import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import bgu.spl.mics.Future;

/**
 * HanSoloMicroservices is in charge of the handling {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class HanSoloMicroservice extends MicroService {
	private final Ewoks ewoks = Ewoks.getInstance();
	private final Diary d = Diary.getInstance();
	private Future<CountDownLatch> cdl;

	public HanSoloMicroservice() {
		super("Han");
	}

	public HanSoloMicroservice(Future<CountDownLatch> cdl) {
		super("Han");
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
			complete(c,true);
			d.setHanSoloFinish(System.currentTimeMillis());
		});

		//each microservice subscribe to this broadcast to terminate itself in the end
		subscribeBroadcast(TerminateBroadcast.class, tick -> {
			terminate();
			d.setHanSoloTerminate(System.currentTimeMillis());
		});

		this.cdl.get().countDown();
	}
}
