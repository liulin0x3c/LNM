package org.liulin.last.v1.io;

import org.liulin.last.v1.Edge;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class IO {
    public static void recordEdges(String fileName, Edge[] edges) {
        recordEdges(fileName, edges, false);
    }


    public static void recordEdges(String fileName, Edge[] edges, boolean weight) {
        String[] array = Arrays.stream(edges).map(edge -> {
            if (weight) return edge.toString() + "\t" + edge.weight;
            else return edge.toString();
        }).toArray(String[]::new);
        var log = String.join("\n", array);
        Path path = Paths.get("data" + File.separator + "cut" + File.separator + fileName + ".txt");
        try {
            Files.writeString(path, log, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Edge[] loadEdges(String fileName) {
        Path path = Paths.get("data" + File.separator + "edge" + File.separator + fileName + ".txt");
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
            String[] blocks = line.strip().split("[ \t]+");
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
