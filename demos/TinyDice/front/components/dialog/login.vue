 <template>
    <el-dialog title="" :visible.sync="params.show" append-to-body :close-on-click-modal="false">
        <el-form :model="form" size="small" ref="form">
            <el-form-item label="Private Key" :label-width="formLabelWidth" prop="privateKey">
               <el-input size="small" v-model.trim="form.privateKey" clearable></el-input>
            </el-form-item>
        </el-form>
        <div style="text-align:center;">Tronlink will support soon...</div>
        <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="confirm" size="small" :disabled="form.privateKey == ''">{{$t('SIGNIN')}}</el-button>
        </div>
    </el-dialog>
</template>
<script type="text/javascript">
    export default {
        props: ['params'],
        data() {
            return {
                form: {
                    privateKey: ''

                },
                formLabelWidth: '100px'
            };
        },
        methods: {
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
                        privateKey: this.form.privateKey
                    });
                });
            }
        }
    };
</script>
