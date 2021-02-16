package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.Future;

/**
 * LandoMicroservice
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LandoMicroservice extends MicroService {
	private final long duration;
	private final Diary d = Diary.getInstance();
	private final Future<CountDownLatch> cdl;
	
	public LandoMicroservice(Future<CountDownLatch> cdl, long duration) {
		super("Lando");
		this.duration = duration;
		this.cdl = cdl;
	}
	
	@Override
	protected void initialize() {
		subscribeEvent(BombDestroyerEvent.class, (BombDestroyerEvent bomb) ->
		{
			Thread.sleep(duration);
			complete(bomb, true);
		});

		//each microservice subscribe to this broadcast to terminate itself in the end
		subscribeBroadcast(TerminateBroadcast.class, tick -> {
			terminate();
			d.setLandoTerminate(System.currentTimeMillis());
		});

		this.cdl.get().countDown();
	}
}
