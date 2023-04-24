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

    public synchronized static Edge[] loadEdges(String fileName) {
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
