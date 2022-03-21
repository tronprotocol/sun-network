<template>
    <div class="main">
        <div class="block introduce-main">
            <!-- <section>
        <p class="main-title">DAppChain</p>
        <p class="main-desc">
          Similar environment, Better experience
          <br />Providing unlimited capacity for TRON's main network.
        </p>
        <p class="main-doc">
          <el-button type="danger" @click="learnMore">Learn More</el-button>
        </p>
      </section> -->
            <div class="main-footer">
                <section>
                    <p class="main-footer-title">DAppChain, sidechain of TRON</p>
                    <p class="main-footer-desc">
                        DAppChain is TRON's sidechain project. The goal is to help DApps to operate on TRON with lower
                        energy consumption, faster speed and enhanced safety, providing unlimited capacity for TRON's
                        main network.
                    </p>
                </section>
            </div>
        </div>
        <div class="block introduce-1">
            <section>
                <el-row :gutter="10">
                    <el-col :span="12" :xs="24">
                        <div class="introduce-1-img">
                            <img src="../assets/sidechainl.png" alt="sidechainl" />
                        </div>
                    </el-col>
                    <el-col :span="12" :xs="24">
                        <div class="introduce-1-right">
                            <div class="introduce-1-right-content">
                                <div class="introduce-1-right-title">Compatible with TRON Protocol</div>
                                <div class="introduce-1-right-desc">
                                    The sidechain uses the DPoS consensus, the same as the mainchain. It supports smart
                                    contract and almost all of the functionalities of the mainchain. TRON users can
                                    easily adapt to the sidechain, and developers can quickly migrate or develop new
                                    DApps on the sidechain.
                                </div>
                                <div class="introduce-1-right-title">Highly Customizable</div>
                                <div class="introduce-1-right-desc">
                                    DAppChain is committed to provide all ecosystem participants with a highly flexible,
                                    well-configured, and highly customized network of multiple sidechains. This is also
                                    one of the characteristics of each sidechain in DAppChain that is different from the
                                    main chain.
                                </div>
                            </div>
                        </div>
                    </el-col>
                </el-row>
            </section>
        </div>
        <div class="block introduce-2">
            <section>
                <el-row :gutter="10">
                    <el-col :span="12" :xs="24">
                        <div class="introduce-2-left">
                            <div class="introduce-2-left-content">
                                <div class="introduce-2-left-title">Lower Resource Cost</div>
                                <div class="introduce-2-left-desc">
                                    The resource consumption on the sidechain can be much lower. For a same transaction,
                                    the cost of resources consumed is only a fraction of the cost on the mainchain. This
                                    is great news for DApps that have large transaction volume and it can help contract
                                    developers reduce their energy consumption.
                                </div>
                                <div class="introduce-2-left-title">High Security</div>
                                <div class="introduce-2-left-desc">
                                    Oracle relay-chain and DPoS on a single sidechain can ensure the security of
                                    on-chain/cross-chain transactions for users. Ultimately, the consensus of
                                    mainchain(TRON-network) safeguards the security of all sidechains.
                                </div>
                            </div>
                        </div>
                    </el-col>
                    <el-col :span="12" :xs="24">
                        <div class="introduce-2-img">
                            <img src="../assets/sidechainr.png" alt="sidechainr" />
                        </div>
                    </el-col>
                </el-row>
            </section>
        </div>
        <div class="block cards">
            <section>
                <div class="cards-line">
                    <el-row :gutter="50">
                        <el-col :span="8" :xs="24">
                            <div class="card-div" @click="gotoGithub">
                                <el-card>
                                    <div class="card-content">
                                        <img src="../assets/github.png" alt="github" />
                                        <div class="card-title">Github</div>
                                        <div class="card-desc">Latest changes & version history.</div>
                                    </div>
                                </el-card>
                            </div>
                        </el-col>
                        <el-col :span="8" :xs="24">
                            <div class="card-div" @click="gotoDoc">
                                <el-card>
                                    <div class="card-content">
                                        <img src="../assets/doc.png" alt="doc" />
                                        <div class="card-title">Documentation</div>
                                        <div class="card-desc">Get more from our documentation.</div>
                                    </div>
                                </el-card>
                            </div>
                        </el-col>
                        <el-col :span="8" :xs="24">
                            <div class="card-div" @click="gotoDiscord">
                                <el-card>
                                    <div class="card-content">
                                        <img src="../assets/telegram.png" alt="discord" />
                                        <div class="card-title">Discord</div>
                                        <div class="card-desc">Join the community.</div>
                                    </div>
                                </el-card>
                            </div>
                        </el-col>
                    </el-row>
                </div>
            </section>
        </div>
        <div class="block lower-priced">
            <section>
                <div class="lower-title">
                    <b class="divider"></b>
                    <div class="divider-title">Lower Priced</div>
                    <b class="divider"></b>
                </div>
                <div class="lower-desc">
                    <el-row :gutter="50">
                        <el-col :span="12" :xs="24">
                            <div class="lower-left">
                                <div class="left-title">Same freezing more energy</div>
                                <b class="divider"></b>
                                <div class="left-desc">
                                    On DAppChain, freezing 1 trx gets 10000 energy everyday, 100x of main-chain.
                                </div>
                            </div>
                        </el-col>
                        <el-col :span="12" :xs="24">
                            <div class="lower-right">
                                <div class="right-title">Main-chain</div>
                                <el-progress :percentage="10" color="red"></el-progress>
                                <div class="right-desc">100 free energy for 1 trx everyday</div>
                                <div class="right-title">DAppchain</div>
                                <el-progress :percentage="100" color="red"></el-progress>
                                <div class="right-desc">Get 100x free energy of main-chain</div>
                            </div>
                        </el-col>
                    </el-row>
                </div>
            </section>
        </div>
        <div class="block demos" id="demos">
            <section>
                <div class="demos-title">
                    <b class="divider"></b>
                    <div class="divider-title">Demos</div>
                    <b class="divider"></b>
                </div>
                <div class="demo-cards">
                    <el-row :gutter="50">
                        <el-col :span="12" :xs="24">
                            <div class="demo-card" @click="gotoDemo1">
                                <el-card :body-style="{ padding: '0px' }" shadow="hover">
                                    <img src="../assets/demo1.png" alt="demo1" />
                                    <div style="padding: 14px;">
                                        <div class="title">Demo #1</div>
                                        <div class="desc">
                                            A demo shows how to build a DApp on DAppChain and play it.
                                        </div>
                                    </div>
                                </el-card>
                            </div>
                        </el-col>
                        <el-col :span="12" :xs="24">
                            <div class="demo-card" @click="gotoDemo2">
                                <el-card :body-style="{ padding: '0px' }" shadow="hover">
                                    <img src="../assets/demo2.png" alt="demo2" />
                                    <div style="padding: 14px;">
                                        <div class="title">Demo #2</div>
                                        <div class="desc">
                                            Show how to interact with DAppChain both with js-sdk and java-sdk
                                        </div>
                                    </div>
                                </el-card>
                            </div>
                        </el-col>
                    </el-row>
                </div>
                <p class="demos-doc">
                    <el-button type="danger" @click="gotoGithub">Get Started</el-button>
                </p>
            </section>
        </div>
    </div>
</template>

<script>
export default {
    name: 'Main',
    methods: {
        learnMore: function() {
            var elmnt = document.getElementsByClassName('main-footer');
            elmnt[0].scrollIntoView({
                behavior: 'smooth'
            });
        },
        gotoDoc: function() {
            window.open(this.$store.state.docUrl);
        },
        gotoGithub: function() {
            window.open(this.$store.state.githubUrl);
        },
        gotoDiscord: function() {
            window.open(this.$store.state.discordUrl);
        },
        gotoDemo1: function() {
            window.open(this.$store.state.demo1Url);
        },
        gotoDemo2: function() {
            window.open(this.$store.state.demo2Url);
        }
    }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
.main {
    .block {
        width: 100%;

        &.introduce-main {
            min-height: 200px;
            .main-title {
                font-size: 50px;
                color: #383838;
                padding: 30px 30px 0 30px;
                @media (max-width: 600px) {
                    padding: 80px 50px 0 50px;
                }
            }
            .main-desc {
                font-size: 30px;
                color: #686868;
                padding: 10px 30px 90px 30px;
                @media (max-width: 600px) {
                    padding: 10px 50px 140px 50px;
                }
            }
            .main-doc {
                text-align: center;
                padding-bottom: 100px;
                .el-button {
                    font-size: 30px;
                    font-family: 'Baskerville';
                    background: #ff543d;
                }
            }
            .main-footer {
                background: #ff543d;
                color: white;
                text-align: center;
                padding: 60px 0;
                .main-footer-title {
                    font-size: 34px;
                    padding-top: 15px;
                    font-family: 'Arial-BoldMT';
                }
                .main-footer-desc {
                    margin: 0;
                    margin-top: -20px;
                    font-size: 18px;
                    font-weight: lighter;
                    padding: 0 100px 20px 100px;
                    @media (max-width: 600px) {
                        padding: 0 50px 20px 50px;
                    }
                }
            }
        }
        &.introduce-1 {
            min-height: 600px;
            background: #f7f7f7;
            .introduce-1-img {
                line-height: 600px;
                @media (max-width: 600px) {
                    padding-top: 10px;
                    line-height: 200px;
                }
                img {
                    max-width: 100%;
                    max-height: 100%;
                    vertical-align: middle;
                }
            }
            .introduce-1-right {
                .introduce-1-right-content {
                    padding: 50px;
                    .introduce-1-right-title {
                        font-size: 30px;
                        padding: 20px 0;
                        color: #353535;
                    }
                    .introduce-1-right-desc {
                        font-size: 18px;
                        padding-bottom: 40px;
                        color: #686868;
                        font-weight: lighter;
                    }
                }
            }
        }
        &.introduce-2 {
            min-height: 600px;
            .introduce-2-img {
                line-height: 600px;
                @media (max-width: 600px) {
                    padding: 0 0 30px 0;
                    line-height: 200px;
                }
                img {
                    max-width: 100%;
                    max-height: 100%;
                    vertical-align: middle;
                }
            }
            .introduce-2-left {
                .introduce-2-left-content {
                    padding: 50px;
                    .introduce-2-left-title {
                        font-size: 30px;
                        padding: 20px 0;
                        color: #353535;
                    }
                    .introduce-2-left-desc {
                        font-size: 18px;
                        padding-bottom: 40px;
                        color: #686868;
                        font-weight: lighter;
                        @media (max-width: 600px) {
                            padding-bottom: 0;
                        }
                    }
                }
            }
        }
        &.cards {
            position: flex;
            .cards-line {
                padding: 0 50px;
                .card-div {
                    padding-top: 20px;
                    .card-content {
                        img {
                            width: 50px;
                            float: left;
                            padding: 0 10px 0 0;
                        }
                        .card-title {
                            font-weight: bold;
                            font-size: 20px;
                        }
                        .card-desc {
                            font-size: 14px;
                        }
                    }
                }
            }
        }
        &.lower-priced {
            min-height: 400px;
            background: #f7f7f7;
            margin-top: -45px;
            .lower-title {
                padding: 110px 0 50px 0;
                text-align: center;
            }
            .lower-desc {
                padding: 0 100px 60px 100px;
                font-weight: lighter;
                @media (max-width: 600px) {
                    padding: 0 50px;
                }
                .lower-left {
                    .left-title {
                        font-size: 28px;
                        padding: 20px 0;
                        color: #686868;
                        font-family: 'Copperplate-Bold';
                    }
                    .left-desc {
                        font-size: 18px;
                        color: #8f8f8f;
                        padding: 20px 20px 20px 0;
                        font-weight: lighter;
                    }
                }
                .lower-right {
                    .right-title {
                        font-family: 'Copperplate-Bold';
                        color: #686868;
                        font-size: 28px;
                        padding: 20px 0;
                    }
                    .right-desc {
                        font-size: 18px;
                        color: #8f8f8f;
                        padding: 10px 0;
                        font-weight: lighter;
                    }
                }
            }
        }
        &.demos {
            position: flex;
            min-height: 800px;
            .demos-title {
                padding: 50px 0 20px 0;
                text-align: center;
            }
            .demo-cards {
                padding: 0 150px;

                @media (max-width: 600px) {
                    padding: 0 10px;
                }
                .demo-card {
                    margin: auto;
                    width: 345px;
                    padding-top: 40px;
                }
                .title {
                    font-size: 28px;
                }
                .desc {
                    font-size: 14px;
                    font-family: Arial;
                    color: #999999;
                    padding: 10px 0;
                }
            }
            .demos-doc {
                padding: 50px 0;
                text-align: center;
                .el-button {
                    font-size: 26px;
                    background: #ff543d;
                    font-family: 'Baskerville';
                }
            }
        }
        .divider {
            display: inline-block;
            vertical-align: middle;
            width: 80px;
            border: solid 0.5px #cdcdcd;
        }
        .divider-title {
            display: inline-block;
            vertical-align: middle;
            font-size: 42px;
            padding: 0 25px;
        }
    }
}
</style>
