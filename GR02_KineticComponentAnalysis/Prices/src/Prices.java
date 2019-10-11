//In order for us to do accurate and precise analysis on price history, we need to do some modifications to the
// time series data. (The 7 Reasons Most Machine Learning Funds Fail. [prado 2017])

// This class needs to return a subsampled section of the prices, with certain formulae applied/
// - returns on prices (or changes in log-prices)
//  – changes in yield
//  – changes in volatility

// Dai and Zheng (2013) shows that the more indicators used the better. 2 features -> 55% Accuracy. 16 Features -> 79%

// we should also allow a user to exponentially smooth and weight the time series data.

// Stretch Goals Indicators to Implement:
// -  Relative Strength Index
// -  Stochastic Oscillator
// -  Williams %R
// -  Moving Average Convergence Divergence
// -  Price Rate of Change
// -  On Balance Volume
// - Triple Barrier Labeling Method


// How I envision the API for this class: *nvm this is dumb
//      Prices('2016', '2018-01;, '14d').RSI()
//      Prices(2014-09, 2018-01-01, '1w'.Volatility()
//      Prices(2014-09, 2018-01-01 '1w').LogReturns()

// Not entirely certain what the return values are, probably just regular arrays

// We will want this prices class to be its own module, probably running in its own non-blocking process or thread.
// and we need to rename it.

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;


public class Prices {
    private static ArrayList<HashMap<String,String>> minute_bars;

    public Prices() {
        //Timestamp, Open, High, Low, Close, Volume_(BTC), Volume_(currency), Weighted_Price
        minute_bars= load_data("./Data/bitcoin-historical-data/coinbaseUSD_1-min_data_2014-12-01_to_2018-01-08.csv");

    }

    public ArrayList<HashMap<String,String>> getMinuteBars(){
        return minute_bars;
    }

    public String returnString() {
        return "hello";
    }

    //loads the data from the input file, using an array of key string key value hashmaps
    private ArrayList<HashMap<String,String>> load_data(String path){
        System.out.println("load_data called");
        BufferedReader br = null;
        String line = null;
        ArrayList<HashMap<String,String>> bars = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(path));
            br.readLine();
            String[] keys = {"date","open","high","low","close","volume","Volume_(currency)","Weighted_Proce"};
            while ((line = br.readLine()) != null) {
                //Timestamp, Open, High, Low, Close, Volume_(BTC), Volume_(currency), Weighted_Price
                String[] bar_string = line.split(",");
                HashMap<String,String> bar = new HashMap<String,String>(8);
                for (int i = 0; i < 8; i++) {
                    bar.put(keys[i], bar_string[i]);
                }
                bars.add(bar);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Prices loaded.");
        return bars;
    }

    //returns an array of minute bars, between 2 given times
    //Takes longs as input. These are the start and end time in unix time
    private ArrayList<HashMap<String,String>> parse(long start_time, long end_time){
        int place = minute_bars.size()/2;
        int high = minute_bars.size()-1;
        int low = 0;
        System.out.println(start_time);

        while(Integer.parseInt(minute_bars.get(place).get("date")) != start_time) {
            if (Integer.parseInt((minute_bars.get(place).get("date"))) < start_time) {
                low = place;
                place = place + ((high-low)/2);
            }else {
                high = place;
                place = place - ((high-low)/2);
            }
        }
        System.out.println(place);
        ArrayList<HashMap<String,String>> SubArray = new ArrayList<HashMap<String,String>>();
        for (int i = place; (Integer.parseInt((minute_bars.get(i).get("date")))) <= end_time;i++) {
            SubArray.add(minute_bars.get(i));

        }
        return SubArray;
    }


    //converts the minute bars graph into a graph of differently timed bars.
    //Can convert into minutes, hours and days
    //String format - length of bars + m or h or d (minutes,hours,days) -- ex 2m 3h 7d -- 2 minutes 3 hours 7days
    //takes 2 longs as start and end times This is the unix time of the start and end times of the bars

    public ArrayList<HashMap<String,String>> bars(long start_time, long end_time, String interval_sample){

        ArrayList<HashMap<String, String>> barsArray = new ArrayList<HashMap<String,String>>();



        ArrayList<HashMap<String,String>> parseArray = parse(start_time, end_time);
        if (interval_sample == "1m") {
            return parseArray;
        }
        String[] intervalSampleSplit = interval_sample.split("");
        int barsInBar;
        interval_sample = "";
        for (int i = 0;i<intervalSampleSplit.length-1;i++) {
            interval_sample = interval_sample + intervalSampleSplit[i];
        }

        if(intervalSampleSplit[intervalSampleSplit.length-1].equals("m")){
            barsInBar = Integer.parseInt(interval_sample);
        }else if(intervalSampleSplit[intervalSampleSplit.length-1].equals("h")) {
            barsInBar = Integer.parseInt(interval_sample)*60;
        }else if(intervalSampleSplit[intervalSampleSplit.length-1].equals("d")) {
            barsInBar = Integer.parseInt(interval_sample)*24*60;
        }else {
            barsInBar = 0;
            System.out.println("error");
        }
        //Timestamp, Open, High, Low, Close, Volume_(BTC), Volume_(currency), Weighted_Price
        String Timestamp;
        Double Open;
        Double High;
        Double Low;
        Double Close;
        Double Volume_BTC;
        Double Volume_currency;
        //Float Weighted_Price -- Not yet implemented


        for (int i = 0; i < parseArray.size()/barsInBar; i++) {
            Timestamp = parseArray.get(i*barsInBar).get("date");
            Open = Double.parseDouble(parseArray.get(i*barsInBar).get("open"));
            Close = Double.parseDouble(parseArray.get(((i+1)*barsInBar)-1).get("close"));
            High = Double.parseDouble(parseArray.get(i*barsInBar).get("high"));
            Low = Double.parseDouble(parseArray.get(i*barsInBar).get("low"));
            Volume_BTC = Double.parseDouble(parseArray.get(i*barsInBar).get("volume"));
            Volume_currency = Double.parseDouble(parseArray.get(i*barsInBar).get("Volume_(currency)"));
            //Weighted_Price = 0.0 -- not yet implemented

            for (int j = 1; j < barsInBar; j++) {
                if (Double.parseDouble(parseArray.get(j + (i *barsInBar)).get("high"))>High) {
                    High = Double.parseDouble(parseArray.get(j + (i *barsInBar)).get("high"));
                }

                if (Double.parseDouble(parseArray.get(j + (i * barsInBar)).get("low")) < Low) {
                    Low = Double.parseDouble(parseArray.get(j + (i *barsInBar)).get("low"));
                }
                Volume_BTC = Volume_BTC + (Double.parseDouble(parseArray.get(j + (i *barsInBar)).get("volume")));
                Volume_currency = Volume_currency + (Double.parseDouble(parseArray.get(j + (i *barsInBar)).get("Volume_(currency)")));
                //Weighted_Price -- not yet implemented
            }
            HashMap<String,String> bar = new HashMap<String,String>(8);
            bar.put("date",Timestamp);
            bar.put("open", Open.toString());
            bar.put("high",High.toString());
            bar.put("low",Low.toString());
            bar.put("close", Close.toString());
            bar.put("volume",Volume_BTC.toString());
            bar.put("Volume_(currency)", Volume_currency.toString());
            bar.put("Weighted_Price", "NYI");
            barsArray.add(bar);

        }

        return barsArray;
    }


    //open getters
    public Double[] getOpen(){
        ArrayList<Double> openValues = new ArrayList<Double>();
        for (int i = 0; i < minute_bars.size(); i++) {
            openValues.add(Double.parseDouble(minute_bars.get(i).get("Open")));
        }
        Double[] returnList = openValues.toArray(new Double[openValues.size()]);
        return returnList;
    }

    public  Double[] getOpen(String startDate, String endDate){

        long start = toUnixtime(startDate);
        long end = toUnixtime(endDate);
        ArrayList<Double> openValues = new ArrayList<Double>();
        ArrayList<HashMap<String,String>> parseArray = parse(start,end);
        for (int i = 0; i < parseArray.size(); i++) {
            openValues.add(Double.parseDouble(parseArray.get(i).get("Open")));
        }

        Double[] returnList = openValues.toArray(new Double[openValues.size()]);
        return returnList;

    }

    public Double[] getOpen(int start, int end){
        ArrayList<Double> openValues = new ArrayList<Double>();
        for (int i = start; i <= end; i++) {
            openValues.add(Double.parseDouble((minute_bars.get(i).get("Open"))));
        }

        Double[] returnList = openValues.toArray(new Double[openValues.size()]);
        return returnList;

    }

    //high getters

    public Double[] getHigh(){
        ArrayList<Double> highValues = new ArrayList<Double>();
        for (int i = 0; i < minute_bars.size(); i++) {
            highValues.add(Double.parseDouble(minute_bars.get(i).get("High")));
        }
        Double[] returnList = highValues.toArray(new Double[highValues.size()]);
        return returnList;
    }

    public  Double[] getHigh(String startDate, String endDate){

        long start = toUnixtime(startDate);
        long end = toUnixtime(endDate);
        ArrayList<Double> highValues = new ArrayList<Double>();
        ArrayList<HashMap<String,String>> parseArray = parse(start,end);
        for (int i = 0; i < parseArray.size(); i++) {
            highValues.add(Double.parseDouble(parseArray.get(i).get("High")));
        }

        Double[] returnList = highValues.toArray(new Double[highValues.size()]);
        return returnList;

    }


    public Double[] getHigh(int start, int end){
        ArrayList<Double> highValues = new ArrayList<Double>();
        for (int i = start; i <= end; i++) {
            highValues.add(Double.parseDouble((minute_bars.get(i).get("High"))));
        }


        Double[] returnList = highValues.toArray(new Double[highValues.size()]);
        return returnList;
    }

    //Low getters

    public Double[] getLow(){
        ArrayList<Double> lowValues = new ArrayList<Double>();
        for (int i = 0; i < minute_bars.size(); i++) {
            lowValues.add(Double.parseDouble(minute_bars.get(i).get("Low")));
        }
        Double[] returnList = lowValues.toArray(new Double[lowValues.size()]);
        return returnList;
    }

    public  Double[] getLow(String startDate, String endDate){

        long start = toUnixtime(startDate);
        long end = toUnixtime(endDate);
        ArrayList<Double> lowValues = new ArrayList<Double>();
        ArrayList<HashMap<String,String>> parseArray = parse(start,end);
        for (int i = 0; i < parseArray.size(); i++) {
            lowValues.add(Double.parseDouble(parseArray.get(i).get("Low")));
        }

        Double[] returnList = lowValues.toArray(new Double[lowValues.size()]);
        return returnList;

    }

    public Double[] getLow(int start, int end){
        ArrayList<Double> lowValues = new ArrayList<Double>();
        for (int i = start; i <= end; i++) {
            lowValues.add(Double.parseDouble((minute_bars.get(i).get("Low"))));
        }


        Double[] returnList = lowValues.toArray(new Double[lowValues.size()]);
        return returnList;
    }



    //close getters

    public Double[] getClose(){
        ArrayList<Double> closeValues = new ArrayList<Double>();
        for (int i = 0; i < minute_bars.size(); i++) {
            closeValues.add(Double.parseDouble(minute_bars.get(i).get("Close")));
        }
        Double[] returnList = closeValues.toArray(new Double[closeValues.size()]);
        return returnList;
    }

    public  Double[] getClose(String startDate, String endDate){

        long start = toUnixtime(startDate);
        long end = toUnixtime(endDate);
        ArrayList<Double> closeValues = new ArrayList<Double>();
        ArrayList<HashMap<String,String>> parseArray = parse(start,end);
        for (int i = 0; i < parseArray.size(); i++) {
            closeValues.add(Double.parseDouble(parseArray.get(i).get("close")));
        }

        Double[] returnList = closeValues.toArray(new Double[closeValues.size()]);
        return returnList;

    }


    public Double[] getClose(int start, int end){
        ArrayList<Double> closeValues = new ArrayList<Double>();
        for (int i = start; i <= end; i++) {
            closeValues.add(Double.parseDouble((minute_bars.get(i).get("close"))));
        }


        Double[] returnList = closeValues.toArray(new Double[closeValues.size()]);
        return returnList;
    }

    //Volume_(BTC) getters

    public Double[] getVolume_BTC(){
        ArrayList<Double> volume_BTCValues = new ArrayList<Double>();
        for (int i = 0; i < minute_bars.size(); i++) {
            volume_BTCValues.add(Double.parseDouble(minute_bars.get(i).get("Volume_(BTC)")));
        }
        Double[] returnList = volume_BTCValues.toArray(new Double[volume_BTCValues.size()]);
        return returnList;
    }


    public  Double[] getVolume_BTC(String startDate, String endDate){

        long start = toUnixtime(startDate);
        long end = toUnixtime(endDate);
        ArrayList<Double> volume_BTCValues = new ArrayList<Double>();
        ArrayList<HashMap<String,String>> parseArray = parse(start,end);
        for (int i = 0; i < parseArray.size(); i++) {
            volume_BTCValues.add(Double.parseDouble(parseArray.get(i).get("Volume_(BTC)")));
        }

        Double[] returnList = volume_BTCValues.toArray(new Double[volume_BTCValues.size()]);
        return returnList;

    }

    public Double[] getVolume_BTC(int start, int end){
        ArrayList<Double> volume_BTCValues = new ArrayList<Double>();
        for (int i = start; i <= end; i++) {
            volume_BTCValues.add(Double.parseDouble((minute_bars.get(i).get("Volume_(BTC)"))));
        }


        Double[] returnList = volume_BTCValues.toArray(new Double[volume_BTCValues.size()]);
        return returnList;
    }

    //Volume_(currency) getters

    public Double[] getVolume_Currency(){
        ArrayList<Double> volume_CurrencyValues = new ArrayList<Double>();
        for (int i = 0; i < minute_bars.size(); i++) {
            volume_CurrencyValues.add(Double.parseDouble(minute_bars.get(i).get("Volume_(currency)")));
        }
        Double[] returnList = volume_CurrencyValues.toArray(new Double[volume_CurrencyValues.size()]);
        return returnList;
    }


    public  Double[] getVolume_Currency(String startDate, String endDate){

        long start = toUnixtime(startDate);
        long end = toUnixtime(endDate);
        ArrayList<Double> volume_CurrencyValues = new ArrayList<Double>();
        ArrayList<HashMap<String,String>> parseArray = parse(start,end);
        for (int i = 0; i < parseArray.size(); i++) {
            volume_CurrencyValues.add(Double.parseDouble(parseArray.get(i).get("Volume_(currency)")));
        }

        Double[] returnList = volume_CurrencyValues.toArray(new Double[volume_CurrencyValues.size()]);
        return returnList;

    }

    public Double[] getVolume_Currency(int start, int end){
        ArrayList<Double> volume_CurrencyValues = new ArrayList<Double>();
        for (int i = start; i <= end; i++) {
            volume_CurrencyValues.add(Double.parseDouble((minute_bars.get(i).get("Volume_(currency)"))));
        }


        Double[] returnList = volume_CurrencyValues.toArray(new Double[volume_CurrencyValues.size()]);
        return returnList;
    }


    //Time conversion

    public long toUnixtime(String time){
        //Time is always at midnight UTC but that can be changed.
        //Time zone is UTC
        //changed return type to a long
        //format yyyy-MM-dd HH:mm:ss
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        try {
            Date date = format.parse(time + "-0000");
            return (long) date.getTime()/1000;
        }catch(ParseException e) {
            System.out.println("Format: yyyy-MM-dd HH:mm:ss");
        }
        return 0;
    }

    public String fromUnixtime(long i) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = Date.from( Instant.ofEpochSecond(i));
        System.out.println(format.format(date));
        return format.format(date);
    }
}