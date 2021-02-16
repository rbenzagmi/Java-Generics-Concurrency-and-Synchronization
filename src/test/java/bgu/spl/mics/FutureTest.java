package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class FutureTest {

    private Future<String> future;

    @BeforeEach
    public void setUp() {
        future = new Future<>();
    }

    @Test
    public void testResolve() throws InterruptedException {
        String str = "someResult";
        future.resolve(str);
        assertTrue(future.isDone());
        assertTrue(str.equals(future.get()));
    }

    @Test
    void get() throws InterruptedException {
        String res = "myResult" + Math.random();
        future.resolve(res);
        //checking if the get method return our res
        assertEquals(res, future.get());
    }

    @Test
    void isDone(){
        assertDoesNotThrow(() -> assertFalse(future.isDone()));
        String res = "myResult" + Math.random();
        future.resolve(res);
        assertDoesNotThrow(() -> assertTrue(future.isDone()));
        //checking if the isDone method return true after the resolve
        assertTrue(future.isDone());
    }

    @Test
    void testGet() {
        long t0 = System.currentTimeMillis(); //the current time
        String result = future.get(1, TimeUnit.SECONDS);
        long t1 = System.currentTimeMillis(); //the time after the get method
        assertTrue(t1-t0>=1000); //checking that really passed more than a sec
        assertNull(result);
        String res = "myResult" + Math.random();
        assertFalse(future.isDone());
        future.resolve(res);
        //now after the resolve we need to get our res
        assertEquals(res, future.get(1, TimeUnit.SECONDS));
    }
}