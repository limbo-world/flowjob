<template>
    <el-container id="page-register">
        <el-main>

            <el-form id="form-register" label-position="top">
                <el-form-item>
                    <el-input v-model="registerForm.username" placeholder="用户名"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-input v-model="registerForm.password" type="password" placeholder="密码"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-input v-model="registerForm.passwordConfirm" type="password" placeholder="再次确认密码"></el-input>
                </el-form-item>
                <el-form-item id="verify-code-item">
                    <el-image src="http://tencent.github.io/weui/images/vcode.jpg" class="verify-code-image"></el-image>
                    <el-input v-model="registerForm.verifyCode" placeholder="请输入左侧验证码" class="verify-code-input"></el-input>
                </el-form-item>
            </el-form>

            <div class="register-btn-group">
                <el-button type="primary" class="register-btn">注册</el-button>
                <el-link href="/login" type="primary" class="login-link">已有账号？去登录</el-link>
            </div>

        </el-main>
    </el-container>
</template>

<script setup lang="ts">
import {ref} from 'vue'

defineProps<{ msg: string }>()

const registerForm = ref({
    username: '',
    password: '',
    passwordConfirm: '',
    verifyCode: ''
});
</script>

<style lang="scss">
#page-register {
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

    .register-btn-group {
        display: flex;
        flex-direction: column;
        align-items: center;
        .register-btn {
            width: 200px;
        }
        .login-link {
            margin-top: 10px;
            font-size: .4em;
            font-weight: 300;
        }
    }
}
</style>