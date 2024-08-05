<template>
	<div id="app">
		<router-view></router-view>
	</div>
</template>

<script>
	export default {
		name: 'App',
		components: {}
	}
</script>

<style lang="scss">
@import './assets/style/global.css';

#app {
	font-family: 'Avenir', Helvetica, Arial, sans-serif;
	-webkit-font-smoothing: antialiased;
	-moz-osx-font-smoothing: grayscale;
	position: absolute;
	height: 100%;
	width: 100%;
}

.el-message {
	z-index: 99999999 !important;
}

.el-scrollbar__thumb {
	background-color: #A0A8AF !important;
}

.el-dialog {
	border-radius: 8px !important;
	overflow: hidden !important;
}

.el-dialog__header {
	background-color: #5870e6 !important;
	
}

.el-dialog__title {
	color: #f8f8f8 !important;
}

.el-dialog__close {
	color: white !important;
	font-size: 20px;
}

.el-checkbox__inner {
	border-radius: 50% !important;
}

.el-input__inner {
	border-radius: 5px !important;
	border: #587FF0 1px solid !important;
}

.el-textarea__inner{
	border-radius: 5px !important;
	border: #587FF0 1px solid !important;
}

.el-icon-search {
	color:#587FF0 !important;
}

.el-button--primary {
    background-color: #687Ff0 !important;
	border-color: #687Ff0 !important;
}

.el-button--success {
	background-color: #4cae1b !important;
	border-color: #4cae1b !important;
}
.el-button--danger {
	background-color: #ea4949 !important;
	border-color: #ea4949 !important;
}

.el-button {
	padding: 8px 15px !important;
}

.el-checkbox {
	display: flex;
	align-items: center;

	//修改选中框的大小
	.el-checkbox__inner {
		width: 20px;
		height: 20px;

		//修改选中框中的对勾的大小和位置
		&::after {
			height: 12px;
			left: 7px;
		}
	}

	//修改点击文字颜色不变
	.el-checkbox__input.is-checked+.el-checkbox__label {
		color: #333333;
	}

	.el-checkbox__label {
		line-height: 20px;
		padding-left: 8px;
	}
}
</style>