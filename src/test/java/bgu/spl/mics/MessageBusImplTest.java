package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

//event in type integer that will double the number
class DoubleMe implements Event<Integer> {
    int param;

    public DoubleMe(int param) {
        this.param = param;
    }
}

//even in type integer that will triple the number
class TripleMe implements Event<Integer> {
    int param;

    public TripleMe(int param) {
        this.param = param;
    }
}

//broadcast that will print something
class Echo implements Broadcast {
    String content;
    boolean catched = false;

    public Echo(String content) {
        this.content = content;
    }
}

//microservice that executing the events and the broadcast
class TestMicroService extends MicroService {

    public TestMicroService() {
        super("TestMs");
    }

    @Override
    protected void initialize() {
        //subscribe to the DoubleMe event and executing it
        this.subscribeEvent(DoubleMe.class,(event) -> {
            int parameter = event.param;
            int result = parameter * 2;
            this.complete(event, result);
        });
        //subscribe to the TripleMe event and executing it
        this.subscribeEvent(TripleMe.class, (event) -> {
            int parameter = event.param;
            int result = parameter * 3;
            this.complete(event, result);
        });
        //subscribe to the print Broadcast and executing it
        this.subscribeBroadcast(Echo.class, (b) -> {
            System.out.println(getName() + " got B: " + b);
            b.catched = true;
        });
    }
}

class MessageBusImplTest {

    MessageBus mb;
    Event<Integer> e;
    MicroService m;
    int temp;

    static int get_random() {
        return (int)(10 * Math.random());
    }

    @BeforeEach
    void setUp() {
        mb = new MessageBusImpl();
        e = new DoubleMe(get_random());
        m = new TestMicroService();
        temp = 0;
    }

    @Test
    void register() {
        //now we just checking that this method does not throw exception
        //in sendEvent test we will check more things about it
        assertDoesNotThrow(() -> mb.register(m));
    }

    @Test
    void subscribeEvent() {
        mb.register(m);
        //now we just checking that this method does not throw exception
        //in sendEvent test we will check more things about it
        assertDoesNotThrow(() -> mb.subscribeEvent(DoubleMe.class, m));
    }

    @Test
    void sendEvent() {
        Future<Integer> box0 = mb.sendEvent(new DoubleMe(get_random()));
        assertNull(box0);//we didnt register m to mb
        mb.register(m);
        Future<Integer> box1 = mb.sendEvent(new DoubleMe(get_random()));
        assertNull(box1);//we didnt subscribe m so its still null

        int param = get_random();

        Thread micro_service_thread = new Thread(m);
        micro_service_thread.start();
        Future<Integer> future = mb.sendEvent(new DoubleMe(param));
        m.terminate();
        micro_service_thread.interrupt();

        assertNotNull(future);
        assertTrue(future.isDone());//check complete() method too
        temp = 2 * param - 1;//field
        assertDoesNotThrow(() -> { temp = future.get(); });
        assertEquals(2 * param, temp);
    }

    @Test
    void subscribeBroadcast() {
        mb.register(m);
        //now we just checking that this method does not throw exception
        //in sendEvent test we will check more things about it
        assertDoesNotThrow(() -> mb.subscribeBroadcast(Echo.class, m));
    }

    @Test
    void sendBroadcast() {
        mb.register(m);
        Echo b = new Echo("Message");

        Thread micro_service_thread = new Thread(m);
        micro_service_thread.start();
        mb.sendBroadcast(b);
        m.terminate();
        micro_service_thread.interrupt();

        assertTrue(b.catched);//if catched field is true so the broadcast works
    }

    @Test
    void complete() {
        //already checked in sendEvent method
    }

    @Test
    void unregister() {
        mb.register(m);

        Thread micro_service_thread = new Thread(m);
        micro_service_thread.start();

        mb.unregister(m);

        Echo b = new Echo("Message");
        Future<Integer> future = mb.sendEvent(new TripleMe(1));
        mb.sendBroadcast(b);
        assertNull(future);//the future need to be null after the unregistering
        assertFalse(b.catched);//the catched field need to be false after the unregistering

        m.terminate();
        micro_service_thread.interrupt();
    }

    @Test
    void awaitMessage() {
        //already checked in all the tests that recieved messages
    }
}