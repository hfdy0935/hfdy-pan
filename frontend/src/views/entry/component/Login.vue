<template>
  <a-form :model="data" class="p-4" :rules="rules" ref="form">
    <a-form-item field="email" hide-label validate-trigger="blur">
      <a-input v-model.trim="data.email" placeholder="请输入邮箱" allow-clear size="large">
        <template #prepend>
          <icon-email />
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
          <!-- 没找到验证码类似的图标，找了个iconfont的svg代替了 -->
          <img :src="Captcha" width="14" alt="" />
        </template>
        <template #append>
          <slot name="captcha"></slot>
        </template>
      </a-input>
    </a-form-item>
    <slot name="bottom" :submit></slot>
  </a-form>
</template>

<script setup lang="ts">
import { reqLogin } from '@/api/entry';
import Captcha from '@/assets/image/captcha.svg';
import { Form, Message } from '@arco-design/web-vue';
import { IconEmail, IconLock } from '@arco-design/web-vue/es/icon';
import { getRules } from '../validate';
import type { LoginDTO } from '@/types/entry';
import { useUserStore } from '@/stores/modules/user';

const router = useRouter();
const isLoading = defineModel();
const { userInfo } = storeToRefs(useUserStore());
const formRef = useTemplateRef<InstanceType<typeof Form> | null>('form')
const data = ref<LoginDTO>({} as LoginDTO); // 登录表单数据
const rules = computed(() => getRules(data.value.password));
/**
 * 提交表单
 */
const submit = async () => {
  // 校验表单
  let pass = true;
  await formRef.value?.validate(err => {
    if (err) pass = false;
  });
  if (!pass) return;
  isLoading.value = true;
  try {
    const resp = await reqLogin(data.value);
    if (resp.code === 200) {
      userInfo.value = resp.data!;
      Message.success('登录成功');
      await router.push('/file/all');
    } else Message.error(resp.message);
  } finally {
    isLoading.value = false;
  }
};
</script>
