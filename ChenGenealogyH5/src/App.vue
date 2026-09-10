<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'

onLaunch(() => {
  const user = useUserStore()
  user.restore()
  if (user.token) user.fetchInfo()
})
</script>
<style lang="scss">
page {
  background: #f6f1e6;
  color: #2a251f;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.page {
  min-height: 100vh;
  padding: 24rpx 24rpx 48rpx;
  box-sizing: border-box;
}
.card {
  background: #fffdf7;
  border: 1px solid #e8dfcc;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}
.card-title {
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}
.btn-primary {
  background: #a63d2f;
  color: #fff;
  border: none;
}
.muted {
  color: #8b8273;
  font-size: 24rpx;
}
.tag {
  display: inline-block;
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  background: #f4e3de;
  color: #a63d2f;
}
.ghost {
  background: #fffdf7;
  color: #a63d2f;
  border: 1px solid #e8dfcc;
}
</style>
