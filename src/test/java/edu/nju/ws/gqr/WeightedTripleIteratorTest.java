package edu.nju.ws.gqr;

import org.junit.Before;
import org.junit.Test;

public class WeightedTripleIteratorTest {

    private WeightedTripleIterator<Relation, CSP<Relation, CalculusOperations<Relation>>> propagation;
    private CSP<Relation, CalculusOperations<Relation>> csp;

    @Before
    public void setUp() throws Exception {
        String calculusName = "rcc8";
        String dir = "resource/gqr/data";
        String config = "resource/gqr/data/rcc8.spec";
//        String cspFile = "resource/gqr/data/rcc8/csp/PC.csp";
        String cspFile = "resource/gqr/data/rcc8/csp/alleq.csp";
        CalculusReader reader = new CalculusReader(calculusName, dir, config);
        Calculus calculus = reader.makeCalculus();
        CSPReader cspReader = new CSPReader(calculus, cspFile);
        CSPSparse sparse = cspReader.makeCSP();
        CalculusOperations<Relation> calculusOperation = new CalculusOperations<>(calculus, Relation.class);
        csp = new CSP<>(sparse, calculusOperation);
        propagation = new WeightedTripleIterator<>();
    }

    @Test
    public void enforce() {
        System.out.println(propagation.enforce(csp));
    }

    @Test
    public void enforce1() {
    }
}