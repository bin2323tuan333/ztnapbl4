// Ứng dụng web "nội bộ" giả để thử tunnel: lắng nghe cổng 8081, 8082, 8083 (cổng của 3 resource mẫu).
// Chạy trên máy Connector:  node kiem-thu/web-gia.js
const http = require('http');
for (const port of [8081, 8082, 8083]) {
    http.createServer((req, res) => {
        console.log(`[web-gia:${port}] ${new Date().toLocaleTimeString()} ${req.method} ${req.url}`);
        res.setHeader('Content-Type', 'text/html; charset=utf-8');
        res.end(`<h1>Ứng dụng nội bộ (cổng ${port})</h1><p>Đường dẫn: ${req.url}</p><p>${new Date().toLocaleString()}</p>`);
    }).listen(port, () => console.log(`web-gia đang chạy ở cổng ${port}`))
      .on('error', e => console.log(`Không mở được cổng ${port}: ${e.message}`));
}
