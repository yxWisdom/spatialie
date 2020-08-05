package edu.nju.ws.gqr;

import org.junit.Test;

public class CSPReaderTest {

    @Test
    public void makeCSP() {
        String calculusName = "rcc8";
        String dir = "resource/gqr/data";
        String config = "resource/gqr/data/rcc8.spec";
        String cspFile = "resource/gqr/data/rcc8/csp/PC.csp";
        CalculusReader reader = new CalculusReader(calculusName, dir, config);
        Calculus calculus = reader.makeCalculus();
        CSPReader cspReader = new CSPReader(calculus, cspFile);
        cspReader.makeCSP();
    }
}