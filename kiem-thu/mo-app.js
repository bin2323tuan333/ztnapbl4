// Mở một ứng dụng qua Agent thay cho người dùng (giống bấm "Mở" trên Portal nhưng không bật Chrome),
// rồi gửi thử 1 request qua tunnel. Dùng để kiểm T4 (không có rule thì bị từ chối) và N1 (nhiều người chung 1 Agent).
// Dùng: node kiem-thu/mo-app.js <tài khoản> [chuỗi trong tên ứng dụng, mặc định "Web"]
//   Hỏi mật khẩu của tài khoản đó và của admin (để tra id ứng dụng). Có thể đặt sẵn PASS và ADMIN_PASS.
//   SERVER (mặc định http://localhost:443), AGENT (mặc định http://127.0.0.1:9999).
const SERVER = process.env.SERVER || 'http://localhost:443', AGENT = process.env.AGENT || 'http://127.0.0.1:9999';
const B = SERVER + '/api/v1';
const [, , username, appName = 'Web'] = process.argv;
if (!username) { console.log('Dùng: node kiem-thu/mo-app.js <tài khoản> [tên ứng dụng]'); process.exit(1); }
const ask = q => new Promise(r => { const rl = require('readline').createInterface({ input: process.stdin, output: process.stdout }); rl.question(q, a => { rl.close(); r(a); }); });
const login = async (u, p) => { const r = await fetch(B + '/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username: u, password: p }) }); return r.ok ? (await r.json()).accessToken : null; };

(async () => {
    const pass = process.env.PASS ?? await ask(`Mật khẩu ${username}: `);
    const adminPass = process.env.ADMIN_PASS ?? await ask('Mật khẩu admin (để tra id ứng dụng): ');
    const tok = await login(username, pass), adminTok = await login('admin', adminPass);
    if (!tok || !adminTok) { console.log('Đăng nhập thất bại (sai mật khẩu, tài khoản bị khoá, hoặc Server chưa chạy).'); process.exit(1); }
    const res = await (await fetch(B + '/admin/resources', { headers: { Authorization: 'Bearer ' + adminTok } })).json();
    const list = Array.isArray(res) ? res : (res.content || res.data || []);
    const app = list.find(r => (r.name || '').toLowerCase().includes(appName.toLowerCase()));
    if (!app) { console.log(`Không có ứng dụng nào tên chứa "${appName}". Có: ${list.map(r => r.name).join(', ')}`); process.exit(1); }
    console.log(`Người dùng: ${username}   Ứng dụng: ${app.name} (${app.host}:${app.port})`);

    let r;
    try { r = await fetch(`${AGENT}/open?appId=${app.id}&type=none`, { headers: { Authorization: 'Bearer ' + tok } }); }
    catch (e) { console.log('Không gọi được Agent ở ' + AGENT + ' (Agent chưa chạy?)'); process.exit(1); }
    const body = await r.text();
    console.log(`Agent trả về: HTTP ${r.status}  ${body}`);
    const port = (body.match(/"port":(\d+)/) || [])[1];
    if (!port) { console.log('=> KHÔNG được mở ứng dụng.'); return; }
    try {
        const page = await (await fetch(`http://127.0.0.1:${port}/thu-tu-${username}`, { signal: AbortSignal.timeout(8000) })).text();
        console.log(`=> ĐÃ MỞ qua cổng local ${port}. Nội dung nhận được: ${page.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim().slice(0, 120)}`);
    } catch (e) { console.log(`=> Agent mở cổng ${port} nhưng request qua tunnel lỗi: ${e.message}`); }
})();
