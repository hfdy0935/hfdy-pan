<template>
  <a-row class="background" justify="center" align="center">
    <a-col :xs="24" :lg="10" class="col">
      <div class="img"></div>
    </a-col>
    <a-col :xs="24" :lg="14" class="col">
      <a-card class="card pt-6">
        <template #title>
          <div class="title">hfdy 云盘</div>
        </template>
        <Login v-if="op===EntryOp.LOGIN" v-model="isLoading">
          <template #captcha>
            <img :src="captchaUrl" class="w-32 cursor-pointer select-none" @click="getCaptcha" alt="验证码" />
          </template>
          <template #bottom="{submit}">
            <a-form-item hide-label>
              <a-checkbox v-model="rememberMe" style="user-select: none">记住我</a-checkbox>
            </a-form-item>
            <a-form-item hide-label>
              <div class="w-full flex justify-between select-none">
                <a-link :hoverable="false" @click="op=EntryOp.REGISTER">没有账号？去注册</a-link>
                <a-link :hoverable="false" @click="op=EntryOp.UPDATE_PASSWORD">忘记密码？</a-link>
              </div>
            </a-form-item>
            <a-form-item hide-label>
              <a-button class="submit-button" type="primary" :disabled="isLoading" @click="submit">{{ op }}
              </a-button>
            </a-form-item>
          </template>
        </Login>
        <Register v-if="op===EntryOp.REGISTER" v-model="isLoading" @getCaptcha="getCaptcha">
          <template #captcha>
            <img :src="captchaUrl" class="w-32 cursor-pointer select-none" @click="getCaptcha" alt="验证码" />
          </template>
          <template #bottom="{submit}">
            <a-form-item hide-label>
              <a-checkbox v-model="rememberMe" style="user-select: none">记住我</a-checkbox>
            </a-form-item>
            <a-form-item hide-label>
              <div class="w-full flex justify-between select-none">
                <a-link :hoverable="false" @click="op=EntryOp.UPDATE_PASSWORD">忘记密码？</a-link>
                <a-link :hoverable="false" @click="op=EntryOp.LOGIN">已有帐号？去登录</a-link>
              </div>
            </a-form-item>
            <a-form-item hide-label>
              <a-button class="submit-button" type="primary" :disabled="isLoading" @click="submit">{{ op }}
              </a-button>
            </a-form-item>
          </template>
        </Register>
        <update-password v-if="op===EntryOp.UPDATE_PASSWORD" v-model="isLoading" @getCaptcha="getCaptcha">
          <template #captcha>
            <img :src="captchaUrl" class="w-32 cursor-pointer select-none" @click="getCaptcha" alt="验证码" />
          </template>
          <template #bottom="{submit}">
            <a-form-item hide-label>
              <a-checkbox v-model="rememberMe" style="user-select: none">记住我</a-checkbox>
            </a-form-item>
            <a-form-item hide-label>
              <div class="w-full flex justify-between select-none">
                <a-link :hoverable="false" @click="op=EntryOp.LOGIN">已有帐号？去登录</a-link>
                <a-link :hoverable="false" @click="op=EntryOp.REGISTER">没有账号？去注册</a-link>
              </div>
            </a-form-item>
            <a-form-item hide-label>
              <a-button class="submit-button" type="primary" :disabled="isLoading" @click="submit">{{ op }}
              </a-button>
            </a-form-item>
          </template>
        </update-password>
      </a-card>
    </a-col>
  </a-row>
</template>


<script setup lang="ts">
import { EntryOp } from '@/constants';
import Login from './component/Login.vue';
import { reqGetCaptcha } from '@/api/entry';
import { Message } from '@arco-design/web-vue';
import Register from '@/views/entry/component/Register.vue';
import UpdatePassword from '@/views/entry/component/UpdatePassword.vue';

defineOptions({
  name: 'Entry'
});

/**
 * 操作类型
 */
const op = ref<EntryOp>(EntryOp.LOGIN);
const rememberMe = ref(false); // 是否勾选记住我
const isLoading = ref(false);
const captchaUrl = ref<string>(''); // 图形l验证码链接
/**
 * 获取图形验证码
 */
const getCaptcha = async () => {
  try {
    const res = await reqGetCaptcha();
    captchaUrl.value = URL.createObjectURL(res);
  } catch {
    Message.error('验证码获取失败，请稍后再试');
  }
};
watch(op, getCaptcha, {
  immediate: true
});
</script>


<style scoped lang="scss">
.background {
  width: 100%;
  height: 100%;
  background: url(@/assets/image/login_bg.png) no-repeat;
  background-size: cover;
  padding: 20px;

  .col {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 10px;

    .img {
      width: 400px;
      height: 400px;
      background: url(@/assets/image/login.img.png) no-repeat;
      background-size: contain;
    }

    @media (max-width: 992px) {
      .img {
        margin-bottom: -200px;
      }
    }

    .card {
      width: 100%;
      max-width: 600px;
      height: 600px;

      .title {
        text-align: center;
        font-weight: bold;
        font-size: 20px;
      }
    }
  }
}

:deep(.submit-button) {
  width: 100%;
  height: 50px;
}
</style>
