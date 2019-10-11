<template>
  <div class="container is-fluid">
    <section class="hero is-dark is-small">
      <div class="hero-body">
        <div class="container">
          <h2 class="title">
            BTC/USD Price Chart
          </h2>

          <!-- OPTIONS BAR -->
          <h3 class="subtitle">
            <div class="columns">

              <!-- Start Date -->
              <div class="column">
                <div class="field" style="position:relative; margin-top:-20px">
                  <label class="label">Name</label>
                  <div class="control">
                    <input class="input" v-model="sample_start_date" type="text">
                  </div>
                </div>
              </div>

              <!-- End Date -->
              <div class="column">
                <div class="field" style="position:relative; margin-top:-20px">
                  <label class="label">Name</label>
                  <div class="control">
                    <input class="input" v-model="sample_end_date" type="text">
                  </div>
                </div>
              </div>

              <!-- interval sample dropdown -->
              <div class="column">
                <div class="dropdown is-hoverable">
                  <div class="dropdown-trigger">
                    <button class="button is-dark">
                      <span>{{selected_sample_rate}}</span>
                      <span class="icon is-small">
                    <i class="fas fa-angle-down"></i>
                  </span>
                    </button>
                  </div>
                  <div class="dropdown-menu">
                    <div class="dropdown-content">
                      <a v-for="rate in sample_rates" v-on:click="selected_sample_rate=rate" class="dropdown-item" style="color:#4a4a4a">
                        {{rate}}
                      </a>
                    </div>
                  </div>
                </div>
              </div>

              <!-- KCA OPTIONS -->
              <div class="column">
                <div class="control">
                  <label class="checkbox">
                    <input v-model="isKCA" type="checkbox">
                    Enable KCA {{isKCA}}
                  </label>
                </div>
              </div>
              <div class="column">
                <div class="field" style="position:relative; margin-top:-20px">
                  <label class="label"></label>
                  <div class="control">
                    <input class="input" v-model="KCA_q" type="text">
                  </div>
                </div>
              </div>
              <div class="column">
                <a v-on:click="createD3FCchart()" class="button">Resample</a>
              </div>
            </div>
          </h3>
        </div>
      </div>
    </section>
    <div>
      <div id="chart" style="height:500px;"></div>
    </div>
    {{KCA_predictions}}
  </div>
</template>

<script>
  import axios from 'axios'
  import timestamp from 'unix-timestamp'
  import * as d3 from 'd3'
  import {
    extentLinear
    , extentDate
    , feedGdax
    , randomFinancial
    , annotationSvgGridline
    , seriesSvgMulti
    , seriesSvgLine
    , chartCanvasCartesian
    , scaleDiscontinuous
    ,seriesSvgCandlestick
    , chartSvgCartesian
  } from 'd3fc';

  export default {
    name: 'Charts',
    data: function () {
      return {
        text: '',
        isKCA: false,
        KCA_q: 0.5,
        KCA_fwd: 5,
        KCA_DATA: [],
        BARS_DATA : [],
        sample_rates: ['1m', '5m', '15m', '1h', '6h', '1d', '7d', '30d'],
        selected_sample_rate: '15m',
        sample_start_date: "2017-01-20 00:00:00",
        sample_end_date: "2017-01-21 00:00:00",
        prices: [],
        KCA_predictions:[],
      }
    },
    methods: {
      createD3FCchart:async function(){
        // Chart creation method.
        // all values are pulled from global state. icky and gross but it works.
        var data = await this.getPricesIntervalBars(); //The functions querys the state.
        var kca_data = await this.getKCAValues() //array of arrays [[pos, vel, acc]]
        console.log(data)
        let i = 0
        for(var bar of data){
          bar["date"] = new Date(bar["date"] * 1000)
          bar["kca_x"] = kca_data[i][0]
          i++
        }
        this.KCA_predictions = kca_data.splice(i,kca_data.length)
        var yExtent = extentLinear()
          .accessors([
            function(d) {return d.high; },
            function(d) {return d.low; }
          ]);

        var xExtent = extentDate()
          .accessors([function(d) {return d.date; }]);

        var gridlines = annotationSvgGridline();
        var candlestick = seriesSvgCandlestick();
        var kca = seriesSvgLine().mainValue(d => d.kca_x).crossValue(d => d.date)
        var multi = seriesSvgMulti()
          .series([gridlines, candlestick, kca]);

        var chart = chartSvgCartesian(
          scaleDiscontinuous(d3.scaleTime()),
          d3.scaleLinear()
        )
          .yDomain(yExtent(data))
          .xDomain(xExtent(data))
          .plotArea(multi)


        d3.select('#chart')
          .datum(data)
          .call(chart);
      },

      getPricesIntervalBars: async function () {
        try {
          const URL = "http://localhost:8000/intervalbars"
          const response = await axios.get(URL, {
            params: {
              start: this.sample_start_date,
              end: this.sample_end_date,
              interval: this.selected_sample_rate
            }
          })
          // Response:
          // [
          //   {
          //     date: 2016-01-01T00:00:00.000Z,
          //     open: 100,
          //     high: 100.37497903455065,
          //     low: 99.9344064016257,
          //     close: 100.13532170178823,
          //     volume: 974
          //   },
          //   {
          //     date: 2016-01-04T00:00:00.000Z,
          //     open: 100.2078374019404,
          //     high: 100.55251268471399,
          //     low: 99.7272105851512,
          //     close: 99.7272105851512,
          //     volume: 992
          //   }
          // ]
          return response.data
        } catch(e){
          console.error("Error in request: ", e)
        }
      },

      getKCAValues: async function () {
        try {
          const URL = "http://localhost:8000/KCA"
          const response = await axios.get(URL, {
            params: {
              start: this.sample_start_date,
              end: this.sample_end_date,
              interval: this.selected_sample_rate,
              q: this.KCA_q,
              fwd: 5,
            }
          })
          // Response:
          // [

          // ]
          return response.data
        } catch(e){
          console.error("Error in request: ", e)
        }
      },


      getPrices: async function () {
        try {
          const response = await axios.get('http://localhost:8000/prices?start=250000&end=251000')
          //[{"Close": "222.33", "Timestamp":"1429313220"}, {
          this.prices = []
          for (var t of response.data) {
            console.log(t["Close"])
            this.chartData.data.datasets[0].data.push(parseFloat(t["Close"]))
            this.chartData.data.labels.push(parseFloat(t["Timestamp"]))
          }
          this.createChart('myChart', this.chartData)
          console.log(this.chartData)
        } catch (e) {
          console.error(e)
        }
      }
    },
  }
</script>
