// Kiểm tự động N4 (refresh token không dùng thay access token), N5 (đăng xuất phía server),
// N6 (khoá user có hiệu lực ngay). Cần Server đang chạy; Agent chạy thì kiểm thêm phần Agent.
// Dùng: node kiem-thu/kiem-token.js
//   Mật khẩu admin và user (xem DataInit.java) được hỏi khi chạy, hoặc đặt sẵn biến ADMIN_PASS, USER_PASS.
//   Địa chỉ khác mặc định: đặt SERVER (vd http://192.168.1.10:443) và AGENT (mặc định http://127.0.0.1:9999).
// Lưu ý: script KHOÁ rồi MỞ KHOÁ lại tài khoản "user" (biến TEST_USER để đổi tài khoản).
const SERVER = process.env.SERVER || 'http://localhost:443';
const AGENT = process.env.AGENT || 'http://127.0.0.1:9999';
const ADMIN = process.env.TEST_ADMIN || 'admin', USER = process.env.TEST_USER || 'user';
const B = SERVER + '/api/v1';
let pass = 0, fail = 0;

const ask = q => new Promise(r => { const rl = require('readline').createInterface({ input: process.stdin, output: process.stdout }); rl.question(q, a => { rl.close(); r(a); }); });
async function call(method, path, { token, cookie, body, base = B } = {}) {
    const headers = {};
    if (token) headers.Authorization = 'Bearer ' + token;
    if (cookie) headers.Cookie = 'refresh_token=' + cookie;
    if (body) headers['Content-Type'] = 'application/json';
    try {
        const r = await fetch(base + path, { method, headers, body: body && JSON.stringify(body) });
        const text = await r.text(); let json = null; try { json = JSON.parse(text); } catch { }
        const sc = r.headers.getSetCookie ? r.headers.getSetCookie() : [];
        const m = sc.map(c => c.match(/^refresh_token=([^;]*)/)).find(Boolean);
        return { code: r.status, json, text, refresh: m ? m[1] : undefined };
    } catch (e) { return { code: -1, text: e.cause?.code || e.message }; }
}
function check(name, actual, expected, extra = '') {
    const ok = Array.isArray(expected) ? expected.includes(actual) : actual === expected;
    if (ok) pass++; else fail++;
    console.log(`${ok ? '  [ĐẠT]      ' : '  [KHÔNG ĐẠT]'} ${name}: nhận ${actual}, mong đợi ${expected}${extra ? '  ' + extra : ''}`);
}
const login = async (u, p) => call('POST', '/auth/login', { body: { username: u, password: p } });

(async () => {
    const adminPass = process.env.ADMIN_PASS ?? await ask(`Mật khẩu ${ADMIN}: `);
    const userPass = process.env.USER_PASS ?? await ask(`Mật khẩu ${USER}: `);

    console.log('\n== Chuẩn bị');
    const a = await login(ADMIN, adminPass); check(`đăng nhập ${ADMIN}`, a.code, 200);
    const u = await login(USER, userPass); check(`đăng nhập ${USER}`, u.code, 200);
    if (a.code !== 200 || u.code !== 200) { console.log('Không đăng nhập được, dừng. Kiểm Server đang chạy và mật khẩu.'); process.exit(1); }
    const adminTok = a.json.accessToken, userTok = u.json.accessToken;
    const me = await call('GET', '/users/me', { token: userTok });
    const userId = me.json?.id;
    const res = await call('GET', '/admin/resources', { token: adminTok });
    const list = Array.isArray(res.json) ? res.json : (res.json?.content || res.json?.data || []);
    const appId = list[0]?.id;
    const agentUp = (await call('GET', '/open', { base: AGENT })).code !== -1;
    if (!agentUp) console.log('  (Agent không chạy ở ' + AGENT + ': bỏ qua các bước kiểm qua Agent)');

    console.log('\n== N4: refresh token không được dùng thay access token');
    check('GET /users/me bằng access token', (await call('GET', '/users/me', { token: userTok })).code, 200);
    check('GET /users/me bằng REFRESH token', (await call('GET', '/users/me', { token: u.refresh })).code, 401);
    if (agentUp && appId) {
        const r = await call('GET', `/open?appId=${appId}&type=none`, { base: AGENT, token: u.refresh });
        check('Agent /open bằng REFRESH token', r.code, 401, r.text);
    }

    console.log('\n== N5: đăng xuất phía server');
    const u2 = await login(USER, userPass);
    check('refresh trước khi đăng xuất', (await call('POST', '/auth/refresh', { cookie: u2.refresh })).code, 200);
    const lo = await call('POST', '/auth/logout', { cookie: u2.refresh });
    check('POST /auth/logout', lo.code, 204, lo.refresh === '' ? '(cookie đã bị xoá)' : '');
    const after = await call('POST', '/auth/refresh', { cookie: u2.refresh });
    check('refresh bằng cookie CŨ sau đăng xuất', after.code, 401, after.text);

    console.log('\n== N6: khoá user thì token cũ mất tác dụng ngay');
    const u3 = await login(USER, userPass);
    check(`khoá ${USER}`, (await call('PATCH', `/admin/users/${userId}/block`, { token: adminTok })).code, [200, 204]);
    try {
        check('token cũ gọi /users/me sau khi khoá', (await call('GET', '/users/me', { token: u3.json.accessToken })).code, 401);
        check('refresh sau khi khoá', (await call('POST', '/auth/refresh', { cookie: u3.refresh })).code, [401, 403]);
        if (agentUp && appId) {
            const r = await call('GET', `/open?appId=${appId}&type=none`, { base: AGENT, token: u3.json.accessToken });
            check('Agent /open bằng token cũ sau khi khoá', r.code, 401, r.text);
        }
    } finally {
        check(`mở khoá ${USER} (trả lại như cũ)`, (await call('PATCH', `/admin/users/${userId}/unblock`, { token: adminTok })).code, [200, 204]);
    }
    check('token cũ dùng lại được sau khi mở khoá', (await call('GET', '/users/me', { token: u3.json.accessToken })).code, 200);

    console.log(`\nKết quả: ${pass} đạt, ${fail} không đạt`);
    process.exit(fail ? 1 : 0);
})();
