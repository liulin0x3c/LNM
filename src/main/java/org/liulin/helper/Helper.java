package org.liulin.helper;

public class Helper {

    private static double[][] normalize(double[][] params) {
//        params[k][n] k:params num; n param len
        double[][] normalized = new double[params.length][params[0].length];
        for (int j = 0; j < params.length; j++) {
            double[] param = params[j];
            double sum = 0;
            for (double data : param) {
                sum += data;
            }
            for (int i = 0; i < param.length; i++) {
                normalized[j][i] = param[i] / sum;
            }
        }
        return normalized;
    }

    private static double[] calculateH(double[][] params) {
        var H = new double[params.length];
        var n = params[0].length;
        var logn = Math.log(n);
        for (int i = 0; i < params.length; i++) {
            var param = params[i];
            var sum = 0.0D;
            for (double Nij : param) {
                if (Nij == 0) continue;
                sum += Nij * Math.log(Nij);
            }
            H[i] = -1 * sum / logn;
        }
        return H;
    }

    private static double[] calculateAlpha(double[] H) {
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
//        double[][] normalized = normalize(params);
        double[][] normalized = params;
        double[] H = calculateH(normalized);
        double[] alpha = calculateAlpha(H);
        var crit = new double[params[0].length];
        for (int i = 0; i < crit.length; i++) {
//            for (int j = 0; j < alpha.length; j++) {
//                crit[i] += alpha[j] * normalized[j][i];
//            }
            crit[i] += Math.exp(normalized[0][i]);
        }
        return crit;
    }


}
