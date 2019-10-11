import static spark.Spark.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.lang.Thread.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;

import spark.*;



// This will be the backend for the user facing client, whatever that ends up being.

// A RESTful API using spring.

// The person who takes ownership of this will probably also end up owning the front end client as well.




public class Server {

    private static Prices prices = new Prices();

    public static void main(String[] args) {
        port(8000);

        //get("/hello", (request, response) -> "Hello World!");
        Gson gson = new Gson();


        get("/prices", (request, response) -> {

            //check if we have the parameters
            //if we do, start building the response'

            //x = list of response objects
            ArrayList<HashMap<String,String>> x = new ArrayList<>();
            // start at line 16264
            int start = Integer.parseInt(request.queryParams("start")); //make these ints or whatever we need for prices
            int end =  Integer.parseInt(request.queryParams("end"));
            for(int i = start; i < end; i++) {
                HashMap<String, String> response_obj = new HashMap<String, String>();
                response_obj.put("Close", prices.getMinuteBars().get(i).get("Close"));
                response_obj.put("Timestamp", prices.getMinuteBars().get(i).get("Timestamp"));
                x.add(response_obj);
            }
            //values = price.getClosingPrices(start, end)




            return x;
        }, gson::toJson);

        get("/KCA", (request, response) -> {
            //This is a blocking operation, lets spool it up in its own thread.
            long start = prices.toUnixtime(request.queryParams("start"));
            long end =  prices.toUnixtime(request.queryParams("end"));
            double q = Double.parseDouble(request.queryParams("q"));
            int fwd = Integer.parseInt(request.queryParams("fwd"));
            String interval = request.queryParams("interval");
            System.out.println("request parsed, calling bars");
            ArrayList<HashMap<String, String>> SubArray = prices.bars(start, end, interval);

            double[] timeseries = new double[SubArray.size()];
            for(int i = 0; i < SubArray.size(); i++){
                timeseries[i]= Double.parseDouble(SubArray.get(i).get("close"));
            }
            /*
            class Bang implements Runnable {
                double[] data;
                Bang(double[] data){this.data = data; }
                public void run(){
                    KalmanFilter kf = new KalmanFilter();
                    kf.KCA_setup(timeseries, 0.1);
                    ArrayList<double[]> x = kf.run_on_stream(Arrays.stream(data));
                }
                Thread t = new Thread(new Bang(timeseries)).start();
            }
            String kca_thread = Thread.currentThread().getName();
            */
            KalmanFilter kf = new KalmanFilter();
            kf.KCA_setup(timeseries, q);

            ArrayList<double[]> x = kf.run_on_stream(Arrays.stream(timeseries));
            ArrayList<double[]> pred = kf.KCA_predict(fwd);
            return Stream.concat(x.stream(), pred.stream()).collect(Collectors.toList());
        }, gson::toJson);


        get("/intervalbars", (request, response) -> {
            long start = prices.toUnixtime(request.queryParams("start"));
            long end =  prices.toUnixtime(request.queryParams("end"));
            String interval = request.queryParams("interval");
            ArrayList<HashMap<String, String>> SubArray = prices.bars(start, end, interval);
            return SubArray;
        }, gson::toJson);

        //http://localhost:8000/prices?start=2015&end=2017
//        get("/prices2", (request, response) -> {
//    		StringBuilder x = new StringBuilder();
//    		x.append("Timestamp: ");
//    		x.append(prices.fromUnixtime(Math.round(prices.getMinuteBars().get(0)[0])));
//
//    		x.append("Open: ");
//    		x.append(prices.getMinuteBars().get(0)[1]);
//    		return x.toString();
//    });
    }


}