import { reqGetEmailCheckCodeExpires, reqSendEmailCheckCode } from '@/api/entry';
import { Form, Message } from '@arco-design/web-vue';
import type { Ref } from 'vue';
import { EMAIL_EXP_TIME } from '@/constants';

export default function useEmailCheckCode(): {
  restSeconds: Ref<number>,
  getEmailCheckCode(email: string, type: 0 | 1): void,
  formRef: Ref<InstanceType<typeof Form> | null>,
  showEmailBtn: Ref<boolean>,
  isSending: Ref<boolean>
} {
  const formRef = ref<InstanceType<typeof Form> | null>(null); // 表单ref
  const restSeconds = ref<number>(-1); // 邮箱验证码剩余秒
  const showEmailBtn = ref(false); // 是否显示发送邮件的按钮，在获取到剩余时间之前应该保持为false
  const isSending = ref(false); // 是否正在请求发送验证码
  // 获取一次剩余时间
  watchEffect(async () => {
    try {
      const resp = await reqGetEmailCheckCodeExpires();
      if (resp.code === 200) {
        const time = resp.data ?? -1; // 负数表示没有验证码
        if (time > 0) {
          restSeconds.value = resp.data!;
          timer = setInterval(() => {
            if (restSeconds.value >= 1) restSeconds.value--;
            else {
              restSeconds.value = 0;
              clearInterval(timer!);
            }
          }, 1000);
        }
      }
    } catch {
    } finally {
      showEmailBtn.value = true;
    }
  });
  /**
   * 获取邮箱验证码
   */
  let timer: number | null;
  onBeforeUnmount(() => {
    if (timer) clearInterval(timer);
  });
  /**
   * 获取邮箱验证码
   */
  const getEmailCheckCode = async (email: string, type: 0 | 1) => {
    // 校验email
    let pass = true;
    await formRef.value?.validateField('email', err => {
      if (err) pass = false;
    });
    if (!pass) return;
    try {
      isSending.value = true;
      const resp = await reqSendEmailCheckCode({ email, type });
      if (resp.code === 200) {
        Message.success('验证码已发送');
        restSeconds.value = EMAIL_EXP_TIME;
        timer = setInterval(() => {
          if (restSeconds.value >= 1) restSeconds.value--;
          else {
            restSeconds.value = 0;
            clearInterval(timer!);
          }
        }, 1000);
      } else {
        Message.error('验证码发送失败，' + resp.message);
      }
    } catch {
      Message.error('验证码发送失败');
    } finally {
      isSending.value = false;
    }
  };
  return { restSeconds, getEmailCheckCode, formRef, showEmailBtn, isSending };
}
