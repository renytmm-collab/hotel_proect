import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
    // 移除错误的 token 配置，在拦截器中处理
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 调试输出请求信息
    console.log('请求URL:', config.url);
    console.log('请求方法:', config.method);
    console.log('请求数据:', config.data);
    console.log('请求头:', config.headers);
    
    // 确定是否是登录或注册请求
    const isAuthRequest = 
      (config.url.includes('/login') || config.url.includes('/register')) &&
      config.method === 'post';
    
    // 检查是否是文件上传请求
    const isFileUpload = config.headers['Content-Type'] === 'multipart/form-data';
    
    // 如果是身份验证请求，则不添加token
    if (isAuthRequest) {
      console.log('身份验证请求，不添加token');
      return config;
    }
    
    // 根据请求URL判断是用户请求还是员工请求
    // if(config.url.includes('/user')){
    //   try {
        const loginUser = JSON.parse(localStorage.getItem('loginUser'));
        if(loginUser && loginUser.token){
          config.headers.token = loginUser.token;
          console.log('添加用户Token:', loginUser.token);
        }
      // } 
      // catch (error) {
      //   console.error('解析用户信息失败:', error);
      // }}
    
    
    // 处理员工API请求的token
    if(config.url.includes('/api/employee') || config.url.includes('/api/staff') || config.url.includes('/api/admin/')){
      try {
        const loginEmployee = localStorage.getItem('LoginEmployee');
        if(loginEmployee) {
          const employeeData = JSON.parse(loginEmployee);
          if(employeeData && employeeData.token){
            config.headers.token = employeeData.token;
            console.log('添加员工Token:', employeeData.token);
          } else {
            console.warn('员工Token不存在，可能导致授权失败');
          }
        } else {
          console.warn('未找到员工登录信息');
        }
      } catch (error) {
        console.error('解析员工信息失败:', error);
      }
    }
    
    // 如果不是文件上传请求，确保Content-Type是正确的
    if (!isFileUpload && !config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json';
    }
    
    return config;
  },

  error => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    console.log('响应数据:', response.data);
    
    const res = response.data;
    // 如果返回的状态码不是200，表示请求出错，我们可以做一些统一的错误处理
    if (res.code !== 200) {
      // 使用后端返回的错误消息，如果没有则使用默认消息
      const errorMsg = res.msg || res.message || '请求失败';
      
      // 对于登录/注册相关的错误，不显示错误消息，让组件自己处理
      const isAuthRequest = 
        (response.config.url.includes('/login') || response.config.url.includes('/register')) &&
        response.config.method === 'post';
      
      if (!isAuthRequest) {
        ElMessage.error(errorMsg);
      }
      
      console.error('响应错误:', res);
      
      // 如果是未登录状态，可以重定向到登录页面
      if (res.code === 401) {
        // 判断是员工/管理员还是用户接口
        if (response.config.url.includes('/api/employee') || response.config.url.includes('/api/admin/')) {
          // 清除员工token和相关信息
          localStorage.removeItem('LoginEmployee');
          localStorage.removeItem('staffLoggedIn');
          localStorage.removeItem('staffUsername');
          localStorage.removeItem('staffRole');
          // 跳转到员工登录页面
          setTimeout(() => {
            window.location.href = '/staff/login';
          }, 1500);
        } else {
          // 清除用户token
          localStorage.removeItem('token');
          localStorage.removeItem('userInfo');
          localStorage.removeItem('loginUser');
          // 跳转到用户登录页面
          setTimeout(() => {
            window.location.href = '/user/login';
          }, 1500);
        }
      }
      
      // 返回带有后端错误信息的错误对象
      return Promise.reject(new Error(errorMsg));
    } else {
      // 如果是登录请求，保存token
      if (response.config.url.includes('/login')) {
        // 判断是员工还是用户登录
        if (response.config.url.includes('/admin')) {
          // 员工登录响应处理在Login.vue组件中
          console.log('员工登录成功，令牌已在组件中处理');
        } else {
          // 用户登录
          const { token, ...userInfo } = res.data;
          if (token) {
            localStorage.setItem('token', token);
            localStorage.setItem('userInfo', JSON.stringify(userInfo));
            console.log('用户登录成功，保存token:', token);
          }
        }
      }
      return res;
    }
  },
  error => {
    // 详细记录错误信息
    console.error('响应错误完整信息:', error);
    if (error.response) {
      console.error('错误响应数据:', error.response.data);
      console.error('错误状态码:', error.response.status);
    }
    
    // 处理网络错误或后端返回的HTTP错误状态码
    let message = '请求失败，请检查网络';
    
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = error.response.data.message || '请求参数错误';
          break;
        case 401:
          message = '未授权，请登录';
          
          // 判断是员工/管理员还是用户接口
          if (error.config.url.includes('/api/employee') || error.config.url.includes('/api/admin/')) {
            // 清除员工token和相关信息
            localStorage.removeItem('LoginEmployee');
            localStorage.removeItem('staffLoggedIn');
            localStorage.removeItem('staffUsername');
            localStorage.removeItem('staffRole');
            // 跳转到员工登录页面
            setTimeout(() => {
              window.location.href = '/staff/login';
            }, 1500);
          } else {
            // 清除用户token
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            localStorage.removeItem('loginUser');
            // 跳转到用户登录页面
            setTimeout(() => {
              window.location.href = '/user/login';
            }, 1500);
          }
          break;
        case 403:
          message = '拒绝访问';
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          message = '服务器内部错误';
          break;
        default:
          message = `请求失败: ${error.response.status}`;
      }
    } else if (error.request) {
      // 请求已发送但未收到响应
      message = '服务器未响应';
    } else {
      // 请求配置错误
      message = error.message;
    }
    
    ElMessage.error(message);
    return Promise.reject(error);
  }
)

export default request 