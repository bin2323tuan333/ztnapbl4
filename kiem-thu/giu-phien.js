// Giữ một kết nối TCP qua tunnel và gửi request mỗi giây, để quan sát lúc phiên bị cắt.
// Dùng: node kiem-thu/giu-phien.js <cổng local Agent mở> [số giây, mặc định 120]
const [, , port, secs = '120'] = process.argv;
if (!port) { console.log('Thiếu cổng. Dùng: node kiem-thu/giu-phien.js <cổng> [giây]'); process.exit(1); }
const t0 = Date.now(); const t = () => ((Date.now() - t0) / 1000).toFixed(1) + 's';
const s = require('net').connect(+port, '127.0.0.1');
let n = 0, closed = false;
s.on('data', d => { const m = d.toString().match(/Đường dẫn: ([^<]*)/); console.log(`[${t()}] nhận phản hồi ${m ? m[1] : d.length + ' byte'}`); });
s.on('close', () => { closed = true; console.log(`[${t()}] >>> KẾT NỐI ĐÃ BỊ ĐÓNG <<<`); process.exit(0); });
s.on('error', e => console.log(`[${t()}] lỗi: ${e.message}`));
s.on('connect', () => {
    console.log(`Đã nối vào 127.0.0.1:${port}. Gửi request mỗi giây trong ${secs} giây...`);
    const send = () => { if (!closed) { n++; s.write(`GET /lan-${n} HTTP/1.1\r\nHost: x\r\nConnection: keep-alive\r\n\r\n`); } };
    send(); const iv = setInterval(send, 1000);
    setTimeout(() => { clearInterval(iv); console.log(`[${t()}] Hết thời gian, phiên VẪN MỞ`); s.destroy(); process.exit(0); }, +secs * 1000);
});
