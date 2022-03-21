<template>
    <div class="navBar">
        <ul>
            <section>
                <li style="float:left">
                    <a :href="$store.state.homeUrl">
                        <img src="../assets/tronBanner.svg" alt="Tron" />
                    </a>
                </li>
                <!-- <li>
        <el-dropdown>
          <a>English</a>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item>English</el-dropdown-item>
            <el-dropdown-item>中文</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        </li>-->
                <li class="nav-menu">
                    <div @click="demos">
                        <a>Resources</a>
                    </div>
                </li>
                <li class="nav-menu">
                    <a :href="$store.state.docUrl">Documents</a>
                </li>
                <li class="nav-menu">
                    <a :href="$store.state.testNetsUrl">TestNets</a>
                </li>
                <li class="nav-menu">
                    <div @click="mainNet">
                        <a>MainNets</a>
                    </div>
                </li>
            </section>
        </ul>
    </div>
</template>

<script>
export default {
    name: 'NavBar',
    methods: {
        mainNet: function() {
            this.$message('Coming soon');
        },
        demos: function() {
            var elmnt = document.getElementById('demos');
            elmnt.scrollIntoView({
                behavior: 'smooth'
            });
        }
    }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
.navBar {
    * {
        font-size: 16px;
    }
    ul {
        list-style-type: none;
        margin: 0;
        padding: 0 100px;
        overflow: hidden;
        border-bottom: 1px solid rgb(216, 216, 216);
        @media (max-width: 600px) {
            padding: 0 30px;
        }
    }

    li {
        float: right;
        &.nav-menu {
            @media (max-width: 600px) {
                display: none;
            }
        }
    }

    li a {
        display: block;
        color: #111;
        text-align: center;
        padding: 14px 16px;
        text-decoration: none;
        line-height: 30px;
        height: 30px;
        img {
            height: 30px;
        }
    }

    li a:hover:not(.active) {
        color: #f56c6c;
    }

    .active {
        background-color: #4caf50;
    }
}
</style>
