<template>
  <nav class="panel">
    <p class="panel-heading">
      Sources
    </p>
  <label class="panel-block">
    <input type="checkbox">
      BTC/USD
  </label>
    <label class="panel-block">
      <div class="control">
        <label class="radio">
          <input type="radio" name="answer">
          1m
        </label>
        <label class="radio">
          <input type="radio" name="answer">
          5m
        </label>
      </div>
    </label>
  </nav>
</template>

<script>
  export default {
    name: "Sidebar"
  }
</script>

<style scoped>

</style>