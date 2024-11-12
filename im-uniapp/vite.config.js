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
				target: 'http://127.0.0.1:8888',
				changeOrigin: true
			}
		}
	}
})