<template>
    <el-container id="page-login">
        <el-main>

            <el-form id="form-login" label-position="top">
                <el-form-item>
                    <el-input v-model="loginForm.username" placeholder="用户名"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-input v-model="loginForm.password" type="password" placeholder="密码"></el-input>
                </el-form-item>
                <el-form-item id="verify-code-item">
                    <el-image src="http://tencent.github.io/weui/images/vcode.jpg" class="verify-code-image"></el-image>
                    <el-input v-model="loginForm.verifyCode" placeholder="请输入左侧验证码" class="verify-code-input"></el-input>
                </el-form-item>
            </el-form>

            <div class="login-btn-group">
                <el-button type="primary" class="login-btn" @click="login">登录</el-button>
                <el-link href="/register" type="primary" class="register-link">还没有账号？去注册</el-link>
            </div>

        </el-main>
    </el-container>
</template>


<script lang="ts">
import { useRouter } from "vue-router";
import { defineComponent } from "vue";

export default defineComponent({
    data() {
        return {
            loginForm: {
                username: '',
                password: '',
                verifyCode: ''
            }
        }
    },
    methods: {
        login() {
            this.$router.push({
                path: '/home'
            })
        }
    }
});
</script>


<style lang="scss">
#page-login {
    height: 100%;
    justify-content: center;
    align-items: center;

    .el-main {
        width: 40%;
        max-width: 300px;

        #verify-code-item {
            .verify-code-image {
                height: 32px;
                width: 100px;
            }
            .verify-code-input {
                width: 160px;

                .el-input__inner {
                    border-radius: 0 5px 5px 0 !important;
                }
            }
        }
    }

    .login-btn-group {
        display: flex;
        flex-direction: column;
        align-items: center;
        .login-btn {
            width: 200px;
        }
        .register-link {
            margin-top: 10px;
            font-size: .4em;
            font-weight: 300;
        }
    }
}
</style>