<template>
	<view>
		<text>{{formatDate}}</text>
	</view>

</template>

<script>
	export default {
		name: "chat-time",
		data() {
			return {}
		},
		props: {
			time: {
				type: Number
			}
		},
		computed: {
			formatDate() {
				let time = new Date(this.time);
				let strtime = "";

				let todayTime = new Date();
				todayTime.setHours(0, 0, 0, 0)
				let dayDiff = Math.floor((todayTime.getTime() - time.getTime()) / (24 * 3600 * 1000));
				if (time.getTime() > todayTime.getTime()) {
					strtime = time.getHours() <= 9 ? "0" + time.getHours() : time.getHours();
					strtime += ":"
					strtime += time.getMinutes() <= 9 ? "0" + time.getMinutes() : time.getMinutes();
				} else if (dayDiff < 1) {
					strtime = "昨天";
				} else if (dayDiff < 7) {
					strtime = `${dayDiff+1}天前`;
				} else {
					strtime = time.getMonth() + 1 + "月" + time.getDate() + "日";
				}
				return strtime;
			}
		}
	}
</script>

<style>
</style>