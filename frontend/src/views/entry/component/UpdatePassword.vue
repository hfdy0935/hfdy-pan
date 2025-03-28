<template>
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
      <a-button type="primary" @click="getEmailCheckCode(data.email, 1)" v-if="showEmailBtn" :disabled="restSeconds > 0"
        style="height: 36px; min-width: 100px">{{ restSeconds > 0 ? restSeconds + 's' : '发送验证码'
        }}
      </a-button>
    </a-form-item>
    <a-form-item field="password" hide-label validate-trigger="blur">
      <a-input-password v-model.trim="data.password" placeholder="请输入密码" allow-clear size="large">
        <template #prepend>
          <icon-lock />
        </template>
      </a-input-password>
    </a-form-item>
    <a-form-item field="confirmPassword" hide-label validate-trigger="blur">
      <a-input-password v-model.trim="data.confirmPassword" placeholder="请确认密码" allow-clear size="large">
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
</template>

<script setup lang="ts">
import Captcha from '@/assets/image/captcha.svg';
import type { IUpdatePassword } from '@/types/entry';
import useEmailCheckCode from '@/composable/useEmailCheckCode';
import { getRules } from '@/views/entry/validate';
import { Form, Message } from '@arco-design/web-vue';
import { reqUpdatePassword } from '@/api/entry';
import {
  IconEmail,
  IconLock,
  IconSend,
} from '@arco-design/web-vue/es/icon';

defineOptions({
  name: 'UpdatePassword'
});

const emits = defineEmits<{
  getCaptcha: []
}>();
const isLoading = defineModel(); // 是否加载中
const data = ref<IUpdatePassword>({} as IUpdatePassword);
// 验证规则
const rules = computed(() => getRules(data.value.password));
const { restSeconds, getEmailCheckCode, formRef, showEmailBtn } = useEmailCheckCode();


const submit = async () => {
  // 校验表单
  let pass = true;
  await formRef.value?.validate(err => {
    if (err) pass = false;
  });
  if (!pass) return;
  isLoading.value = true;
  try {
    const { confirmPassword, ...target } = data.value;
    const resp = await reqUpdatePassword(target);
    if (resp.code === 200) {
      Message.success('修改成功');
      formRef.value!.resetFields();
    } else {
      Message.error(resp.message);
      emits('getCaptcha');
    }
  } catch (e) {
    emits('getCaptcha');
  } finally {
    isLoading.value = false;
  }
};
</script>
