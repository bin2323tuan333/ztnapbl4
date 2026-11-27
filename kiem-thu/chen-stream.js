// Kiểm T5: mở một kết nối WebSocket MỚI tới Server (giả làm Agent khác) rồi gửi DATA và CLOSE
// vào streamId của phiên đang mở. Server phải từ chối: ứng dụng nội bộ không nhận request "/CHEN-TRAI-PHEP"
// và phiên gốc vẫn chạy.
// Dùng: node kiem-thu/chen-stream.js <streamId> [ws://localhost:9443/app]
// (Viết WebSocket client tối giản bằng module net để chạy được trên mọi bản Node, không cần cài thư viện.)
const net = require('net'), crypto = require('crypto');
const [, , streamId, url = 'ws://localhost:9443/app'] = process.argv;
if (!streamId) { console.log('Thiếu streamId. Dùng: node kiem-thu/chen-stream.js <streamId> [ws-url]'); process.exit(1); }
const u = new URL(url);

const id = Buffer.from(streamId, 'utf8');
const payload = Buffer.from('GET /CHEN-TRAI-PHEP HTTP/1.1\r\nHost: x\r\n\r\n');
const len4 = Buffer.alloc(4); len4.writeInt32BE(payload.length);
const dataFrame = Buffer.concat([Buffer.from([0x02, id.length]), id, len4, payload]);   // DATA: [0x02][len][streamId][len4][data]
const closeFrame = Buffer.concat([Buffer.from([0x03, id.length]), id, Buffer.from([0x01])]); // CLOSE: [0x03][len][streamId][lý do]

function wsBinary(buf) {                       // khung WebSocket nhị phân, có mask (bắt buộc từ phía client)
    const mask = crypto.randomBytes(4);
    let head;
    if (buf.length < 126) head = Buffer.from([0x82, 0x80 | buf.length]);
    else { head = Buffer.alloc(4); head[0] = 0x82; head[1] = 0x80 | 126; head.writeUInt16BE(buf.length, 2); }
    const body = Buffer.from(buf.map((b, i) => b ^ mask[i % 4]));
    return Buffer.concat([head, mask, body]);
}

const sock = net.connect(+u.port || 80, u.hostname, () => {
    const key = crypto.randomBytes(16).toString('base64');
    sock.write(`GET ${u.pathname} HTTP/1.1\r\nHost: ${u.host}\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n`
        + `Sec-WebSocket-Key: ${key}\r\nSec-WebSocket-Version: 13\r\n\r\n`);
});
let upgraded = false;
sock.on('data', d => {
    if (upgraded) return;
    if (!d.toString().startsWith('HTTP/1.1 101')) { console.log('Bắt tay WebSocket thất bại:\n' + d.toString().split('\r\n')[0]); process.exit(1); }
    upgraded = true;
    console.log('Đã nối WebSocket mới tới', url, '(kênh lạ, không sở hữu stream)');
    sock.write(wsBinary(dataFrame)); console.log('Đã gửi DATA chứa "GET /CHEN-TRAI-PHEP" vào stream', streamId);
    setTimeout(() => { sock.write(wsBinary(closeFrame)); console.log('Đã gửi CLOSE cho stream', streamId); }, 300);
    setTimeout(() => { sock.end(); console.log('Xong. Kiểm: web-gia KHÔNG in "/CHEN-TRAI-PHEP", phiên gốc vẫn nhận phản hồi.'); process.exit(0); }, 1200);
});
sock.on('error', e => { console.log('Lỗi kết nối:', e.message); process.exit(1); });
