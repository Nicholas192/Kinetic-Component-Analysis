<!-- navbar is on the top of the chart page-->
<template>
  <nav class="navbar">
    <div class="navbar-brand">
      <a class="navbar-item">
        <h1 class="title">
          KCA
        </h1>
      </a>
    </div>
    <div class="navbar-menu is-active">
      <div class="navbar-start">
        <div class="navbar-item has-dropdown is-hoverable">
          <div class="navbar-link">
            BTC/USD {{current_price}}
          </div>
          <div class="navbar-dropdown">
            <a class="navbar-item">
              Overview
            </a>
            <a class="navbar-item">
              Elements
            </a>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
  export default {
    name: "Navbar",
    data(){
      return{
        current_prices: 3000.00
      }
    }
  }
</script>

<style scoped>

</style>