package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import bgu.spl.mics.Future;

/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link DeactivationEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class R2D2Microservice extends MicroService {
    private final long duration;
    private final Diary d = Diary.getInstance();
    private final Future<CountDownLatch> cdl;

    public R2D2Microservice(Future<CountDownLatch> cdl, long duration) {
        super("R2D2");
        this.duration=duration;
        this.cdl = cdl;
    }

    @Override
    protected void initialize() {
        subscribeEvent(DeactivationEvent.class, (DeactivationEvent da) ->
        {
            Thread.sleep(duration);
            complete(da,true);
            d.setR2D2Deactivate(System.currentTimeMillis());
        });

        //each microservice subscribe to this broadcast to terminate itself in the end
        subscribeBroadcast(TerminateBroadcast.class, tick -> {
            terminate();
            d.setR2D2Terminate(System.currentTimeMillis());
        });

        this.cdl.get().countDown();
    }
}
