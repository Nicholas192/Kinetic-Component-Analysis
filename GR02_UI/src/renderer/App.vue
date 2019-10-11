<template>
  <charts></charts>
</template>

<script>
  import WelcomePage from './components/WelcomePage/WelcomePage'
  import Navbar from "./components/Navbar/Navbar";
  import Charts from "./components/Charts/Charts";
  import Sidebar from "./components/Sidebar/Sidebar";
  export default {
    name:'app',
    components: {Sidebar, Charts, Navbar, WelcomePage},
    data(){
      return{
        text: ''
      }
    }
  };
</script>

<style>
</style>
