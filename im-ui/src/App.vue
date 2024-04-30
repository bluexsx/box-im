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
</style>