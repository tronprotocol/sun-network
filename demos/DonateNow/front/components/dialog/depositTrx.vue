 <template>
    <el-dialog :title="params.title" :visible.sync="params.show" append-to-body :close-on-click-modal="false"
    width="5.8rem"
    custom-class="how-dialog play-dialog custom-dg">
        <el-form :model="form" size="small" :rules="formRule" ref="form">
            <el-form-item :label="$t('DepositNum')" :label-width="formLabelWidth" prop="num">
                <el-input v-model.trim="form.num"></el-input>
            </el-form-item>
            <el-form-item :label="$t('FeeLimit')" :label-width="formLabelWidth" prop="num">
                <el-input v-model.trim="form.feeLimit"></el-input>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="hide" size="small">{{$t('cancel')}}</el-button>
            <el-button type="primary" @click="confirm" size="small">{{$t('Confirm')}}</el-button>
        </div>
    </el-dialog>
</template>
<script type="text/javascript">
    export default {
        props: ['params'],
        data() {
            return {
                form: {
                    num: 10,
                    feeLimit: 1000
                },
                formLabelWidth: '100px',
                formRule: this.initRules()
            };
        },
        methods: {
            initRules() {
                const validateBudget = (rule, value, callback) => {
                    if (this.form.type == '1') {
                        let reg = /^\d+(.\d{1,2})?$/;
                        if (this.params.level === 'account') {
                            reg = /^\d+$/;
                        }
                        if (!reg.test(value) || value < 100 || value > 1000000) {
                            callback(new Error('error'));
                        }
                        else {
                            callback();
                        }
                    }
                    else {
                        callback();
                    }
                };
                return {
                    value: [
                        { validator: validateBudget, trigger: 'change' }
                    ]
                };
            },
            hide() {
                this.params.show = false;
            },
            confirm() {
                this.$refs.form.validate(valid => {
                    if (!valid) {
                        return;
                    }
                    this.params.show = false;
                    this.params.confirm({
                        num: this.form.num,
                        feeLimit: this.form.feeLimit
                    });
                });
            }
        }
    };
</script>
