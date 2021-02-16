package bgu.spl.mics.application.passiveObjects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EwokTest {

    private Ewok ewok;

    @BeforeEach
    void setUp() {
        ewok = new Ewok(1,true);
    }

    @Test
    void acquire() throws InterruptedException {
        ewok.acquire();
        assertFalse(ewok.available); //after - the available need to be false
    }

    @Test
    void release() throws InterruptedException {
        ewok.release();
        assertTrue(ewok.available); //after - the available need to be true
        ewok.acquire(); //we call acquire and then check again
        ewok.release();
        assertTrue(ewok.available); //after - the available need to be true
    }
}