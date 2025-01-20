import { useAppStore, useUserStore } from '@/stores';
import { menus } from '@/constants/menuData';
import type { IMenuItem } from '@/types/home';
import { calcFileSize } from '@/utils/calc';

/**
 * 根据使用率得到对应的颜色
 * @param rate
 */
const calcColorByUsedRate = (rate: number) => {
  if (rate < 0.3) return '#00B42A';
  if (rate < 0.5) return '#165DFF';
  if (rate < 0.8) return '#FF7D00';
  return '#F53F3F';
};

/**
 * 左侧菜单栏
 */
const SideNav = defineComponent(() => {
  const { menuIdx, subMenuIdx } = storeToRefs(useAppStore());
  const { userInfo } = storeToRefs(useUserStore());// 使用率
  const usedRate = computed(() => userInfo.value.usedSpace / userInfo.value.totalSpace);
  const router = useRouter();
  return () => (
    <aside class="h-full flex flex-col justify-between">
      <div class="flex">
        <div class="w-24">
          {menus.map((m, i) => m.isShow ? (
            <div
              key={m.id}
              class={`${i === menuIdx.value ? 'text-h-blue' : ''} text-lg h-24  flex flex-col justify-center items-center select-none cursor-pointer hover:text-h-blue transition-all my-4`}
              onClick={async () => {
                await router.push(m.path);
                subMenuIdx.value = 0;
                menuIdx.value = i;
              }}
            >
              <div class="scale-150 mb-2">{m.icon}</div>
              <div>{m.name}</div>
            </div>
          ) : <></>)}
        </div>
        <div class="w-3 bg-[#eee]"></div>
        <div class="w-36 mr-10 mt-3">
          {menus[menuIdx.value].children?.map((c: IMenuItem, i: number) => c.isShow ? (
              <div
                key={i}
                class={`flex justify-center items-center rounded-lg hover:text-h-blue h-20 text-lg cursor-pointer select-none ${i === subMenuIdx.value ? 'text-h-blue bg-h-blue bg-opacity-30' : ''}`}
                onClick={async () => {
                  await router.push(c.path);
                  subMenuIdx.value = i;
                }}
              >
                <div class="scale-150">
                  {c.icon}
                </div>
                <div class="ml-3 transition">
                  {c.name}</div>
              </div>
            ) : <></>
          )}
        </div>
      </div>
      <div class="h-24 mb-4 flex flex-col justify-between items-center">
        已用空间：{calcFileSize(userInfo.value.usedSpace)} / {calcFileSize(userInfo.value.totalSpace)}
        <a-progress type="circle" percent={usedRate.value} color={calcColorByUsedRate(usedRate.value)}
                    stroke-width={4} />
      </div>
    </aside>);
});

function Content() {
  const show = ref<boolean>(false);
  return () => (
    <main class="flex w-full h-full">
      <SideNav class="hidden lg:flex" />
      <div class="lg:hidden pl-2">
        <a-tooltip content="展开菜单">
          <icon-double-right class="scale-150 mt-10 hover:text-h-blue" onClick={() => show.value = true} />
        </a-tooltip>
      </div>
      <a-drawer
        width={340}
        placement="left"
        closable={false}
        footer={false}
        v-model:visible={show.value}
        unmountOnClose
      >
        <SideNav />
      </a-drawer>
      <section class="flex-1 overflow-auto">
        <router-view></router-view>
      </section>
      ;
    </main>
  )
    ;
}

export default defineComponent(Content, {});
