package org.liulin.last.v1;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class H {
    public double getH(double[] arr) {
        if (arr.length == 0) return 0;
        double VAL = 0;
        for (double v : arr) {
            VAL += v;
        }
        double sum = 0;
        for (double v : arr) {
            if (v == 0) continue;
            double a = v / VAL;
            sum -= Math.log(a) * a;
        }
        return sum;
    }

    public static void normalize(double[] param) {
//        params[k][n] k:params num; n param len
        double sum = 0;
        for (double data : param) {
            sum += data;
        }
        for (int i = 0; i < param.length; i++) {
            param[i] = param[i] / sum;
        }
    }


    public static void randCUT() throws IOException {
        Edge[] edges = H.loadEdges("my");
        {
            Path path = Paths.get("data\\" + "WEIGHTS" + ".txt");
            String data;
            try {
                data = Files.readString(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String[] lines = data.split("\n");
            for (int i = 0; i < lines.length; i++) {
                edges[i].weight = Double.parseDouble(lines[i]);
            }
        }
        String[] array = Arrays.stream(edges).map(edge -> edge.sour + "\t" + edge.dest + "\t" + edge.weight).filter(e -> new Random().nextDouble() < 0.3).toArray(String[]::new);
        var log = String.join("\n", array);
        Path path = Paths.get("data" + File.separator + "EW_CUT" + ".txt");
        Files.writeString(path, log, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static int[] mergeSortedArraysWithoutDuplicates(int[] a, int[] b) {
        int[] merged = new int[a.length + b.length]; // 创建一个合并后的数组

        int i = 0; // 指向数组 a 的指针
        int j = 0; // 指向数组 b 的指针
        int k = 0; // 指向合并后的数组 merged 的指针

        // 比较数组 a 和 b 的元素，并将不重复的元素合并到 merged 中
        while (i < a.length && j < b.length) {
            if (a[i] < b[j]) {
                if (k == 0 || merged[k - 1] != a[i]) {
                    merged[k++] = a[i]; // 如果 a[i] 不重复，则将 a[i] 添加到 merged 中，并移动指针 i 和 k
                }
                i++;
            } else if (a[i] > b[j]) {
                if (k == 0 || merged[k - 1] != b[j]) {
                    merged[k++] = b[j]; // 如果 b[j] 不重复，则将 b[j] 添加到 merged 中，并移动指针 j 和 k
                }
                j++;
            } else {
                if (k == 0 || merged[k - 1] != a[i]) {
                    merged[k++] = a[i]; // 如果 a[i] 和 b[j] 相等且不重复，则将 a[i] 添加到 merged 中，并移动指针 i、j 和 k
                }
                i++;
                j++;
            }
        }

        // 将数组 a 或 b 中剩余的元素添加到 merged 中
        while (i < a.length) {
            if (k == 0 || merged[k - 1] != a[i]) {
                merged[k++] = a[i++];
            } else {
                i++;
            }
        }

        while (j < b.length) {
            if (k == 0 || merged[k - 1] != b[j]) {
                merged[k++] = b[j++];
            } else {
                j++;
            }
        }

        return Arrays.copyOf(merged, k); // 返回合并后的数组 merged，并截取前 k 个元素
    }

    public static int[] findCommonElements(int[] a, int[] b) {
        List<Integer> commonList = new ArrayList<>(); // 存储共同元素的 List

        int i = 0; // 指向数组 a 的指针
        int j = 0; // 指向数组 b 的指针

        while (i < a.length && j < b.length) {
            if (a[i] == b[j]) {
                commonList.add(a[i]); // 如果当前元素相等，则添加到 commonList 中
                i++;
                j++;
            } else if (a[i] < b[j]) {
                i++; // 如果 a[i] < b[j]，则移动指针 i
            } else {
                j++; // 如果 a[i] > b[j]，则移动指针 j
            }
        }

        // 将 commonList 转换为 int[] 数组
        int[] result = new int[commonList.size()];
        for (int k = 0; k < commonList.size(); k++) {
            result[k] = commonList.get(k);
        }
        return result;
    }

    public static Edge[] loadEdges(String fileName) {
        Path path = Paths.get("data\\" + fileName + ".txt");
        String data;
        try {
            data = Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] lines = data.split("\n");
        var edgeNum = lines.length;
        var E = new Edge[edgeNum];
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] blocks = line.strip().split("\t");
            int fromId = Integer.parseInt(blocks[0]);
            int toId = Integer.parseInt(blocks[1]);
            if (blocks.length != 3) {
                E[i] = new Edge(fromId, toId, 1);
                continue;
            }
            double weight = Double.parseDouble(blocks[2]);
            E[i] = new Edge(fromId, toId, weight);
        }
        return E;
    }
}
