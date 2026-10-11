<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full text-center">
      <div class="mb-8">
        <h1 class="text-9xl font-bold text-blue-600">404</h1>
        <h2 class="text-3xl font-bold text-gray-900 mt-4">页面未找到</h2>
        <p class="mt-2 text-base text-gray-500">
          抱歉，您访问的页面不存在或已被删除。
        </p>
      </div>
      <div class="flex flex-col space-y-4">
        <router-link
          to="/"
          class="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          返回首页
        </router-link>
        <button
          @click="goBack"
          class="w-full flex justify-center py-3 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          返回上一页
        </button>
      </div>
      
      <!-- 一些可能有用的链接 -->
      <div class="mt-8">
        <p class="text-sm text-gray-500 mb-2">您可能想要访问：</p>
        <div class="flex flex-wrap justify-center gap-2">
          <router-link to="/" class="text-sm text-blue-600 hover:text-blue-800">首页</router-link>
          <router-link to="/room/list" class="text-sm text-blue-600 hover:text-blue-800">房型列表</router-link>
          <router-link to="/user/login" class="text-sm text-blue-600 hover:text-blue-800">用户登录</router-link>
          <router-link to="/admin/login" class="text-sm text-blue-600 hover:text-blue-800">管理员登录</router-link>
        </div>
      </div>
      
      <!-- 简单的装饰图案 -->
      <div class="mt-12">
        <svg class="mx-auto" width="200" height="80" viewBox="0 0 200 80" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M35.5 16C24.4543 16 15.5 24.9543 15.5 36C15.5 47.0457 24.4543 56 35.5 56H55.5V36C55.5 24.9543 46.5457 16 35.5 16Z" fill="#DBEAFE" />
          <path d="M85.5 16C74.4543 16 65.5 24.9543 65.5 36C65.5 47.0457 74.4543 56 85.5 56H105.5V36C105.5 24.9543 96.5457 16 85.5 16Z" fill="#DBEAFE" />
          <path d="M135.5 16C124.454 16 115.5 24.9543 115.5 36C115.5 47.0457 124.454 56 135.5 56H155.5V36C155.5 24.9543 146.546 16 135.5 16Z" fill="#DBEAFE" />
          <path d="M165.5 36C165.5 24.9543 174.454 16 185.5 16C196.546 16 205.5 24.9543 205.5 36C205.5 47.0457 196.546 56 185.5 56C174.454 56 165.5 47.0457 165.5 36Z" fill="#DBEAFE" />
          <path d="M35.5 24C28.8726 24 23.5 29.3726 23.5 36C23.5 42.6274 28.8726 48 35.5 48H47.5V36C47.5 29.3726 42.1274 24 35.5 24Z" fill="#2563EB" />
          <path d="M85.5 24C78.8726 24 73.5 29.3726 73.5 36C73.5 42.6274 78.8726 48 85.5 48H97.5V36C97.5 29.3726 92.1274 24 85.5 24Z" fill="#2563EB" />
          <path d="M135.5 24C128.873 24 123.5 29.3726 123.5 36C123.5 42.6274 128.873 48 135.5 48H147.5V36C147.5 29.3726 142.127 24 135.5 24Z" fill="#2563EB" />
          <path d="M173.5 36C173.5 29.3726 178.873 24 185.5 24C192.127 24 197.5 29.3726 197.5 36C197.5 42.6274 192.127 48 185.5 48C178.873 48 173.5 42.6274 173.5 36Z" fill="#2563EB" />
        </svg>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  methods: {
    goBack() {
      window.history.length > 1 ? this.$router.go(-1) : this.$router.push('/')
    }
  }
}
</script>