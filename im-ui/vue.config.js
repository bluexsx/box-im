const path = require('path')
const fs = require('fs')

module.exports = {
	devServer: {
		proxy: {
			'/api': {
				target: 'http://127.0.0.1:8888',
				changeOrigin: true,
				ws: false,
				pathRewrite: {
					'^/api': ''
				}
			}
		},
		// 音视频功能需要ssl证书，如需调试请打开注释
		// https: {
		// 	cert: fs.readFileSync(path.join(__dirname, 'src/ssl/cert.crt')),
		// 	key: fs.readFileSync(path.join(__dirname, 'src/ssl/cert.key'))
		// }
	}

}