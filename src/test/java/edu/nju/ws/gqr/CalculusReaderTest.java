package edu.nju.ws.gqr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CalculusReaderTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void makeCalculus() {
        String calculusName = "rcc8";
        String dir = "resource/gqr/data";
        String config = "resource/gqr/data/rcc8.spec";
        CalculusReader reader = new CalculusReader(calculusName, dir, config);
        reader.makeCalculus();
    }
}