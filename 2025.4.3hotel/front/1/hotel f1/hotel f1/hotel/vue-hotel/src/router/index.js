import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 统一登录页面路由
const unifiedAuthRoutes = [
  {
    path: '/login',
    name: 'UnifiedLogin',
    component: () => import('../views/UnifiedLogin.vue'),
    meta: { title: '登录' }
  }
]

// 用户端路由
const userRoutes = [
  {
    path: '/',
    component: () => import('../layouts/UserLayout.vue'),
    children: [
      {
        path: '',
        name: 'UserHome',
        component: () => import('../views/user/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'test-route',
        name: 'TestRoute',
        component: () => import('../views/TestRoute.vue'),
        meta: { title: '测试路由' }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('../views/user/Profile.vue'),
        meta: { title: '用户信息', requireAuth: true }
      },
      {
        path: 'user/change-password',
        name: 'UserChangePassword',
        component: () => import('../views/user/ChangePassword.vue'),
        meta: { title: '修改密码', requireAuth: true }
      },
      {
        path: 'room/list',
        name: 'RoomList',
        component: () => import('../views/user/RoomList.vue'),
        meta: { title: '房型列表' }
      },
      {
        path: 'room/detail/:id',
        name: 'RoomDetail',
        component: () => import('../views/user/RoomDetail.vue'),
        meta: { title: '房型详情' }
      },
      {
        path: 'order/create',
        name: 'OrderCreate',
        component: () => import('../views/user/OrderCreate.vue'),
        meta: { title: '创建订单', requireAuth: true }
      },
      {
        path: 'order/list',
        name: 'OrderList',
        component: () => import('../views/user/OrderList.vue'),
        meta: { title: '订单列表', requireAuth: true }
      },
      {
        path: 'order/detail/:id',
        name: 'OrderDetail',
        component: () => import('../views/user/OrderDetail.vue'),
        meta: { title: '订单详情', requireAuth: true }
      },
      {
        path: 'favorite',
        name: 'Favorite',
        component: () => import('../views/user/Favorite.vue'),
        meta: { title: '我的收藏', requireAuth: true }
      },
      {
        path: 'comment',
        name: 'Comment',
        component: () => import('../views/user/Comment.vue'),
        meta: { title: '我的评价', requireAuth: true }
      },
      {
        path: 'review/create/:id',
        name: 'ReviewCreate',
        component: () => import('../views/user/ReviewCreate.vue'),
        meta: { title: '提交评价', requireAuth: true }
      },
      {
        path: 'review/list',
        name: 'ReviewList',
        component: () => import('../views/user/Comment.vue'),
        meta: { title: '我的评价', requireAuth: true }
      }
    ],
    meta: { title: '酒店预订系统' }
  },
  // 添加兼容性重定向路由
  {
    path: '/register',
    redirect: '/user/register'
  },
  {
    path: '/login',
    redirect: '/login'
  },
  // 处理/user/开头的路径
  {
    path: '/user/order/list',
    redirect: '/order/list'
  },
  {
    path: '/user/order/detail/:id',
    redirect: '/order/detail/:id'
  },
  {
    path: '/user/order/create',
    redirect: '/order/create'
  },
  {
    path: '/user/room/list',
    redirect: '/room/list'
  },
  {
    path: '/user/room/detail/:id',
    redirect: '/room/detail/:id'
  },
  {
    path: '/user/comment',
    redirect: '/comment'
  },
  {
    path: '/user/favorite',
    redirect: '/favorite'
  },
  {
    path: '/user/review/create/:id',
    redirect: '/review/create/:id'
  },
  {
    path: '/user/review/list',
    redirect: '/review/list'
  },
  // 处理/order/开头的错误路径
  {
    path: '/order/user/profile',
    redirect: '/user/profile'
  },
  {
    path: '/order/user/change-password',
    redirect: '/user/change-password'
  },
  {
    path: '/order/room/list',
    redirect: '/room/list'
  },
  {
    path: '/order/room/detail/:id',
    redirect: '/room/detail/:id'
  },
  {
    path: '/order/comment',
    redirect: '/comment'
  },
  {
    path: '/order/favorite',
    redirect: '/favorite'
  },
  // 处理/room/开头的错误路径
  {
    path: '/room/user/profile',
    redirect: '/user/profile'
  },
  {
    path: '/room/order/list',
    redirect: '/order/list'
  },
  {
    path: '/room/comment',
    redirect: '/comment'
  },
  {
    path: '/room/favorite',
    redirect: '/favorite'
  },
  {
    path: '/user',
    name: 'UserLayout',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      {
        path: 'info',
        name: 'UserInfo',
        component: () => import('@/views/user/UserInfo.vue'),
        meta: { title: '个人信息' }
      }
    ]
  }
]

// 用户独立页面路由（不包含导航栏）
const userStandaloneRoutes = [
  {
    path: '/user/login',
    redirect: '/login'
  },
  {
    path: '/user/register',
    name: 'UserRegister',
    component: () => import('@/views/user/Register.vue'),
    meta: { title: '用户注册' }
  }
]

// 员工后台路由
const staffRoutes = [
  {
    path: '/staff/login',
    redirect: '/login'
  },
  {
    path: '/staff',
    name: 'StaffLayout',
    component: () => import('@/layouts/StaffLayout.vue'),
    children: [
      {
        path: '', // 默认子路由
        redirect: '/staff/orders'
      },
      {
        path: 'dashboard',
        name: 'StaffDashboard',
        component: () => import('@/views/staff/Dashboard.vue'),
        meta: { 
          title: '员工仪表盘',
          requireStaffAuth: true
        }
      },
      {
        path: 'profile',
        name: 'StaffProfile',
        component: () => import('@/views/staff/ProfileManage.vue'),
        meta: { 
          title: '员工个人中心',
          requireStaffAuth: true
        }
      },
      {
        path: 'order',
        name: 'StaffOrder',
        component: () => import('../views/staff/Order.vue'),
        meta: { title: '订单详情', requireStaffAuth: true }
      },
      {
        path: 'orders',
        name: 'StaffOrders',
        component: () => import('../views/staff/Order.vue'),
        meta: { title: '预订管理', requireStaffAuth: true }
      },
      {
        path: 'checkin',
        name: 'StaffCheckIn',
        component: () => import('../views/staff/CheckIn.vue'),
        meta: { title: '入住登记', requireStaffAuth: true }
      },
      {
        path: 'checkin/list',
        name: 'StaffCheckInList',
        component: () => import('../views/staff/CheckInList.vue'),
        meta: { title: '入住记录列表', requireStaffAuth: true }
      },
      {
        path: 'checkout',
        name: 'StaffCheckOut',
        component: () => import('../views/staff/CheckOut.vue'),
        meta: { title: '退房结算', requireStaffAuth: true }
      },
      {
        path: 'checkout/list',
        name: 'StaffCheckOutList',
        component: () => import('../views/staff/CheckOutList.vue'),
        meta: { title: '退房记录列表', requireStaffAuth: true }
      },
      {
        path: 'room-status',
        name: 'StaffRoomStatus',
        component: () => import('../views/staff/RoomStatus.vue'),
        meta: { title: '房态管理', requireStaffAuth: true }
      },
      {
        path: 'guest',
        name: 'StaffGuest',
        component: () => import('../views/staff/Guest.vue'),
        meta: { title: '客人信息', requireStaffAuth: true }
      },
      {
        path: 'reviews',
        name: 'StaffReviews',
        component: () => import('../views/staff/ReviewManage.vue'),
        meta: { title: '评价管理', requireStaffAuth: true }
      },
      {
        path: 'attendance',
        name: 'StaffAttendance',
        component: () => import('../views/staff/Attendance.vue'),
        meta: { title: '考勤管理', requireStaffAuth: true }
      }
    ]
  }
]

// 管理端路由
const adminRoutes = [
  {
    path: '/admin/login',
    redirect: '/login'
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      {
        path: '',
        name: 'AdminHome',
        component: () => import('../views/admin/Home.vue'),
        meta: { title: '管理首页', requireAdminAuth: true }
      },
      {
        path: 'attendance',
        name: 'AdminAttendance',
        component: () => import('../views/admin/Attendance.vue'),
        meta: { title: '考勤管理', requireAdminAuth: true }
      },
      {
        path: 'account',
        name: 'AdminAccount',
        component: () => import('../views/admin/Account.vue'),
        meta: { title: '账户管理', requireAdminAuth: true }
      },
      {
        path: 'role',
        name: 'AdminRole',
        component: () => import('../views/admin/Role.vue'),
        meta: { title: '角色管理', requireAdminAuth: true }
      },
      {
        path: 'role-manage',
        name: 'AdminRoleManage',
        component: () => import('../views/admin/RoleManage.vue'),
        meta: { title: '角色管理', requireAdminAuth: true }
      },
      {
        path: 'employee-manage',
        name: 'AdminEmployeeManage',
        component: () => import('../views/admin/EmployeeManage.vue'),
        meta: { title: '员工管理', requireAdminAuth: true }
      },
      {
        path: 'profile-manage',
        name: 'AdminProfileManage',
        component: () => import('../views/admin/ProfileManage.vue'),
        meta: { title: '个人信息管理', requireAdminAuth: true }
      },
      {
        path: 'room-type',
        name: 'AdminRoomType',
        component: () => import('../views/admin/RoomType.vue'),
        meta: { title: '房型管理', requireAdminAuth: true }
      },
      {
        path: 'room-type-manage',
        name: 'AdminRoomTypeManage',
        component: () => import('../views/admin/RoomTypeManage.vue'),
        meta: { title: '房型管理', requireAdminAuth: true }
      },
      {
        path: 'room',
        name: 'AdminRoom',
        component: () => import('../views/admin/Room.vue'),
        meta: { title: '房间管理', requireAdminAuth: true }
      },
      {
        path: 'room-manage',
        name: 'AdminRoomManage',
        component: () => import('../views/admin/RoomManage.vue'),
        meta: { title: '房间管理', requireAdminAuth: true }
      },
      {
        path: 'staff',
        name: 'AdminStaff',
        component: () => import('../views/admin/Staff.vue'),
        meta: { title: '员工管理', requireAdminAuth: true }
      },
      {
        path: 'performance',
        name: 'AdminPerformance',
        component: () => import('../views/admin/Performance.vue'),
        meta: { title: '绩效管理', requireAdminAuth: true }
      },
      {
        path: 'finance',
        name: 'AdminFinance',
        component: () => import('../views/admin/Finance.vue'),
        meta: { title: '财务管理', requireAdminAuth: true }
      },
      {
        path: 'report',
        name: 'AdminReport',
        component: () => import('../views/admin/Report.vue'),
        meta: { title: '报表统计', requireAdminAuth: true }
      }
    ]
  }
]

// 所有路由
const routes = [
  ...unifiedAuthRoutes,
  ...userRoutes,
  ...userStandaloneRoutes,
  ...staffRoutes,
  ...adminRoutes,
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 酒店预订系统` : '酒店预订系统'
  
  // 访问登录页面的处理
  if (to.path === '/login') {
    // 检查是否有管理员/员工登录
    const employeeData = localStorage.getItem('LoginEmployee');
    if (employeeData) {
      try {
        const employee = JSON.parse(employeeData);
        // 根据角色重定向到不同页面
        if (employee.roleId === 1) {
          next('/admin');
          return;
        } else if (employee.roleId === 2) {
          next('/staff/orders');
          return;
        }
      } catch (e) {
        console.error('解析登录信息失败', e);
      }
    }
    
    // 检查普通用户是否登录
    if (localStorage.getItem('token')) {
      next('/');
      return;
    }
    
    // 未登录访问登录页面，允许访问
    next();
    return;
  }
  
  // 检查页面是否需要用户登录
  if (to.meta.requireAuth) {
    // 检查用户是否已登录
    const isUserLoggedIn = localStorage.getItem('token')
    if (!isUserLoggedIn) {
      // 保存原本要访问的路径
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
      return;
    }
  }
  
  // 检查是否需要员工登录权限
  if (to.meta.requireStaffAuth) {
    const employeeData = localStorage.getItem('LoginEmployee');
    
    if (!employeeData) {
      // 清除可能存在的不完整登录信息
      localStorage.removeItem('staffLoggedIn');
      localStorage.removeItem('staffUsername');
      localStorage.removeItem('staffRole');
      localStorage.removeItem('staffToken');
      
      console.log('需要员工登录权限，重定向到登录页');
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
      return;
    }
    
    try {
      const employee = JSON.parse(employeeData);
      // 员工(roleId=2)和管理员(roleId=1)都可以访问员工页面
      if (employee.roleId !== 1 && employee.roleId !== 2) {
        console.log('需要员工权限，当前用户没有员工权限');
        ElMessage.error('您没有员工权限');
        next('/login');
        return;
      }
    } catch (e) {
      console.error('解析员工信息失败', e);
      next('/login');
      return;
    }
  }
  
  // 检查是否需要管理员登录权限
  if (to.meta.requireAdminAuth) {
    const adminData = localStorage.getItem('LoginEmployee');
    if (!adminData) {
      console.log('需要管理员登录权限，重定向到登录页');
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
      return;
    }
    
    try {
      const employee = JSON.parse(adminData);
      if (employee.roleId !== 1) {
        console.log('需要管理员权限，当前用户不是管理员');
        ElMessage.error('您没有管理员权限');
        next('/staff/orders'); // 如果是员工，重定向到员工页面
        return;
      }
    } catch (e) {
      console.error('解析管理员信息失败', e);
      next('/login');
      return;
    }
  }
  
  next();
})

export default router 