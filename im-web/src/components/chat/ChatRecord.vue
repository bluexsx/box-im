<template>
  <el-dialog v-model="show" class="chat-record" :title="'语音录制'" width="600px" draggable destroy-on-close :before-close="onBeforeClose">
    <div v-show="mode == 'RECORD'">
      <div class="tip">{{ stateTip }}</div>
      <div>{{ '时长' }}: {{ state == 'STOP' ? 0 : parseInt(String(rc.duration)) }}s</div>
    </div>
    <audio v-show="mode == 'PLAY'" ref="audioRef" :src="url" controls @ended="onStopAudio"></audio>
    <el-divider content-position="center"></el-divider>
    <el-row class="btn-group" justify="center">
      <el-button v-show="state == 'STOP'" round type="primary" @click="onStartRecord">{{ '开始录音' }}</el-button>
      <el-button v-show="state == 'RUNNING'" round type="warning" @click="onPauseRecord">{{ '暂停录音' }}</el-button>
      <el-button v-show="state == 'PAUSE'" round type="primary" @click="onResumeRecord">{{ '继续录音' }}</el-button>
      <el-button v-show="state == 'RUNNING' || state == 'PAUSE'" round type="danger" @click="onCompleteRecord">{{ '结束录音' }}</el-button>
      <el-button v-show="state == 'COMPLETE' && mode != 'PLAY'" round type="success" @click="onPlayAudio">{{ '播放录音' }}</el-button>
      <el-button v-show="state == 'COMPLETE' && mode == 'PLAY'" round type="warning" @click="onStopAudio">{{ '停止播放' }}</el-button>
      <el-button v-show="state == 'COMPLETE'" round type="primary" @click="onRestartRecord">{{ '重新录音' }}</el-button>
      <el-button v-show="state == 'COMPLETE'" round type="primary" @click="onSendRecord">{{ '立即发送' }}</el-button>
    </el-row>
  </el-dialog>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import Recorder from 'js-audio-recorder';
import { uploadFile } from '@/api/file';
const emit = defineEmits<{ send: [data: { duration: number; url: string }] }>();
const show = ref(false);
const rc = ref(new Recorder());
const state = ref<'STOP' | 'RUNNING' | 'PAUSE' | 'COMPLETE'>('STOP');
const stateTip = ref('');
const mode = ref<'RECORD' | 'PLAY'>('RECORD');
const url = ref('');
const audioRef = ref<HTMLAudioElement>();

const resetRecorder = () => {
  rc.value.destroy();
  rc.value = new Recorder();
};

const cleanup = () => {
  resetRecorder();
  audioRef.value?.pause();
  mode.value = 'RECORD';
  state.value = 'STOP';
  stateTip.value = '未开始';
};

const open = () => {
  stateTip.value = '未开始';
  show.value = true;
};

const close = () => {
  cleanup();
  show.value = false;
};

const onBeforeClose = (done: () => void) => {
  // 关闭前清除数据
  cleanup();
  done();
};

const onStartRecord = () => {
  rc.value
    .start()
    .then(() => {
      state.value = 'RUNNING';
      stateTip.value = '正在录音...';
    })
    .catch((error: Error) => {
      ElMessage.error(error.message || String(error));
    });
};

const onPauseRecord = () => {
  rc.value.pause();
  state.value = 'PAUSE';
  stateTip.value = '已暂停录音';
};

const onResumeRecord = () => {
  rc.value.resume();
  state.value = 'RUNNING';
  stateTip.value = '正在录音...';
};

const onCompleteRecord = () => {
  rc.value.pause();
  state.value = 'COMPLETE';
  stateTip.value = '已结束录音';
};

const onPlayAudio = () => {
  const wav = rc.value.getWAVBlob();
  const blobUrl = URL.createObjectURL(wav);
  if (audioRef.value) {
    audioRef.value.src = blobUrl;
    audioRef.value.play();
  }
  mode.value = 'PLAY';
};

const onStopAudio = () => {
  audioRef.value?.pause();
  mode.value = 'RECORD';
};

const onRestartRecord = () => {
  resetRecorder();
  rc.value.start();
  state.value = 'RUNNING';
  mode.value = 'RECORD';
  stateTip.value = '正在录音...';
};

const onSendRecord = async () => {
  const wav = rc.value.getWAVBlob();
  const name = new Date().getDate() + '.wav';
  const file = new File([wav], name, { type: 'audio/wav' });
  const fileUrl = await uploadFile(file);
  const data = {
    duration: parseInt(String(rc.value.duration)),
    url: fileUrl
  };
  emit('send', data);
  close();
};

defineExpose({ open, close });
</script>
<style lang="scss" scoped>
.chat-record {
  .tip {
    font-size: 18px;
  }

  .btn-group {
    margin-bottom: 20px;
  }
}
</style>
