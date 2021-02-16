package bgu.spl.mics.application.passiveObjects;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Passive data-object representing a Diary - in which the flow of the battle is recorded.
 * We are going to compare your recordings with the expected recordings, and make sure that your output makes sense.
 * <p>
 * Do not add to this class nothing but a single constructor, getters and setters.
 */
public class Diary {
	public AtomicInteger totalAttacks = new AtomicInteger(0); //the total number of attacks executed by HanSolo and C3PO.
	public long HanSoloFinish = 0; //a timestamp indicating when HanSolo finished the execution of all his attacks.
	public long C3POFinish = 0; //a timestamp indicating when C3PO finished the execution of all his attacks.
	public long R2D2Deactivate = 0; //a timestamp indicating when R2D2 finished deactivation the shield generator.
	public long LeiaTerminate = 0; //a time stamp that Leia puts in right before termination.
	public long HanSoloTerminate = 0; //a time stamp that HanSolo puts in right before termination.
	public long C3POTerminate = 0; //a time stamp that C3PO puts in right before termination.
	public long R2D2Terminate = 0; //a time stamp that R2d2 puts in right before termination.
	public long LandoTerminate = 0; //a time stamp that Lando puts in right before termination.
	private static long start = System.currentTimeMillis();

	private static class SingletonHolder {
		private static Diary instance = new Diary();
	}
	
	public static Diary getInstance() {
		return Diary.SingletonHolder.instance;
	}
	
	public Diary() {}

	//when we call this function we need to add 1 attack to the totalAttacks
	public void settotalAttacks() {this.totalAttacks.getAndIncrement(); }
	
	public void setHanSoloFinish(long HanSoloFinish) { this.HanSoloFinish = HanSoloFinish - start; }
	
	public void setC3POFinish(long C3POFinish) { this.C3POFinish = C3POFinish - start; }
	
	public void setR2D2Deactivate(long R2D2Deactivate) { this.R2D2Deactivate = R2D2Deactivate - start; }
	
	public void setLeiaTerminate(long LeiaTerminate) { this.LeiaTerminate = LeiaTerminate - start; }
	
	public void setHanSoloTerminate(long HanSoloTerminate) {this.HanSoloTerminate = HanSoloTerminate - start; }
	
	public void setC3POTerminate(long C3POTerminate) { this.C3POTerminate = C3POTerminate - start; }
	
	public void setR2D2Terminate(long R2D2Terminate) { this.R2D2Terminate = R2D2Terminate - start;}
	
	public void setLandoTerminate(long LandoTerminate) {
		this.LandoTerminate = LandoTerminate - start;
	}
}
