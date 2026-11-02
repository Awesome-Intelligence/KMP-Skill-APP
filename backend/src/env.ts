/** 加载 backend/.env（Node 24 原生，无 dotenv 依赖）。文件不存在就算了。 */
try {
  process.loadEnvFile();
} catch {
  // 没有 .env：全走默认值 / 真实环境变量
}
