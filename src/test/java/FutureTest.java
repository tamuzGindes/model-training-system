import bgu.spl.mics.Future;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {

    private Future<String> ftr;
    private String res;

    @Before
    public void setUp() throws Exception {
        ftr = new Future<String>();
        res = "";
    }

    @Test
    public void testGet1() {
        String toResolve = "TEST";
        assertFalse(ftr.isDone());
        assertNotEquals(res,toResolve);
        ftr.resolve(toResolve);
        res = ftr.get(1000,TimeUnit.SECONDS);
        assertTrue(ftr.isDone());
        assertEquals(res,toResolve);

    }

    @Test
    public void testGet2() {
        String toResolve = "TEST2";
        assertFalse(ftr.isDone());
        assertNotEquals(res,toResolve);
        ftr.resolve(toResolve);
        res = ftr.get(1000,TimeUnit.SECONDS);
        assertTrue(ftr.isDone());
        assertEquals(res,toResolve);
    }
}