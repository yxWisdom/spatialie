package edu.nju.ws.gqr;

import edu.nju.ws.spatialie.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class PathConsistency {

    private final String dataDir = "resource/gqr/data";
    private Calculus calculus = null;

    private boolean negativeOnly = false;
    private boolean positiveOnly = false;
    private boolean showSolution = false;
//    private boolean returnState=false;

    public PathConsistency(String calculusName, boolean negativeOnly, boolean positiveOnly,
                           boolean showSolution) {

        this.negativeOnly = negativeOnly;
        this.positiveOnly = positiveOnly;
        this.showSolution = showSolution;
//        this.returnState = returnState;
        calculus = readCalculus(calculusName);
    }

    private Calculus readCalculus(String calculusName) {
        String path = dataDir + "/" + calculusName + ".spec";
        if (!FileUtil.exists(path)) {
            System.err.printf("Undefined calculus: \"%s\" \n", calculusName);
            return null;
        }
        CalculusReader cr = new CalculusReader(calculusName, dataDir, path);
        return cr.makeCalculus();
    }

    public boolean run(String inPath) {
        if (calculus == null) {
            System.err.println("Calculus must be set up");
            return false;
        }
        return applyPathConsistency(inPath);
    }


    private boolean applyPathConsistency(String cspFilePath) {
        if (!FileUtil.exists(cspFilePath)) {
            System.err.printf("File \"%s\" is not found \n", cspFilePath);
            return false;
        }

        CSPReader reader = new CSPReader(calculus, cspFilePath);

        boolean pathConsistent = false;


        List<CSPSparse> inputs = reader.makeCSPList();


        for (CSPSparse input: inputs) {
            if (input == null) {
                System.err.println("PathConsistency: SCP sparse is null!");
                return false;
            }

            CalculusOperations<Relation> calculusOperation = new CalculusOperations<>(calculus, Relation.class);

            CSP<Relation, CalculusOperations<Relation>> csp = new CSP<>(input, calculusOperation);

            WeightedTripleIterator<Relation, CSP<Relation, CalculusOperations<Relation>>> propagation = new WeightedTripleIterator<>();

            pathConsistent = propagation.enforce(csp).isEmpty();

            if (!pathConsistent) {
                if (!positiveOnly) {
                    System.out.println(csp.getName() + ": is not path consistency");
                }
            } else {
                if (!negativeOnly) {
                    System.out.println(csp.getName() + ": is path consistency");
                    if (showSolution) {
                        System.out.printf("%d %s \n", csp.size() - 1, csp.getName());
                        for (int i = 0; i < csp.size(); i++) {
                            for (int j = i + 1; j < csp.size(); j++) {
                                Relation rel = csp.getConstraint(i, j);
                                if (!rel.equals(calculusOperation.getUniversalRelation())) {
                                    System.out.println(i + " " + j);
                                    System.out.println(calculusOperation.getCalculus().relationToString(rel));
                                    System.out.println();
                                }
                            }
                        }
                    }
                }
            }
        }
//        CSPSparse cspSparse = reader.makeCSP();
        return pathConsistent;
    }


    public List<String> getReasoningResult(String cspFilePath) {

        List<String> lines = new ArrayList<>();


        CSPReader reader = new CSPReader(calculus, cspFilePath);
        boolean pathConsistent = false;
        List<CSPSparse> inputs = reader.makeCSPList();

        for (CSPSparse input: inputs) {

            CalculusOperations<Relation> calculusOperation = new CalculusOperations<>(calculus, Relation.class);
            CSP<Relation, CalculusOperations<Relation>> csp = new CSP<>(input, calculusOperation);

            WeightedTripleIterator<Relation, CSP<Relation, CalculusOperations<Relation>>> propagation = new WeightedTripleIterator<>();

            pathConsistent = propagation.enforce(csp).isEmpty();

            if (pathConsistent) {
                for (int i = 0; i < csp.size(); i++) {
                    for (int j = 0; j < csp.size(); j++) {
                        if (i == j) continue;
                        Relation rel = csp.getConstraint(i, j);
                        if (!rel.equals(calculusOperation.getUniversalRelation())) {
                            lines.add(i + " " + j + " " + calculusOperation.getCalculus().relationToString(rel));
                        }
                    }
                }
            }
        }
        return lines;
    }


}
