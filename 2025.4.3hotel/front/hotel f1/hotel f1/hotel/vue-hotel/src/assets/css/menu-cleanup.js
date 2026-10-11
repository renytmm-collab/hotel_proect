/**
 * 用于在运行时清理垂直侧边菜单的辅助脚本
 * 在Vue应用初始化完成后执行
 */

// 定时移除垂直菜单
function removeVerticalMenus() {
  // 移除可能存在的垂直菜单
  const verticalMenus = document.querySelectorAll('.el-menu--vertical');
  if (verticalMenus.length > 0) {
    verticalMenus.forEach(menu => {
      if (menu.parentNode) {
        menu.parentNode.removeChild(menu);
      }
    });
    console.log('已清理垂直菜单元素:', verticalMenus.length);
  }

  // 检查并移除popper容器中的垂直菜单
  const poppers = document.querySelectorAll('.el-popper.is-pure');
  poppers.forEach(popper => {
    if (popper.classList.contains('el-menu--vertical')) {
      if (popper.parentNode) {
        popper.parentNode.removeChild(popper);
      }
    }
  });
}

// 在DOM变化时检查并移除垂直菜单
function setupMenuObserver() {
  const observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      if (mutation.addedNodes.length > 0) {
        // 如果添加了新节点，检查是否为垂直菜单
        removeVerticalMenus();
      }
    }
  });

  // 观察整个文档中的DOM变化
  observer.observe(document.body, {
    childList: true,
    subtree: true
  });

  console.log('已设置菜单监视器');
}

// 导出清理函数
export function setupMenuCleaner() {
  // 立即执行一次清理
  removeVerticalMenus();
  
  // 设置监视器以持续清理
  setupMenuObserver();
  
  // 设置定时任务，每500ms检查一次
  const interval = setInterval(removeVerticalMenus, 500);
  
  // 30秒后停止定时任务，此时应用应该已经稳定
  setTimeout(() => {
    clearInterval(interval);
    console.log('菜单清理定时任务已停止');
  }, 30000);
} 