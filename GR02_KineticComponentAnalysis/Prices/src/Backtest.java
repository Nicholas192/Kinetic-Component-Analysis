import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

/*
 **** THIS IS NOT UNIT TESTING ****
 *
 Okay, so we need a way to run our algo on historical data.
 We need a market simulator?
 We need to calculate the in-sample sharpe ratio and out-of-sample sharpe ratio

 We need a brainstorm session on how we're going to design this.

 These slides are great:
 Lopez de Prado, Marcos, The 7 Reasons Most Machine Learning Funds Fail (Presentation Slides) (September 2, 2017).
 Available at SSRN: https://ssrn.com/abstract=3031282 or http://dx.doi.org/10.2139/ssrn.3031282


 We'd like to analyize the Accuracy, precision, and recall (sensitivity) of the algorithm.

 use k-fold cross-validation (Desai et al., 1996)

 */
public class Backtest {

    private Prices prices;

    public Backtest() {
        prices = new Prices();
    }

    public String[] makePredKCA(int start, int end, int win, int fwd) {
        double[] closing_prices = Stream.of(prices.getClose(start, end)).mapToDouble(Double::doubleValue).toArray();
        String[] predictions = new String[end-start];
        for (int i = 0; i < closing_prices.length - win - 1; i++) {
            double[] closeValsWindow = Stream.of(prices.getClose(i, i+win)).mapToDouble(Double::doubleValue).toArray();
            //We build a kalman filter on the data we need, and make a prediction
            KalmanFilter kf = new KalmanFilter(); //init the object
            kf.KCA_setup(closeValsWindow, 0.1); // alternate constructor essentially
            // next we iterate the KCA algorithm on our stream
            ArrayList<double[]> x = kf.run_on_stream(Arrays.stream(closeValsWindow));
            //now we can get a prediction
            ArrayList<double[]> kca_fwd = kf.KCA_predict(fwd);

            if (kca_fwd.get(kca_fwd.size()-1)[0] > x.get(x.size()-1)[0]){
                predictions[i] = "1";
            } else if (kca_fwd.get(kca_fwd.size()-1)[0] < x.get(x.size()-1)[0]) {
                predictions[i] = "-1";
            }
        }
        return predictions;
    }

    public String[] makeLabels(int start, int end, int fwd) {
        Double[] x = prices.getClose(start, end+fwd);
        String[] label = new String[x.length-1];
        for (int i = 0; i < x.length; i++) {
            if (x[i] > x[x.length-1]) {
                label[i] = "1";
            } else if (x[i] < x[x.length-1]) {
                label[i] = "-1";
            }
        }

        return label;
    }

    public String[] predChart(String[] pred, String[] actual) {
        int chart_size = pred.length - 10;
        String[] chart = new String[chart_size];
        for (int i = 0; i < chart_size-1; i++) {
            System.out.println(pred.length + " " + actual.length + " i" + i);
            if (actual[i] == null){
                System.out.println(actual[i]);
            }
            if (pred[i].equals(actual[i])) {
                if (pred[i].equals("-1")) {
                    chart[i] = "tn";
                } else {
                    chart[i] = "tp";
                }
            } else if (!pred[i].equals(actual[i])) {
                if (pred[i].equals("-1")) {
                    chart[i] = "fn";
                } else {
                    chart[i] = "fp";
                }
            }
        }
        return chart;
    }

    public double accuracy(String[] chart) {
        int tpCount = 0;
        int tnCount = 0;
        int fpCount = 0;
        int fnCount = 0;
        for (int i = 0; i < chart.length - 10; i++) {
            if (chart[i].equals("tp")) {
                tpCount++;
            } else if (chart[i].equals("tn")) {
                tnCount++;
            } else if (chart[i].equals("fp")) {
                fpCount++;
            } else if (chart[i].equals("fn")) {
                fnCount++;
            }
        }
        if (tpCount + tnCount +fpCount + fnCount == 0) {
            return 0.0;
        }
        //System.out.println(tpCount);
        //System.out.println(tnCount);
        //System.out.println(fpCount);
        //System.out.println(fnCount);
        double result = (tpCount + tnCount)/(tpCount + tnCount + fpCount + fnCount);
        System.out.println("tp: " +tpCount + " tn: "+tnCount + "fp: " + fpCount+ "fn: " + fnCount);
        return result;
    }

    public double precision(String[] chart) {
        int tpCount = 0;
        int tnCount = 0;
        int fpCount = 0;
        int fnCount = 0;
        for (int i = 0; i < chart.length - 10; i++) {
            if (chart[i].equals("tp")) {
                tpCount++;
            } else if (chart[i].equals("tn")) {
                tnCount++;
            } else if (chart[i].equals("fp")) {
                fpCount++;
            } else if (chart[i].equals("fn")) {
                fnCount++;
            }
        }
        if (tpCount + fpCount == 0) {
            return 0.0;
        }
        double result = (tpCount)/(tpCount + fpCount);
        return result;
    }

    public double recall(String[] chart) {
        int tpCount = 0;
        int tnCount = 0;
        int fpCount = 0;
        int fnCount = 0;
        for (int i = 0; i < chart.length - 10; i++) {
            if (chart[i].equals("tp")) {
                tpCount++;
            } else if (chart[i].equals("tn")) {
                tnCount++;
            } else if (chart[i].equals("fp")) {
                fpCount++;
            } else if (chart[i].equals("fn")) {
                fnCount++;
            }
        }
        if (tpCount + fnCount == 0) {
            return 0.0;
        }
        double result = (tpCount)/(tpCount + fnCount);
        return result;
    }

    public double specificity(String[] chart) {
        int tpCount = 0;
        int tnCount = 0;
        int fpCount = 0;
        int fnCount = 0;
        for (int i = 0; i < chart.length - 10; i++) {
            if (chart[i].equals("tp")) {
                tpCount++;
            } else if (chart[i].equals("tn")) {
                tnCount++;
            } else if (chart[i].equals("fp")) {
                fpCount++;
            } else if (chart[i].equals("fn")) {
                fnCount++;
            }
        }
        if (tnCount + fpCount == 0) {
            return 0.0;
        }
        double result = (tnCount)/(tnCount + fpCount);
        return result;
    }

    public static void main(String[] args) {
        Backtest temp = new Backtest();

        String[] predKCA = temp.makePredKCA(6000, 10000, 10, 2);
        String[] labels = temp.makeLabels(6000, 10000, 2);
        String[] predChart = temp.predChart(predKCA, labels);

        for (int i = 0; i < predChart.length; i++) {
            System.out.println(predKCA[i]);
        }

        double accuracy = temp.accuracy(predChart);
        double precision = temp.precision(predChart);
        double recall = temp.recall(predChart);
        double specificity = temp.specificity(predChart);
        System.out.println("accuracy: " + accuracy + " precision: " + precision + " recall: " + recall + " specificity: " + specificity);
    }

}