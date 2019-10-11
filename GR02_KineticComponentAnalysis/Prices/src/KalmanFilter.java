/** KalmanFilter.
    The graph algorithm is used here to collect and manage threads.
*/

import org.apache.commons.math3.linear.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.DoubleStream;

import static org.apache.commons.math3.linear.MatrixUtils.createRealIdentityMatrix;

public class KalmanFilter implements Runnable {
    private double[] timeseries_measurements;
    protected RealMatrix X, X0;
    protected RealMatrix F, B, U, Q;
    protected RealMatrix H, R;
    protected RealMatrix P, P0;
    private ArrayList<double[]> x_mean;
    private ArrayList<double[]> x_covar;
    public ArrayList<double[]> get_x_means(){
        return this.x_mean;
    }
    public ArrayList<double[]> get_x_covar(){
        return this.x_covar;
    }
    // State vector
    public void setX(RealMatrix x){ this.X = x; }

    // uncertainty(error) covariance matrix
    public void setP(RealMatrix p){ this.P = p; }

    // transition matrix
    public void setF(RealMatrix f){ this.F = f; }

    // process noise covariance matrix
    public void setQ(RealMatrix q){ this.Q = q; }

    // input vector
    public void setU(RealMatrix u){ this.U = u; }

    // measurement or transformation matrix
    public void setH(RealMatrix h) { this.H = h; }

    // measurement noise covariance
    public void setR(RealMatrix r){ this.R = r; }

    // input gain. unused by KCA, should always be an identity matrix
    public void setB(RealMatrix b){ this.B = b; }

    public void predict() {
        X0 = F.multiply(X).add(B.multiply(U));

        P0 = F.multiply(P).multiply(F.transpose()).add(Q);
    }

    public void correct(RealMatrix Z) {
        RealMatrix S = H.multiply(P0).multiply(H.transpose()).add(R);
        RealMatrix S_inverse = new LUDecomposition(S).getSolver().getInverse(); //this takes a long time
        RealMatrix K = P0.multiply(H.transpose()).multiply(S_inverse);

        X = X0.add(K.multiply(Z.subtract(H.multiply(X0))));

        RealMatrix I = createRealIdentityMatrix(P0.getRowDimension());

        P = (I.subtract(K.multiply(H))).multiply(P0);
    }

    public void KCA_setup(double[] measurements, double q){
        //somehow we need to do extract a taylor expansion of the price dynamics
        this.timeseries_measurements = measurements;

        double h = 1;
        RealMatrix F = new Array2DRowRealMatrix(new double[][] {{ 1, h, .5*(h*h)}
                ,{0, 1, h}
                ,{0, 0, 1}});

        //this needs to change.
        this.setQ(MatrixUtils.createRealIdentityMatrix(3).scalarMultiply(q));

        this.setX(new Array2DRowRealMatrix(new double[][] {{0},{0},{0}}));
        this.setP(MatrixUtils.createRealIdentityMatrix(3));
        this.setF(F); // transition matrix
        this.setU(new Array2DRowRealMatrix(new double[][] {{0}})); //input
        this.setH(new Array2DRowRealMatrix(new double[][] {{1d, 0 ,0}})); //measurements

        // R. measurement noise. unused for our app, used if we could influence price data.
        this.setR(MatrixUtils.createRealIdentityMatrix(1));

        this.setB(new Array2DRowRealMatrix((new double[]{1,0,0}))); // external input

    }

    public ArrayList<double[]> KCA_predict(int fwd){
        ArrayList<double[]> values = new ArrayList<>();
        for(int i =0; i< fwd; i++){
            this.predict();
            this.correct(new Array2DRowRealMatrix(new double[] {this.X.getColumn(0)[0]}));
            values.add(this.X.getColumn(0));
        }
        return values;
    }
    public ArrayList<double[]> run_on_stream(DoubleStream stream){
        ArrayList<double[]> values = new ArrayList<>();
        stream.forEach(measurement -> {
            this.predict();
            this.correct(new Array2DRowRealMatrix(new double[] {measurement}));
            values.add(this.X.getColumn(0));
        });
        return values;
    }

    @Override
    public void run(){
        this.run_on_stream(Arrays.stream(this.timeseries_measurements));
    }

    public static void main(String[] args){
        //tests
        double[] m = new double[] {10.1, 9.9, 10.2, 10.3, 10};
        KalmanFilter kf = new KalmanFilter();
        kf.KCA_setup(m, 0.1);
        ArrayList<double[]> x = kf.run_on_stream(Arrays.stream(m));
        x.forEach(j -> System.out.println(j[0]));
    }
    //getters and setters go here
}
