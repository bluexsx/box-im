import { defineConfig } from "vite"
import uni from "@dcloudio/vite-plugin-uni";
const path = require('path')
const fs = require('fs')
export default defineConfig({
	plugins: [
		uni()
	],
	server: {
		proxy: {
			'/api': {
				rewrite: path => path.replace(/^\/api/, ''),
				logLevel: 'debug',
				target: 'http://localhost:8888',
				changeOrigin: true
			},
			
		},
		// 音视频功能需要ssl证书，如需调试请打开注释
		// https: {
		// 	cert: fs.readFileSync(path.join(__dirname, 'ssl/cert.crt')),
		// 	key: fs.readFileSync(path.join(__dirname, 'ssl/cert.key'))
		// }
	}
})