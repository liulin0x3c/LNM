package org.liulin.helper;

public class Helper {
    public static double[][] normalize(double[][] params) {
//        params[k][n] k:params num; n param len
        for (double[] param : params) {
            double sum = 0;
            for (double data : param) {
                sum += data;
            }
            for (int i = 0; i < param.length; i++) {
                param[i] = param[i] / sum;
            }
        }

        return params;

    }

    // 转置矩阵
    public static double[][] transposeMatrix(double[][] matrix) {
        int rows = matrix.length;
        int columns = matrix[0].length;

        double[][] transposedMatrix = new double[columns][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                transposedMatrix[j][i] = matrix[i][j];
            }
        }
        return transposedMatrix;
    }


    public static double[] calculateH(double[][] params) {
        var H = new double[params.length];
        var n = params[0].length;
        var logn = Math.log(n);
        for (int i = 0; i < params.length; i++) {
            var param = params[i];
            var sum = 0.0D;
            for (int j = 0; j < param.length; j++) {
                double Nij = param[j];
                if (Nij == 0) continue;
                sum += Nij * Math.log(Nij);
            }
            H[i] = -1 * sum / logn;
        }
        return H;
    }

    public static double[] calculateAlpha(double[] H) {
        var alpha = new double[H.length];
        double sum = 0.0;
        for (double v : H) {
            sum += v;
        }
        for (int i = 0; i < H.length; i++) {
            alpha[i] = (1 - H[i]) / (H.length - sum);
        }
        return alpha;
    }

    public static double[] calculateCrit(double[][] params) {
        double[][] normalized = normalize(params);
        double[] H = calculateH(normalized);
        double[] alpha = calculateAlpha(H);
        var crit = new double[params[0].length];
        for (int i = 0; i < crit.length; i++) {
            for (int j = 0; j < alpha.length; j++) {
                crit[i] += normalized[j][i];
            }
        }
        return crit;
    }

}
