<template>
    <div class="markdown-container">
        <div v-html="renderedContent">
        </div>
        
    </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';  // 引入 GitHub 风格的代码高亮样式
import markdownItHighlightjs from 'markdown-it-highlightjs';

// 定义一个 content 字段，用于父组件传入 markdown 文本
const props = defineProps({
    content: {
        type: String,
        default: ''
    }
})

// 解析后的 HTML
const renderedContent = ref('')

// 初始化 MarkdownIt
const md = new MarkdownIt({
    html: true,             // 允许解析 HTML 标签
    xhtmlOut: true,         // 输出符合 XHTML 规范的标签（如`<br />`而不是`<br>`）。默认 false。
    linkify: true,          // 自动将文本中的 URL 转换为可点击的链接
    typographer: true,      // 启用排版优化
    breaks: true,           // 将单个换行符(\n)转换为<br>
    langPrefix: 'language-',// 代码块的语言类名前缀（默认 'language-'）。例如 ```js 会生成 <pre><code class="language-js">
})

// 使用 markdownItHighlightjs 插件启用代码高亮
md.use(markdownItHighlightjs, {
  hljs,
  auto: true, // 自动检测语言
  code: true  // 高亮内联代码
})

// 保存默认的代码块渲染规则
const defaultRender = md.renderer.rules.fence || function(tokens, idx, env, renderer) {
  // 调用默认的渲染函数处理代码块
  return renderer.renderToken(tokens, idx, options)
}

// 重写 Markdown 渲染器的代码块渲染规则
md.renderer.rules.fence = function (tokens, idx, options, env, renderer) {
  // 获取当前索引对应的 token （代码块）
  const token = tokens[idx]
  // 处理语言信息：移除转移字符并去除首尾空格
  const info = token.info ? md.utils.unescapeAll(token.info).trim() : ''
  let langName = ''

  // 如果存在语言信息
  if (info) {
    // 分割信息字符串
    const langCode = info.split(/\s+/g)[0] // 取第一个部分作为语言标识，如 ```js 中的 js
    langName = langCode.toLowerCase() // 转换为小写
  }

  // 使用默认渲染器生成代码块的 HTML 内容
  const originalContent = defaultRender(tokens, idx, options, env, renderer)

  // 拼接最终的 HTML
  let finalContent = `<div class="code-block-wrapper">
    <div class="code-header">
    `
  // 如果有返回代码块语言信息，需要显示
  if (langName) {
    finalContent += `<div class="code-language">${langName}</div>`
  }

  // 代码块中的实际代码
  const codeContent = token.content
  // 为每个代码块分配一个唯一标识，方便知道复制的哪个代码块中的内容
  const codeId = `code-${Math.random().toString(36).substr(2, 9)}`
  
  // 返回渲染结果
  return finalContent += `
        <button class="copy-code-btn" onclick="copyCode('${codeId}')">  
          <svg t="1750068080826" class="copy-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1529" 
          width="15" height="15"><path d="M761.088 715.3152a38.7072 38.7072 0 0 1 0-77.4144 37.4272 37.4272 0 0 0 37.4272-37.4272V265.0112a37.4272 37.4272 0 0 0-37.4272-37.4272H425.6256a37.4272 37.4272 0 0 0-37.4272 37.4272 38.7072 38.7072 0 1 1-77.4144 0 115.0976 115.0976 0 0 1 114.8416-114.8416h335.4624a115.0976 115.0976 0 0 1 114.8416 114.8416v335.4624a115.0976 115.0976 0 0 1-114.8416 114.8416z" p-id="1530"></path><path d="M589.4656 883.0976H268.1856a121.1392 121.1392 0 0 1-121.2928-121.2928v-322.56a121.1392 121.1392 0 0 1 121.2928-121.344h321.28a121.1392 121.1392 0 0 1 121.2928 121.2928v322.56c1.28 67.1232-54.1696 121.344-121.2928 121.344zM268.1856 395.3152a43.52 43.52 0 0 0-43.8784 43.8784v322.56a43.52 43.52 0 0 0 43.8784 43.8784h321.28a43.52 43.52 0 0 0 43.8784-43.8784v-322.56a43.52 43.52 0 0 0-43.8784-43.8784z" p-id="1531"></path></svg>
          <span class="copy-text">复制</span>
        </button>
      </div>
      <div class="code-content" id="${codeId}" data-code="${encodeURIComponent(codeContent)}">
        ${originalContent}
      </div>
    </div>`
}

// 监听 content 字段，流式更新处理
watch(() => props.content, (newVal) => {
    // 即使为空字符串也要渲染，确保初始状态能正确显示
    const html = md.render(newVal || '')
    renderedContent.value = html
}, { immediate: true })

</script>

<style scoped>
.markdown-container {
  width: 100%;
  line-height: 24px;
  color: rgb(64 64 64);
}

/* 第一个 p 标签的上边距设置为0 */
:deep(.markdown-container > p:first-child),
:deep(p:first-child) {
  margin-top: 0;
}

/* Markdown 转换为 HTML 的样式 */

/* 修复标题选择器 - 使用逗号分隔多个选择器 */
:deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
  font-weight: 600;
  margin: calc(1.143 * 16px) 0 calc(1.143 * 12px) 0;
}

:deep(h1) {
  font-size: 1.5em;
  margin-top: 1.2em;
  margin-bottom: 0.7em;
  line-height: 1.5;
}

:deep(h2) {
  font-size: 1.3em;
  margin-top: 1.1em;
  margin-bottom: 0.6em;
  line-height: 1.5;
}

:deep(h3) {
  font-size: calc(1.143 * 16px);
  line-height: 1.5;
}

:deep(p) {
  line-height: 1.7;
  margin: calc(1.143 * 12px) 0;
  font-size: calc(1.143* 14px);
}

:deep(ul) {
  list-style: disc; /* 实心圆点 */
  margin-top: 0.6em;
  margin-bottom: 0.9em;
  padding-left: 2em;
}

:deep(ol) {
  list-style: decimal;
  margin-top: 0.6em;
  margin-bottom: 0.9em;
  padding-left: 2em;
}

/* 列表项样式 */
:deep(li) {
  margin-bottom: 0.5em;
  line-height: 1.7;
}

/* 修复列表标记样式 */
:deep(ol li::marker) {
  line-height: calc(1.143 * 25px);
  color: rgb(139 139 139);
}

:deep(ul li::marker) {
  color: rgb(139 139 139);
}

/* 嵌套列表样式 */
:deep(ul ul) {
  list-style: circle;
  margin-top: 0.3em;
  margin-bottom: 0.3em;
}

:deep(ul ul ul) {
  list-style: square; /* 三级列表使用方块 */
}

:deep(pre) {
  background-color: #fafafa;
  padding: 1em;
  border-radius: 5px;
  overflow-x: auto;
  max-width: 100%; /* 确保不超过容器宽度 */
  white-space: pre; /* 保持原始格式 */
  word-wrap: normal; /* 不在单词内部换行 */
}

/* 单独的 code 标签样式 - 不在 pre 内的code */
:deep(:not(pre) > code) {
  font-size: .875em;
  font-weight: 600;
  background-color: #ececec;
  border-radius: 4px;
  padding: .15rem .3rem;
  margin: 0 .2rem;
}

/* pre 内的 code 标签样式 */
:deep(pre > code) {
  font-size: .875em;
  background-color: transparent;
  padding: 0;
  border-radius: 0;
  font-weight: normal;
  color: #333;
  display: block;
  width: 100%;
}

:deep(a) {
  color: #4d6bfe;
  text-decoration: none;
}

:deep(a:hover) {
  text-decoration: underline;
}

:deep(blockquote) {
  border-left: 4px solid #e5e5e5;
  padding-left: 1em;
  margin: 1em 0;
  color: #666;
}

:deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
  font-size: 0.95em;
}

:deep(th), :deep(td) {
  border: 1px solid #e5e5e5;
  padding: 0.6em;
  text-align: left;
}

:deep(th) {
  background-color: #f5f5f5;
}

:deep(hr) {
  background-color: rgb(229 229 229);
  margin: 1.5em 0;
  height: 1px;
  border: none;
}

/* 确保相邻元素之间的间距一致且适当 */
:deep(h1 + p),
:deep(h2 + p),
:deep(h3 + p) {
  margin-top: 0.5em;
}

:deep(p + ul),
:deep(p + ol) {
  margin-top: 0.5em;
}

:deep(ul + p),
:deep(ol + p) {
  margin-top: 0.7em;
}

/* 代码块包装器样式 */
:deep(.code-block-wrapper) {
  margin: 1em 0;
  border-radius: 14px;
  overflow: hidden;
  background-color: #f6f8fa;
}

/* 代码块头部样式 */
:deep(.code-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f5f5f5;
  padding: 8px 12px;
}

/* 语言标签样式 */
:deep(.code-language-label) {
  color: rgb(82 82 82);
  margin-left: 8px;
  font-size: 12px;
  line-height: 18px;
}

/* 代码高亮样式优化 */
:deep(.hljs) {
  background: transparent !important;
  padding: 0 !important;
}

/* 复制按钮样式 */
:deep(.copy-code-btn) {
  display: flex;
  align-items: center;
  gap: 4px;
  background: transparent;
  border-radius: 12px;
  padding: 0 8px;
  color: #586069;
  font-size: 12px;
  height: 28px;
  cursor: pointer;
  transition: all 0.2s ease;
}

:deep(.copy-code-btn.copied .copy-icon) {
  fill: #22c55e;
}

:deep(.copy-code-btn:hover) {
  background-color: rgb(0 0 0 / 4%);
}

:deep(.copy-icon) {
  fill: currentColor;
  flex-shrink: 0;
}

:deep(.copy-text) {
  white-space: nowrap;
}

</style>