package edu.nju.ws.spatialie.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileUtil {
    public static String readFile(String path) {
        File file = new File(path);
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileContent);
            fileInputStream.close();
            return new String(fileContent, StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> readFileByLine(String path, String delimiter,int row) {
        List<String> rst= new ArrayList<>();
        String allText = readFile(path);
        if (allText != null) {
            String [] lines = allText.split("\n");
            for (String line: lines) {
                rst.add(line.trim().split(delimiter)[row]);
            }
        }
        return rst;
    }


    public static List<String> readFileByLine(String path,int row) {
        List<String> rst= new ArrayList<>();
        String allText = readFile(path);
        if (allText != null) {
            String [] lines = allText.split("\n");
            for (String line: lines) {
                rst.add(line.trim().split(" ")[row]);
            }

        }
        return rst;
    }

    public static List<String []> readFileByLine(String path) {
        List<String []> rst= new ArrayList<>();
        String allText = readFile(path);
        if (allText != null) {
            String [] lines = allText.split("\n+");
            for (String line: lines) {
                rst.add(line.trim().split(" "));
            }

        }
        return rst;
    }

    public static List<String []> readFileByLine(String path, String delimiter) {
        List<String []> rst= new ArrayList<>();
        String allText = readFile(path);
        if (allText != null) {
            String [] lines = allText.split("\n+");
            for (String line: lines) {
                rst.add(line.trim().split(delimiter));
            }

        }
        return rst;
    }


    public static List<String> readLines(String path) {
        List<String> rst= new ArrayList<>();
        String allText = readFile(path);
        if (allText != null) {
            String [] lines = allText.split("\n");
            for (String line: lines) {
                rst.add(line.trim());
            }
        }
        return rst;
    }


    public static void writeFile(String path, Collection<String> lines, boolean append) {
        File file = new File(path);
        try {
            FileWriter writer = new FileWriter(file, append);
            for (String line: lines) {
                writer.write(line + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String path, Collection<String> lines) {
        writeFile(path, lines, false);
    }

    public static void writeFile(String path, String text) {
        writeFile(path, text, false);
    }

    public static void writeFile(String path, String text, boolean append) {
        File file = new File(path);
        try {
            FileWriter writer = new FileWriter(file, append);
            writer.write(text + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mergeFile(String source, String goal) {
        String text = readFile(source);
        writeFile(goal, text,true);
    }


    /**
     * 删除单个文件
     *
     * @param fileName
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
//            System.out.println("删除单个文件" + fileName + "成功！");
//            System.out.println("删除单个文件" + fileName + "失败！");
            return file.delete();
        } else {
//            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    public static boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean createDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return false;
    }


    public static List<File> listFiles(String [] dirs, boolean recursive) {
        List<File> files = new ArrayList<>();
        for (String dir: dirs) {
            files.addAll(listFiles(dir, recursive));
        }
        return files;
    }

    public static List<File> listFiles(String [] dirs) {
        List<File> files = new ArrayList<>();
        for (String dir: dirs) {
            files.addAll(listFiles(dir));
        }
        return files;
    }

    public static List<File> listFiles(String dir) {
        return listFiles(dir, true);
    }

    public static List<File> listFiles(String dir, boolean recursive) {
        List<File> files = new ArrayList<>();
        File dirFile = new File(dir);
        if (!recursive) {
            files = Arrays.asList(Objects.requireNonNull(dirFile.listFiles()));
        } else {
            Queue<File> queue = new LinkedList<>();
            queue.offer(dirFile);
            while (!queue.isEmpty()) {
                File curFile = queue.poll();
                File [] subFiles = curFile.listFiles();
                assert subFiles != null;
                for (File subFile: subFiles) {
                    if (subFile.isDirectory())
                        queue.offer(subFile);
                    else
                        files.add(subFile);
                }
            }
        }
        return files;
    }


    public static void main(String [] args) {
        FileUtil.writeFile("data/corpus/test.txt", Collections.singletonList("123123"), true);
    }

    public static List<String> readLineswithEmptyLine(String path) {
        File file = new File(path);
        InputStream is = null;
        BufferedReader br = null;
        String tmp;
        List<String> rst = new ArrayList<>();
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            while ((tmp = br.readLine()) != null) {
                rst.add(tmp);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rst;
    }
}
