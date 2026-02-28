<template>
  <div class="node-list">
    <div class="full-node">
      <h2 class="title">Full Node</h2>
      <div class="list-item">
        <div>
          <div class="sub-title">HTTP:</div>
          <span>- 47.252.85.90:8090</span>
          <span>- 47.252.80.185:8090</span>
          <span>- 47.252.84.141:8090</span>
        </div>
        <div>
          <div class="sub-title">GRPC:</div>
          <span>- 47.252.85.90:50051</span>
          <span>- 47.252.80.185:50051</span>
          <span>- 47.252.84.141:50051</span>
        </div>
      </div>
    </div>
    <div class="solidity-node">
      <h2 class="title">Solidity Node</h2>
      <div class="list-item">
        <div>
          <div class="sub-title">HTTP:</div>
          <span>- 47.252.85.90:8091</span>
          <span>- 47.252.80.185:8091</span>
          <span>- 47.252.84.141:8091</span>
        </div>
        <div>
          <div class="sub-title">GRPC:</div>
          <span>- 47.252.85.90:50061</span>
          <span>- 47.252.80.185:50061</span>
          <span>- 47.252.84.141:50061</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'nodeList'
};
</script>

<style lang="stylus" scoped>
.node-list {
  .list-item {
    display: flex;
    justify-content: space-evenly;
    padding: 0 20px;

    span {
      display: block;
      padding: 0 20px 10px;
      color: #4e6e8e;
    }
  }

  .title {
    font-weight: 500;
    font-size: 1.6rem;
    color: #3a5169;
  }

  .sub-title {
    margin-top: 10px;
    margin-bottom: 15px;
    font-size: 1.2rem;
  }
}
</style>