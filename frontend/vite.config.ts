import { fileURLToPath, URL } from 'node:url';
import { viteStaticCopy } from 'vite-plugin-static-copy';
import { defineConfig, Plugin } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import { ArcoResolver } from 'unplugin-vue-components/resolvers';
import { visualizer } from "rollup-plugin-visualizer";


const copiedIconPathInDist = 'assets/icon';
/**
 * 转换动态icon路径为打包后复制的路径
 * @returns
 */
const transIconPathPlugin = (): Plugin => {
    return {
        name: 'transIconPath',
        transform(src) {
            // 要有/
            return src.replace(/\/src\/assets\/icon/g, `/${copiedIconPathInDist}`);
        },
    };
};


// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        vueJsx(),
        AutoImport({
            imports: ['vue', 'vue-router', 'pinia'],
            include: [
                /\.[tj]sx?$/, // .ts, .tsx, .js, .jsx
                /\.vue$/,
                /\.vue\?vue/, // .vue
                /\.md$/, // .md
            ],
            dts: true,
            eslintrc: {
                enabled: true,
            },
        }),
        Components({
            resolvers: [
                ArcoResolver({
                    sideEffect: true,
                }),
            ],
        }),
        viteStaticCopy({
            // dest是dist下的文件夹，没有/
            targets: [{ src: 'src/assets/icon/*', dest: copiedIconPathInDist }],
        }),
        transIconPathPlugin(),
        visualizer(),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8002',
                changeOrigin: true,
            },
        },
    },
    build: {
        rollupOptions: {
            output: {
                // experimentalMinChunkSize: 200 * 1024,
                manualChunks(id: string) {
                    // vue-office拆包前，7.6M
                    // 按照ppt、word、pdf、excel拆包后 0.2M、1.3M、1.7M、2.8M
                    // 17.48s => 16.41s
                    if (/@vue-office/.test(id)) {
                        const chunkId = /@(vue-office.*?)@/.exec(id)![1]
                        return chunkId
                    }
                    if (/arco-vue-icon/.test(id)) {
                        return 'arco-design-vue-icon'
                    }
                    if (/arco-vue/.test(id)) {
                        return 'arco-design-vue'
                    }
                }
            }
        }
    }
});
