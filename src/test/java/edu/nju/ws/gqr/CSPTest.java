package edu.nju.ws.gqr;

import org.junit.Before;
import org.junit.Test;

public class CSPTest {
    private CSP<Relation, CalculusOperations<Relation>> csp;

    @Before
    public void setUp() throws Exception {
        String calculusName = "rcc8";
        String dir = "resource/gqr/data";
        String config = "resource/gqr/data/rcc8.spec";
        String cspFile = "resource/gqr/data/rcc8/csp/PC.csp";
        CalculusReader reader = new CalculusReader(calculusName, dir, config);
        Calculus calculus = reader.makeCalculus();
        CSPReader cspReader = new CSPReader(calculus, cspFile);
        CSPSparse sparse = cspReader.makeCSP();
        CalculusOperations<Relation> calculusOperation = new CalculusOperations<>(calculus, Relation.class);
        csp = new CSP<>(sparse, calculusOperation);
    }

    @Test
    public void setConstraint() {
    }

    @Test
    public void getConstraint() {
        Relation r = csp.getConstraint(0, 3);
        System.out.println(r.toString());
    }

    @Test
    public void size() {
    }

    @Test
    public void equals() {
    }

    @Test
    public void getCalculus() {
    }

    @Test
    public void getName() {
    }
}