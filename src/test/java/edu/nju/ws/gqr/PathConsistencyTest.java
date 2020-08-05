package edu.nju.ws.gqr;

import org.junit.Test;

public class PathConsistencyTest {


    @Test
    public void run() {
        PathConsistency pathConsistency = new PathConsistency("rcc8", false, false, true);
        pathConsistency.run("resource/gqr/data/rcc8/csp/example-20x20.csp");
    }

    @Test
    public void run1() {
    }
}