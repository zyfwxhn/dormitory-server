import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  // ========== 学生端 ==========
  {
    path: '/student',
    name: 'StudentLayout',
    component: () => import('@/views/student/Layout.vue'),
    redirect: '/student/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: { title: '首页看板' }
      },
      {
        path: 'repair',
        name: 'StudentRepair',
        component: () => import('@/views/student/repair/Index.vue'),
        meta: { title: '宿舍报修' }
      },
      {
        path: 'reservation',
        name: 'StudentReservation',
        component: () => import('@/views/student/reservation/Index.vue'),
        meta: { title: '生活预约' }
      },
      {
        path: 'lost-found',
        name: 'StudentLostFound',
        component: () => import('@/views/student/lostfound/Index.vue'),
        meta: { title: '失物招领' }
      },
      {
        path: 'secondhand',
        name: 'StudentSecondhand',
        component: () => import('@/views/student/secondhand/Index.vue'),
        meta: { title: '二手交易' }
      },
      {
        path: 'notification',
        name: 'StudentNotification',
        component: () => import('@/views/student/notification/Index.vue'),
        meta: { title: '通知中心' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  // ========== 维修员端 ==========
  {
    path: '/worker',
    name: 'WorkerLayout',
    component: () => import('@/views/worker/Layout.vue'),
    redirect: '/worker/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'WorkerDashboard',
        component: () => import('@/views/worker/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'repair',
        name: 'WorkerRepair',
        component: () => import('@/views/worker/repair/Index.vue'),
        meta: { title: '工单管理' }
      },
      {
        path: 'profile',
        name: 'WorkerProfile',
        component: () => import('@/views/worker/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  // ========== 管理员端 ==========
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/views/admin/Layout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据看板' }
      },
      {
        path: 'device',
        name: 'AdminDevice',
        component: () => import('@/views/admin/device/Index.vue'),
        meta: { title: '设备管理' }
      },
      {
        path: 'worker',
        name: 'AdminWorker',
        component: () => import('@/views/admin/worker/Index.vue'),
        meta: { title: '维修员管理' }
      },
      {
        path: 'repair',
        name: 'AdminRepair',
        component: () => import('@/views/admin/repair/Index.vue'),
        meta: { title: '工单管理' }
      },
      {
        path: 'student',
        name: 'AdminStudent',
        component: () => import('@/views/admin/student/Import.vue'),
        meta: { title: '学生导入' }
      },
      {
        path: 'reservation',
        name: 'AdminReservation',
        component: () => import('@/views/admin/reservation/Index.vue'),
        meta: { title: '预约管理' }
      },
      {
        path: 'review',
        name: 'AdminReview',
        component: () => import('@/views/admin/content/Review.vue'),
        meta: { title: '内容审核' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 宿舍服务系统`
  }
  // 三端 token 分 key 存储，任一有效即可通过
  const hasToken = localStorage.getItem('student_token')
    || localStorage.getItem('worker_token')
    || localStorage.getItem('admin_token')
  if (to.path !== '/login' && !hasToken) {
    next('/login')
  } else {
    next()
  }
})

export default router
