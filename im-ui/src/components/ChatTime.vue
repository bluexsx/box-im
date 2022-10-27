<template>
	<span>{{formatDate}}</span>
</template>

<script>
	export default {
		name: "chatTime",
		data() {
			return {}
		},
		props: {
			time: {
				type: Number
			}
		},
		computed:{
			formatDate(){
				console.log(this.time);
				let time = new Date(this.time);
				let strtime = "";
				let curTime = new Date();
				let dayDiff =curTime.getDate() - time.getDate() ;
				if (time.getDate() === new Date().getDate()) {
					strtime = time.getHours() < 9 ? "0" + time.getHours() : time.getHours();
					strtime += ":"
					strtime += time.getMinutes() < 9 ? "0" + time.getMinutes() : time.getMinutes();
				} else if (dayDiff === 1) {
					strtime = "昨天";
				} else if (dayDiff < 7) {
					strtime = `${dayDiff}天前`;
				} else {
					strtime = time.getMonth()+1+"月"+time.getDate()+"日";
				}
				
				console.log(strtime);
				return strtime;
			}
		}
	}
</script>

<style>
</style>
