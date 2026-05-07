<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="失物招领" name="lostfound" />
      <el-tab-pane label="二手交易" name="secondhand" />
    </el-tabs>

    <!-- 失物招领 -->
    <el-card v-if="activeTab === 'lostfound'" shadow="never" class="table-card">
      <div class="filter-bar">
        <el-select v-model="lfParams.type" placeholder="类型" clearable @change="fetchLostFound" style="width: 120px;">
          <el-option label="寻物启事" :value="0" />
          <el-option label="失物招领" :value="1" />
        </el-select>
        <el-select v-model="lfParams.status" placeholder="状态" clearable @change="fetchLostFound" style="width: 120px; margin-left: 10px;">
          <el-option label="寻找中/待认领" :value="0" />
          <el-option label="已解决" :value="1" />
          <el-option label="已撤销" :value="2" />
        </el-select>
      </div>
      <el-table :data="lfTableData" v-loading="lfLoading" border stripe style="margin-top: 12px;">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 0 ? 'warning' : 'success'" effect="dark">
              {{ scope.row.type === 0 ? '寻物' : '招领' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="发布人" width="140">
          <template #default="scope">{{ scope.row.studentName || '--' }} ({{ scope.row.studentNo || '--' }})</template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="contactInfo" label="联系方式" width="140" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="['warning','success','info'][scope.row.status] || 'info'">
              {{ ['寻找中','已解决','已撤销'][scope.row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="info" link @click="openLfDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="danger"
              link
              @click="handleViolateLf(scope.row.id)"
            >违规下架</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="lfParams.page" v-model:page-size="lfParams.pageSize"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
          :total="lfTotal" @size-change="fetchLostFound" @current-change="fetchLostFound"
        />
      </div>
    </el-card>

    <!-- 二手交易 -->
    <el-card v-if="activeTab === 'secondhand'" shadow="never" class="table-card">
      <div class="filter-bar">
        <el-select v-model="shParams.status" placeholder="状态" clearable @change="fetchSecondhand" style="width: 120px;">
          <el-option label="在售" :value="0" />
          <el-option label="已售出" :value="1" />
          <el-option label="已下架" :value="2" />
        </el-select>
        <el-select v-model="shParams.category" placeholder="分类" clearable @change="fetchSecondhand" style="width: 130px; margin-left: 10px;">
          <el-option label="电子数码" value="电子数码" />
          <el-option label="生活用品" value="生活用品" />
          <el-option label="代步工具" value="代步工具" />
          <el-option label="书籍资料" value="书籍资料" />
          <el-option label="服饰鞋包" value="服饰鞋包" />
          <el-option label="其他" value="其他" />
        </el-select>
      </div>
      <el-table :data="shTableData" v-loading="shLoading" border stripe style="margin-top: 12px;">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="商品名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="卖家" width="140">
          <template #default="scope">{{ scope.row.studentName || '--' }} ({{ scope.row.studentNo || '--' }})</template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="价格" width="110" align="center">
          <template #default="scope"><span style="color:#f56c6c;font-weight:600;">¥{{ scope.row.price }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="['success','info','danger'][scope.row.status] || 'info'">
              {{ ['在售','已售出','已下架'][scope.row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="info" link @click="openShDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="danger"
              link
              @click="handleViolateSh(scope.row.id)"
            >违规下架</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="shParams.page" v-model:page-size="shParams.pageSize"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
          :total="shTotal" @size-change="fetchSecondhand" @current-change="fetchSecondhand"
        />
      </div>
    </el-card>

    <!-- 失物招领详情弹窗 -->
    <el-dialog v-model="lfDetailVisible" title="失物招领详情" width="520px">
      <el-descriptions :column="1" border v-if="lfDetail.id">
        <el-descriptions-item label="标题">{{ lfDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ lfDetail.description }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ lfDetail.contactInfo }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ lfDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="lfDetail.images" label="物品照片">
          <div class="detail-images">
            <el-image v-for="(url, idx) in lfDetail.images.split(',')" :key="idx" :src="url" style="width:120px;height:120px;margin-right:8px;" :preview-src-list="lfDetail.images.split(',')" fit="cover" />
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="lfDetailVisible = false">关闭</el-button></template>
    </el-dialog>

    <!-- 二手详情弹窗 -->
    <el-dialog v-model="shDetailVisible" title="商品详情" width="520px">
      <el-descriptions :column="1" border v-if="shDetail.id">
        <el-descriptions-item label="名称">{{ shDetail.name }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ shDetail.description }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ shDetail.price }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ shDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="shDetail.images" label="商品图片">
          <div class="detail-images">
            <el-image v-for="(url, idx) in shDetail.images.split(',')" :key="idx" :src="url" style="width:120px;height:120px;margin-right:8px;" :preview-src-list="shDetail.images.split(',')" fit="cover" />
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="shDetailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminLostFoundPage, violateLostFound, getAdminSecondhandPage, violateSecondhand } from '@/api/admin'

const activeTab = ref('lostfound')

// === 失物招领 ===
const lfLoading = ref(false)
const lfTableData = ref([])
const lfTotal = ref(0)
const lfParams = reactive({ page: 1, pageSize: 10, type: null, status: null })

const fetchLostFound = async () => {
  lfLoading.value = true
  try {
    const params = { page: lfParams.page, pageSize: lfParams.pageSize }
    if (lfParams.type !== null && lfParams.type !== '') params.type = lfParams.type
    if (lfParams.status !== null && lfParams.status !== '') params.status = lfParams.status
    const res = await getAdminLostFoundPage(params)
    lfTableData.value = res.records || []
    lfTotal.value = res.total || 0
  } catch (e) { console.error(e) } finally { lfLoading.value = false }
}

const lfDetailVisible = ref(false)
const lfDetail = ref({})
const openLfDetail = (row) => { lfDetail.value = row; lfDetailVisible.value = true }

const handleViolateLf = (id) => {
  ElMessageBox.confirm('确认将该信息违规下架？', '提示', { type: 'danger' }).then(async () => {
    await violateLostFound({ id })
    ElMessage.success('已违规下架')
    fetchLostFound()
  }).catch(() => {})
}

// === 二手交易 ===
const shLoading = ref(false)
const shTableData = ref([])
const shTotal = ref(0)
const shParams = reactive({ page: 1, pageSize: 10, status: null, category: null })

const fetchSecondhand = async () => {
  shLoading.value = true
  try {
    const params = { page: shParams.page, pageSize: shParams.pageSize }
    if (shParams.status !== null && shParams.status !== '') params.status = shParams.status
    if (shParams.category !== null && shParams.category !== '') params.category = shParams.category
    const res = await getAdminSecondhandPage(params)
    shTableData.value = res.records || []
    shTotal.value = res.total || 0
  } catch (e) { console.error(e) } finally { shLoading.value = false }
}

const shDetailVisible = ref(false)
const shDetail = ref({})
const openShDetail = (row) => { shDetail.value = row; shDetailVisible.value = true }

const handleViolateSh = (id) => {
  ElMessageBox.confirm('确认将该商品违规下架？', '提示', { type: 'danger' }).then(async () => {
    await violateSecondhand({ id })
    ElMessage.success('已违规下架')
    fetchSecondhand()
  }).catch(() => {})
}

const onTabChange = (tab) => {
  if (tab === 'lostfound') fetchLostFound()
  else fetchSecondhand()
}

onMounted(() => fetchLostFound())
</script>

<style scoped>
.app-container { }
.filter-bar { display: flex; align-items: center; margin-bottom: 4px; }
.table-card { }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
.detail-images { display: flex; flex-wrap: wrap; }
</style>
