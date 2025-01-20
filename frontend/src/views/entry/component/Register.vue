<template>
  <a-spin :loading="isLoading" tip="正在发送请求" class="w-full">
    <a-form :model="data" class="p-4" :rules="rules" ref="formRef">
      <a-form-item field="email" hide-label validate-trigger="blur">
        <a-input v-model.trim="data.email" placeholder="请输入邮箱" allow-clear size="large">
          <template #prepend>
            <icon-email />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item field="emailCheckCode" hide-label>
        <a-input v-model.trim="data.emailCheckCode" placeholder="请输入邮箱验证码" allow-clear size="large">
          <template #prepend>
            <icon-send />
          </template>
        </a-input>
        <a-button type="primary" @click="getEmailCheckCode(data.email,0)" v-if="showEmailBtn"
                  :disabled="restSeconds>0"
                  style="height: 36px; min-width: 100px">{{ restSeconds > 0 ? restSeconds + 's' : '发送验证码'
          }}
        </a-button>
      </a-form-item>
      <a-form-item field="nickname" hide-label alidate-trigger="blur">
        <a-input v-model.trim="data.nickname" placeholder="请输入昵称" allow-clear size="large">
          <template #prepend>
            <icon-user />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item field="password" hide-label validate-trigger="blur">
        <a-input-password v-model.trim="data.password" placeholder="请输入密码" allow-clear size="large">
          <template #prepend>
            <icon-lock />
          </template>
        </a-input-password>
      </a-form-item>
      <a-form-item field="captcha" hide-label>
        <a-input v-model.trim="data.captcha" placeholder="请输入验证码" allow-clear size="large">
          <template #prepend>
            <img :src="Captcha" width="14" alt="" />
          </template>
          <template #append>
            <slot name="captcha"></slot>
          </template>
        </a-input>
      </a-form-item>
      <slot name="bottom" :submit="submit"></slot>
    </a-form>
  </a-spin>
</template>

<script setup lang="ts">
import { reqRegister } from '@/api/entry';
import Captcha from '@/assets/image/captcha.svg';
import { Message } from '@arco-design/web-vue';
import {
  IconEmail,
  IconLock,
  IconSend,
  IconUser
} from '@arco-design/web-vue/es/icon';
import { getRules } from '../validate';
import { useUserStore } from '@/stores';
import type { RegisterDTO } from '@/types/entry';
import useEmailCheckCode from '@/composable/useEmailCheckCode';

const emits = defineEmits<{
  getCaptcha: []
}>();
const router = useRouter();
const isLoading = defineModel(); // 是否加载中
const { userInfo } = storeToRefs(useUserStore());

// 整体数据
const data = ref<RegisterDTO>({} as RegisterDTO);
// 验证规则
const rules = computed(() => getRules(data.value.password));
// 发送邮箱验证码
const { restSeconds, getEmailCheckCode, formRef, showEmailBtn, isSending } = useEmailCheckCode();
// 提交
const submit = async () => {
  // 校验表单
  let pass = true;
  await formRef.value?.validate(err => {
    if (err) pass = false;
  });
  if (!pass) return;
  isLoading.value = true;
  try {
    const resp = await reqRegister(data.value);
    if (resp.code === 200) {
      userInfo.value = resp.data!;
      Message.success('注册成功');
      await router.push('/file/all');
    } else {
      Message.error(resp.message);
      emits('getCaptcha');
    }
  } catch (e) {
    Message.error('注册失败');
    emits('getCaptcha');
  } finally {
    isLoading.value = false;
  }
};
</script>

