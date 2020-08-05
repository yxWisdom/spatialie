package edu.nju.ws.gqr;

import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class CalculusReader extends CalculusMaker {
    private String dataDir;

    private String calculusName;

    private String configFilePath;

    private List<String> baseRelations;

    private Map<String, Integer> baseRelationIndex;



    CalculusReader(String name, String dir, String configFilePath) {
        this.dataDir = dir;
        this.calculusName = name;
        this.configFilePath = configFilePath;
        baseRelationIndex = new HashMap<>();
        baseRelations = new ArrayList<>();
    }


    private static boolean ignoreLine(String s) {
        return s.isEmpty() || s.startsWith("#");
    }


    private void extractBaseRelations(Map<String, String> converseTable) {
        Set<String> baseRelationSet = new TreeSet<>();
        for (Map.Entry<String, String> m : converseTable.entrySet()) {
            baseRelationSet.add(m.getKey());
            baseRelationSet.add(m.getValue());
        }
        baseRelations.addAll(baseRelationSet);

        for (int i = 0; i < baseRelations.size(); i++) {
            baseRelationIndex.put(baseRelations.get(i), i);
        }
    }

    private List<Integer> compactConverseTable(Map<String, String> converseTable) {
        List<Integer> res = new ArrayList<>();
        for (String baseRelation : baseRelations) {
            assert converseTable.containsKey(baseRelation);
            String converseRelation = converseTable.get(baseRelation);
            assert baseRelationIndex.containsKey(converseRelation);
            res.add(baseRelationIndex.get(converseRelation));
        }
        return res;
    }

    private List<List<Relation>> compactCompositionTable(Map<Pair<String, String>, Set<String>> ct) {
        List<List<Relation>> table = new ArrayList<>(baseRelations.size());
        for (int i = 0; i < baseRelations.size(); i++) {
            table.add(new ArrayList<>(baseRelations.size()));
        }
        for (int i = 0; i < baseRelations.size(); i++) {
            String name_i = baseRelations.get(i);
            for (String name_j : baseRelations) {
                Pair<String, String> key = new ImmutablePair<>(name_i, name_j);
                assert ct.containsKey(key);
                Relation r = new Relation();
                for (String br : ct.get(key)) {
                    r.set(baseRelationIndex.get(br));
                }
                table.get(i).add(r);
            }
        }
        return table;
    }

    private List<Integer> compactWeights(Map<String, Integer> weights) {
        List<Integer> res = new ArrayList<>();
        for (String baseRelation : baseRelations) {
            res.add(weights.get(baseRelation));
        }
        return res;
    }

    private Map<String, Integer> readWeights(String filename) {
        Map<String, Integer> weights = new HashMap<>();
        List<String> lines = FileUtil.readLines(filename);
//        lines = lines.stream().filter(x -> !ignoreLine(x)).collect(Collectors.toList());
        for (String line : lines) {
            if (ignoreLine(line))
                continue;

            String[] pair = line.trim().split("\\s+");
            if (pair.length == 2) {
                try {
                    weights.put(pair[0], Integer.valueOf(pair[1]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    System.err.printf("Error while reading weight file. Could not parse \"%s\" \n", line);
                }
            } else {

                System.err.printf("Error while reading weight file. Could not parse \"%s\" \n", line);
            }
        }
        return weights;
    }

    private Map<Pair<String, String>, Set<String>> readCompositionTable(String filename) {
        Map<Pair<String, String>, Set<String>> compositionTable = new HashMap<>();
        List<String> lines = FileUtil.readLines(filename);
        for (String line : lines) {

            line = line.trim();
            if (ignoreLine(line)) continue;

            int compPos = line.indexOf(":");
            int isPos = line.indexOf("::");
            if (compPos >= 0 && isPos >= 0) {
                String r1 = line.substring(0, compPos).trim();
                String r2 = line.substring(compPos+1, isPos).trim();
                String comp = line.substring(isPos + 2).trim();
                comp = comp.substring(1, comp.length() - 1).trim();
                Pair<String, String> key = new ImmutablePair<>(r1, r2);
                String[] relations = comp.split("\\s+");
                Set<String> value = new TreeSet<>(Arrays.asList(relations));
                assert !compositionTable.containsKey(key);
                compositionTable.put(key, value);
            }
        }
        return compositionTable;
    }

    private Map<String, String> readConverseTable(String filename) {
        Map<String, String> converseTable = new HashMap<>();
        List<String> lines = FileUtil.readLines(filename);
        for (String line : lines) {
            line = line.trim();
            if (ignoreLine(line))
                continue;

            String[] p = line.split("::");
            if (p.length != 2) {
                System.err.printf("Error while reading converse file. Could not parse \"%s\" \n", line);
            } else {
                String relation = p[0].trim(), conv_relation = p[1].trim();
                assert !converseTable.containsKey(relation);
                converseTable.put(relation, conv_relation);
            }
        }
        return converseTable;
    }

    private Map<String, String> readConfigFile(String filename) {
        Map<String, String> config = new HashMap<>();
        List<String> lines = FileUtil.readLines(filename);
        for (String line : lines) {
            line = line.trim();
            if (ignoreLine(line))
                continue;
            String[] p = line.split(" ");
            if (p.length != 2) {
                System.err.printf("Error while reading config file. Could not parse \"%s\" \n", line);
            } else {
                String key = p[0].trim(), value = p[1].trim();
                assert !config.containsKey(key);
                config.put(key, value);
            }
        }
        return config;
    }

    @Override
    public Calculus makeCalculus() {
        final String IDENTITY = "identity";
        final String CALCULUS_SIZE = "calculus_size";
        final String CONVERSE_FILE = "converse_file";
        final String COMP_TABLE_FILE = "comp_table_file";
        final String WEIGHTS = "weights";



        Map<String, String> config = readConfigFile(configFilePath);
        if (!config.containsKey(IDENTITY)) {
            System.err.println("No identity relation defined.");
            return null;
        }
        String identity = config.get(IDENTITY);

        if (!config.containsKey(CALCULUS_SIZE)) {
            System.err.println("calculus_size' not defined.");
            return null;
        }
        int baseRelationsNum = Integer.valueOf(config.get(CALCULUS_SIZE));

        if (!config.containsKey(CONVERSE_FILE)) {
            System.err.println("No converse relation table file defined.");
            return null;
        }
        String converseTableFilePath = dataDir + "/" + config.get(CONVERSE_FILE);


        Map<String, String> converseTable = readConverseTable(converseTableFilePath);

        if (baseRelationsNum != converseTable.size()) {
            System.err.println("Indicated number of base relations in spec file does not match the size of the converse table.");
            return null;
        }

        extractBaseRelations(converseTable);

        if (baseRelationsNum != baseRelations.size()) {
            System.err.println("Indicated number of base relations in spec file does not match the number of base relations found in the converse table.");
            return null;
        }

        assert baseRelationIndex.containsKey(identity);

        if (!config.containsKey(COMP_TABLE_FILE)) {
            System.err.println("No comp_table_file defined.");
            return null;
        }
        String compTableFilePath = dataDir + "/" + config.get(COMP_TABLE_FILE);
        Map<Pair<String, String>, Set<String>> compositionTable = readCompositionTable(compTableFilePath);

        if (compositionTable.size() != baseRelationsNum * baseRelationsNum) {
            System.err.println("Size of the composition table does not match the squared number of base relations.");
            return null;
        }

        List<Integer> compactConverseTable = compactConverseTable(converseTable);
        List<List<Relation>> compactCompTable = compactCompositionTable(compositionTable);
        int id = baseRelationIndex.get(identity);

        if (config.containsKey(WEIGHTS)) {
            String weightsFilePath = dataDir + "/" + config.get(WEIGHTS);
            Map<String, Integer> weights = readWeights(weightsFilePath);
            if (weights.size() != baseRelationsNum) {
                System.err.println("Size of weights table does not match the number of base relations.");
                return null;
            }
            List<Integer> compactWeights = compactWeights(weights);
            return new Calculus(calculusName, baseRelations, id, compactConverseTable, compactCompTable, compactWeights);
        } else {
            // TODO：是否赋予合适的权值？
            List<Integer> compactWeights = baseRelations.stream().map(x -> 1).collect(Collectors.toList());
            return new Calculus(calculusName, baseRelations, id, compactConverseTable, compactCompTable, compactWeights);
        }
    }
}
