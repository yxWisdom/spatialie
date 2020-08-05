package edu.nju.ws.gqr;

import edu.nju.ws.spatialie.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

class CSPReader {
    private Calculus calculus;
    private String filePath;

    CSPReader(Calculus calculus, String filePath) {
        this.calculus = calculus;
        this.filePath = filePath;
    }

    CSPSparse makeCSP() {
        List<List<String>> lines = splitCSP();
        if (lines == null)
            return null;
        return makeSingleCSP(lines.get(0));
    }

    List<CSPSparse> makeCSPList() {
        List<List<String>> list = splitCSP();
        if (list == null)
            return null;
        return list.stream().map(this::makeSingleCSP).collect(Collectors.toList());
    }


    private CSPSparse makeSingleCSP(List<String> lines) {
        if (lines == null)
            return null;

        int size;

        String dataName = "";

        if (lines.size() <= 1) {
            System.err.println("CSPReader: the format of csp file is not correct");
            return null;
        }

        String info = lines.get(0);
        String[] array = info.split(" ");

        if (array.length >= 2) {
            size = Integer.valueOf(array[0].trim()) + 1;
        } else {
            System.err.println("CSPReader: the format of csp file is not correct");
            return null;
        }

        if (array[1].startsWith("#")) {
            dataName = array[1].trim().substring(1);
        }

        CSPSparse csp = new CSPSparse(size, calculus, dataName);

        for (String line : lines.subList(1, lines.size())) {
            line = line.trim();
            if (line.startsWith("#") || line.length() == 0)
                continue;
            if (line.startsWith("."))
                break;

            Scanner scanner = new Scanner(line);

            int x = scanner.nextInt();
            int y = scanner.nextInt();
            String s = scanner.nextLine().trim();


            if (!s.startsWith("(") || !s.endsWith(")")) {
                System.err.printf("CSPReader: Failed to parse line: \"%s\"\n", line);
                return null;
            }

            if (!(x >= 0 && x < size) || !(y >= 0 && y < size)) {
                System.err.printf("CSPReader: Non-existent edge (%d,%d)", x, y);
                return null;
            }

            Relation r = calculus.encodeRelation(s.substring(1, s.length() - 1).trim());
            if (r.size() == 0) {
                System.err.printf("CSPReader: Empty relation: \"%s\"", s);
                return null;
            }
            csp.addConstraint(x, y, r);
        }
        return csp;

    }

    private List<List<String>> splitCSP() {

        if (!FileUtil.exists(filePath)) {
            System.err.printf("CSPReader: csp file \"%s\"is not found \n", filePath);
            return null;
        }
        List<List<String>> list = new ArrayList<>();

        List<String> lines = FileUtil.readLines(filePath);

        lines = lines.stream().map(String::trim).filter(s -> s.length()>0).collect(Collectors.toList());
        int index = 0;

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(".")) {
                list.add(lines.subList(index, i+1));
                index = i + 1;
            }
        }
        if (index < lines.size()) {
            list.add(lines.subList(index, lines.size() + 1));
        }
        return list;
    }

}
