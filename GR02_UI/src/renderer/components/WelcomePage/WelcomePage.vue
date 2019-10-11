<template>
  <div>
    <section class="hero is-primary">
      <div class="hero-body">
        <div class="container">
          <h1 class="title">
            Kinetic Component Analysis
          </h1>
          <h2 class="subtitle">
            Welcome
          </h2>
        </div>
      </div>
    </section>
        <div class="columns">
        </div>
            <a class="button is-centered is-primary" v-on:click="buttonClick">
              Click Here {{msg}}
            </a>
  </div>
</template>

<script>
  export default {
    data(){
      return{
        msg: "not clicked"
      }
    },
    methods: {
      buttonClick: function(){
        this.msg="clicked"
      }
    }
  }
</script>