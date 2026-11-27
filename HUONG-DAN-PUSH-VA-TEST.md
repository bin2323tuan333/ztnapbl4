# Hướng dẫn: push lên GitHub và kiểm thử các lỗi đã sửa

Tài liệu gồm 2 phần:
- **Phần A**: đưa 14 patch trong `C:\Users\Legion\Downloads\ztna-patches` vào repo thật, tạo nhánh riêng, push lên GitHub, mở Pull Request.
- **Phần B**: chạy toàn bộ hệ thống trên 1 máy (XAMPP + Eclipse/terminal + VS Code), kiểm lần lượt từng lỗi đã sửa.

Mọi lệnh dưới đây gõ trong **PowerShell** (Windows Terminal hoặc terminal của VS Code), trừ khi ghi khác.
Mật khẩu tài khoản mẫu và token connector **không** chép vào tài liệu này. Xem ở:
- Tài khoản mẫu (admin, user, alice, bob, locked): `ztna-server/src/main/java/com/example/ztna_server/config/DataInit.java`, dòng 83–99.
- Token của connector mẫu: cùng file, dòng 39.

---

## Phần A – Push lên một nhánh riêng trên GitHub

### A0. Vì sao dùng patch mà không push thẳng repo đang sửa?

Repo dùng để sửa lỗi là một repo `git init` riêng: lịch sử không chung gốc với repo trên GitHub, và có 2 commit đầu chứa mật khẩu trong tài liệu.
Vì vậy ta áp các patch (mỗi patch là một commit sửa lỗi) vào **bản clone của repo thật**, rồi push bản đó.

### A1. Kiểm tra công cụ (chỉ làm lần đầu)

```powershell
git --version
```
Phải in ra phiên bản, ví dụ `git version 2.4x`. Nếu báo không tìm thấy lệnh: cài Git for Windows rồi mở lại terminal.

Khai báo tên và email (dùng làm tác giả commit, chỉ cần làm một lần trên mỗi máy):
```powershell
git config --global user.name "Ten Cua Ban"
```
```powershell
git config --global user.email "email-dang-ky-github@example.com"
```

### A2. Có bản clone của repo thật

Nếu máy đã có thư mục clone từ GitHub, bỏ qua bước này. Nếu chưa có:
```powershell
cd $HOME\Documents
```
```powershell
git clone https://github.com/<chủ-repo>/<tên-repo>.git
```
Lần đầu clone repo private, Windows mở cửa sổ đăng nhập GitHub: đăng nhập bằng tài khoản có quyền vào repo.

### A3. Vào repo thật và kiểm tra sạch sẽ

```powershell
cd <đường-dẫn-repo-thật>
```
```powershell
git status
```
Mong đợi: `nothing to commit, working tree clean`.
Nếu còn file đang sửa dở: commit chúng, hoặc cất tạm bằng `git stash` (lấy lại bằng `git stash pop`).

### A4. Cập nhật nhánh chính

Xem tên nhánh chính (thường là `main`, có repo dùng `master`):
```powershell
git branch -a
```
Chuyển sang nhánh chính và kéo bản mới nhất:
```powershell
git checkout main
```
```powershell
git pull
```

### A5. Tạo nhánh mới cho đợt sửa lỗi

```powershell
git checkout -b fix/ztna-review
```
Mong đợi: `Switched to a new branch 'fix/ztna-review'`.

### A6. Áp 14 patch theo đúng thứ tự

PowerShell không tự mở rộng `*.patch` khi gọi git, nên phải liệt kê file và sắp theo tên như sau:
```powershell
git am --3way (Get-ChildItem C:\Users\Legion\Downloads\ztna-patches\*.patch | Sort-Object Name).FullName
```
Mong đợi: 14 dòng `Applying: ...`, bắt đầu bằng `Applying: T17: ...` và kết thúc bằng `Applying: Thêm hướng dẫn push, kiểm thử và script kiểm`. Không có dòng `error`.

**Nếu git báo xung đột** (`Patch failed at 00xx ...` hoặc `CONFLICT`), lý do thường là nhóm đã sửa cùng file đó trên GitHub sau bản `706525e`:
1. Xem file nào bị xung đột:
   ```powershell
   git status
   ```
   Các file ghi `both modified` là file cần sửa.
2. Mở từng file đó trong VS Code, tìm các đoạn `<<<<<<<`, `=======`, `>>>>>>>`. Giữ phần đúng (thường là gộp cả hai bên), xoá 3 dòng đánh dấu.
3. Đánh dấu đã xử lý xong và đi tiếp:
   ```powershell
   git add <tên-file>
   ```
   ```powershell
   git am --continue
   ```
4. Muốn bỏ hết và quay về như trước khi áp patch:
   ```powershell
   git am --abort
   ```

Xung đột dễ gặp nhất là patch `0013` (sửa `application.yaml`), nếu ai đó trong nhóm đã đổi file này.

### A7. Kiểm tra trước khi push

1. Xem 14 commit mới nằm trên nhánh:
   ```powershell
   git log --oneline -15
   ```
2. Build Java (máy không có `mvn` thì dùng wrapper có sẵn):
   ```powershell
   .\ztna-server\mvnw.cmd -f pom.xml -DskipTests package
   ```
   Mong đợi: kết thúc với `BUILD SUCCESS`.
3. Build giao diện:
   ```powershell
   cd ztna-portal-ui
   ```
   ```powershell
   npm ci
   ```
   ```powershell
   npm run build
   ```
   ```powershell
   cd ..
   ```
   Mong đợi: dòng cuối có `built in ...`, không có `error TS`.
4. Đảm bảo không commit file bí mật `.env`. Lệnh sau phải **không in ra gì**:
   ```powershell
   git ls-files .env
   ```
5. (Nên làm) Chạy thử theo **Phần B** ngay trên repo thật trước khi push.

### A8. Push nhánh lên GitHub

```powershell
git push -u origin fix/ztna-review
```
- Lần đầu push, Windows có thể mở cửa sổ đăng nhập GitHub (Git Credential Manager). Chọn đăng nhập bằng trình duyệt.
- Nếu terminal hỏi `Username` / `Password`: GitHub **không nhận mật khẩu tài khoản**. Ô Password phải là Personal Access Token (GitHub → Settings → Developer settings → Personal access tokens → Generate new token, cấp quyền `repo`).
- Mong đợi cuối cùng: `branch 'fix/ztna-review' set up to track 'origin/fix/ztna-review'`.

`-u` chỉ cần ở lần push đầu. Lần sau sửa thêm thì `git add`, `git commit`, rồi chỉ cần `git push`.

### A9. Mở Pull Request trên GitHub

1. Vào trang repo trên GitHub. Sẽ có thanh vàng **Compare & pull request** của nhánh `fix/ztna-review`; bấm vào.
   (Không thấy thì vào tab **Pull requests** → **New pull request**, chọn `base: main` ← `compare: fix/ztna-review`.)
2. Tiêu đề gợi ý: `Sửa lỗi phân quyền, phiên và cấu hình demo LAN (đợt review 27/11)`.
3. Mô tả: liệt kê các mã lỗi đã sửa (T17, N1, N2, T1, T11, T18, T4, T12, N4, N5, N6, T3, T2, T5, T14, N7, T13), và ghi chú **Agent và Server phải chạy cùng bản mới** vì giao thức frame đã đổi.
4. Bấm **Create pull request**. Chờ nhóm xem rồi mới bấm **Merge**.

### A10. Thành viên khác lấy nhánh về để thử

```powershell
git fetch
```
```powershell
git checkout fix/ztna-review
```
Trong Eclipse: chuột phải từng project → **Maven** → **Update Project** (vì `agent/pom.xml` đã đổi). Trong VS Code: chạy lại `npm ci` trong `ztna-portal-ui`.

---

## Phần B – Kiểm thử toàn bộ trên máy local

### B0. Chuẩn bị

| Cần có | Kiểm bằng |
|---|---|
| JDK 21 | `java -version` in ra `21` |
| Node 20 trở lên | `node -v` |
| XAMPP (MySQL) | XAMPP Control Panel |
| Google Chrome | Agent dùng Chrome để mở ứng dụng web |

**Quan trọng về cổng:**
- Trong XAMPP chỉ bấm **Start** cho **MySQL**, **không** Start Apache. Apache của XAMPP dùng cổng 443, trùng với cổng REST của ZTNA Server.
- Các cổng sau phải trống: 443, 8443, 9443 (Server), 9999 (Agent), 3000 (giao diện), 8081–8083 (ứng dụng nội bộ giả).
  Kiểm cổng 443 có bị chiếm không (không in gì là trống):
  ```powershell
  netstat -ano | findstr ":443 "
  ```

**Build một lần** ở thư mục gốc repo:
```powershell
.\ztna-server\mvnw.cmd -f pom.xml -DskipTests package
```
Sinh ra 3 file jar: `ztna-server\target\ztna-server-0.0.1-SNAPSHOT.jar`, `connector\target\connector-1.0-SNAPSHOT.jar`, `agent\target\agent-1.0-SNAPSHOT.jar`.

**(Tuỳ chọn) Bắt đầu với dữ liệu sạch:** dữ liệu mẫu chỉ được tạo khi database còn trống. Nếu `ztna_db` đã có dữ liệu cũ, kết quả có thể khác mô tả. Muốn làm lại từ đầu thì vào phpMyAdmin (http://localhost/phpmyadmin, phải bật Apache tạm thời, xong thì tắt) và xoá database `ztna_db`. **Thao tác này xoá toàn bộ dữ liệu ZTNA trên máy, không lấy lại được.**

### B1. Khởi động hệ thống (5 cửa sổ terminal)

Mở 5 cửa sổ PowerShell, cửa sổ nào cũng `cd` vào thư mục gốc repo. Làm đúng thứ tự.

**Cửa sổ 1 – ZTNA Server**

Cách 1, chạy jar (MySQL XAMPP **không** có mật khẩu):
```powershell
java -jar ztna-server\target\ztna-server-0.0.1-SNAPSHOT.jar --spring.datasource.password=
```
MySQL có mật khẩu thì thay bằng `--spring.datasource.password=<mật-khẩu>`. Nếu MySQL dùng đúng mật khẩu mặc định trong `application.yaml` thì bỏ hẳn tham số này.

Cách 2, chạy bằng Eclipse: chuột phải `ZtnaServerApplication.java` → **Run As** → **Run Configurations...** → tab **Arguments** → ô **Program arguments** gõ `--spring.datasource.password=` → **Run**.

Mong đợi trong log:
- `Started ZtnaServerApplication`
- Lần đầu chạy với DB trống: `ĐÃ TỰ ĐỘNG THÊM CONNECTOR DEMO` và `ĐÃ TỰ ĐỘNG THÊM 3 RESOURCE DEMO`
- Dòng cảnh báo `JWT_SECRET chưa đặt ... dùng secret ngẫu nhiên`. Đây là **đúng thiết kế** (xem mục N7 bên dưới).

**Cửa sổ 2 – Ứng dụng nội bộ giả (đóng vai web nội bộ)**
```powershell
node kiem-thu\web-gia.js
```
Mong đợi: `web-gia đang chạy ở cổng 8081`, `8082`, `8083`. Mỗi request đi qua tunnel sẽ được in ra ở cửa sổ này.

**Cửa sổ 3 – Connector**
```powershell
$env:SERVER_URL = "ws://localhost:8443/connector"
```
```powershell
$env:CONNECTOR_TOKEN = "<token connector mẫu, xem DataInit.java dòng 39>"
```
```powershell
java -jar connector\target\connector-1.0-SNAPSHOT.jar
```
Mong đợi: log connector có `WS handshake complete` và `ROUTING_RESPONSE`. Log Server có `[Connector] Registered` và `Routing table sent`.

**Cửa sổ 4 – Agent**
```powershell
java -jar agent\target\agent-1.0-SNAPSHOT.jar
```
Mong đợi:
- `[AgentConfig] Gateway URL: ws://localhost:9443/app (lấy từ mặc định)`
- Một dòng có `http://127.0.0.1:9999`, tức là Agent đã sẵn sàng.

**Cửa sổ 5 – Giao diện**
```powershell
cd ztna-portal-ui
```
```powershell
npm run dev
```
Mở Chrome: http://localhost:3000. Đăng nhập admin → vào trang quản trị (Bảng điều khiển). Đăng nhập user → vào `/portal`.

Mẹo: thao tác admin và user cùng lúc thì mở user trong cửa sổ **ẩn danh** (Ctrl+Shift+N), vì hai tài khoản dùng chung localStorage nếu cùng cửa sổ.

### B2. Kiểm từng lỗi đã sửa

Thứ tự dưới đây đã sắp để bước trước chuẩn bị cho bước sau. Cột "Mong đợi" là kết quả đúng. Nếu thấy khác, ghi lại log của Server (cửa sổ 1) và Agent (cửa sổ 4).

#### 1. T12 – Agent chạy bằng `java -jar`
Đã kiểm ở cửa sổ 4. Bản cũ báo `no main manifest attribute`; bản mới chạy được.

#### 2. T17 – Agent nhận địa chỉ Server cấu hình được
Dừng Agent (Ctrl+C ở cửa sổ 4) rồi thử 3 cách:

| Lệnh | Mong đợi |
|---|---|
| `java -jar agent\target\agent-1.0-SNAPSHOT.jar ws://<IP-LAN-máy-bạn>:9443/app` (IP lấy bằng `ipconfig`, dòng IPv4) | `Gateway URL: ws://<IP>:9443/app (lấy từ tham số dòng lệnh)` và vẫn kết nối được |
| `$env:SERVER_URL = "ws://<IP-LAN>:9443/app"` rồi chạy jar không tham số | `(lấy từ biến môi trường SERVER_URL)` |
| `java -jar agent\target\agent-1.0-SNAPSHOT.jar http://abc` | Báo `Địa chỉ Server phải có dạng ws://<host>:<port>/app` rồi dừng |

Xong thì xoá biến (`Remove-Item Env:SERVER_URL`) và chạy lại Agent như ở B1.

#### 3. T4 – Không có rule thì không mở được ứng dụng
Lúc này user chưa có rule nào.
```powershell
node kiem-thu\mo-app.js user
```
Script hỏi mật khẩu user và admin (xem DataInit.java).
Mong đợi:
- `Agent trả về: HTTP 403  {"error":"Bạn không có quyền truy cập ứng dụng này"}` và `=> KHÔNG được mở ứng dụng.`
- Cửa sổ 2 (web-gia) không in gì.
- Trang admin **Nhật ký & Kiểm toán → Log truy cập** có dòng **DENY** "Không có quyền truy cập Resource".

#### 4. T14 + luồng chuẩn – Cấp quyền rồi mở ứng dụng từ Portal
1. Admin: **Phân quyền → Quản lý quyền** → tạo rule tên `R-test`, chọn người dùng `user`, ứng dụng `Demo Web Nginx`, không đặt hạn → Lưu.
2. Cửa sổ ẩn danh: đăng nhập `user` → Portal hiện thẻ `Demo Web Nginx`, nhãn **WEB**, đúng icon web (bản cũ hiện sai icon do lệch chữ hoa/thường).
3. Bấm mở ứng dụng → Chrome mở tab `http://127.0.0.1:<cổng>` hiện "Ứng dụng nội bộ (cổng 8082)".
4. Cửa sổ 2 in một dòng `[web-gia:8082] <giờ> GET /`. Log truy cập có dòng **ALLOW**.

Kiểm cùng việc đó bằng script:
```powershell
node kiem-thu\mo-app.js user
```
Mong đợi: `HTTP 200 {"status":"success","port":...}` và `=> ĐÃ MỞ qua cổng local ...`.

#### 5. N1 – Nhiều người dùng chung một Agent không hưởng quyền của nhau
`alice` không có rule. Chạy liên tiếp, đổi thứ tự hai lần:
```powershell
node kiem-thu\mo-app.js user
```
```powershell
node kiem-thu\mo-app.js alice
```
```powershell
node kiem-thu\mo-app.js user
```
Mong đợi: `user` luôn **ĐÃ MỞ**, `alice` luôn **403 / KHÔNG được mở**, bất kể ai chạy trước. Bản cũ: alice chạy sau user thì vào được.

#### 6. N2 – Thu hồi quyền thì phiên đang mở bị cắt ngay
Cần một phiên giữ kết nối liên tục:
1. `node kiem-thu\mo-app.js user` → ghi lại số cổng trong dòng `ĐÃ MỞ qua cổng local <cổng>`.
2. Mở thêm 1 terminal:
   ```powershell
   node kiem-thu\giu-phien.js <cổng> 120
   ```
   Màn hình in `nhận phản hồi /lan-1`, `/lan-2`... mỗi giây.
3. Trong lúc đó, admin **tắt** rule `R-test` (công tắc ở trang Quản lý quyền).
4. Mong đợi: trong khoảng 1 giây, giu-phien in `>>> KẾT NỐI ĐÃ BỊ ĐÓNG <<<`.

Bật lại rule rồi lặp lại bước 1–4, mỗi lần thay bước 3 bằng **một** thao tác sau. Lần nào cũng phải thấy kết nối bị đóng:
- Khoá tài khoản `user` (Quản lý danh tính → Tất cả người dùng). Xong thì mở khoá lại.
- Sửa rule, bỏ `user` ra khỏi rule. Xong thì thêm lại.
- Tắt ứng dụng `Demo Web Nginx` (Quản lý tài nguyên → Ứng dụng). Xong thì bật lại.
- Tạo rule mới có **hạn sau 1–2 phút** (xoá hoặc tắt `R-test` trước), mở phiên, chờ qua giờ hạn: phiên bị cắt muộn nhất khoảng **15 giây** sau giờ hạn.

Thay đổi không liên quan (ví dụ sửa mô tả của ứng dụng khác) **không** được cắt phiên.

Trên trình duyệt: sau khi thu hồi quyền, bấm F5 ở tab ứng dụng → hiện trang "Không truy cập được ứng dụng".

#### 7. T18 – Ứng dụng nội bộ không chạy thì báo lỗi, không treo
1. Admin sửa ứng dụng `Demo Web Nginx`, đổi **cổng 8082 → 8099** (không có gì lắng nghe ở 8099) → Lưu. Đảm bảo rule `R-test` đang bật.
2. User mở ứng dụng từ Portal.
3. Mong đợi: tab Chrome hiện ngay trang **"Không truy cập được ứng dụng"** kèm lý do (HTTP 502), không quay vòng mãi. Bản cũ treo tới khi hết thời gian chờ.
4. Đổi cổng lại **8082**.

#### 8. T1 – Lỗi nghiệp vụ hiện thông báo rõ ràng thay vì 500
- Tạo rule mới trùng tên `R-test` → giao diện báo **"Tên Rule đã tồn tại"** (bản cũ: "Something went wrong").
- Tạo connector trùng tên với connector đã có → báo tên connector đã tồn tại.

#### 9. T2 – Không xoá được connector còn ứng dụng
Admin: **Quản lý tài nguyên → Connectors** → xoá `Local Agent Demo`.
Mong đợi: thông báo **"Connector đang có ứng dụng, hãy xoá hoặc chuyển các ứng dụng sang connector khác trước"**, connector vẫn còn. Bản cũ: lỗi 500 hoặc xoá luôn các ứng dụng.

#### 10. T3 – Tắt connector thì ngắt kết nối của nó
1. Mở một phiên `giu-phien.js` như mục 6.
2. Admin **tắt** connector `Local Agent Demo`.
3. Mong đợi:
   - giu-phien in `KẾT NỐI ĐÃ BỊ ĐÓNG`.
   - Cửa sổ 3 (connector) in `Disconnected`, sau đó cứ vài giây thử nối lại và bị ngắt tiếp.
   - Log Server có `Invalid or disabled connector`.
4. **Bật** lại connector → sau vài giây connector nối lại (log Server: `Registered`, `Routing table sent`), mở ứng dụng lại được.

Bản cũ: tắt connector rồi mà connector vẫn kết nối và phiên vẫn chạy.

#### 11. N4, N5, N6 – Token, đăng xuất, khoá tài khoản (script tự động)
Cần Server và Agent đang chạy. Script sẽ **khoá rồi mở khoá lại** tài khoản `user`.
```powershell
node kiem-thu\kiem-token.js
```
Script hỏi mật khẩu admin và user. Mong đợi dòng cuối: `Kết quả: 14 đạt, 0 không đạt`.
Script kiểm các ý sau:
- **N4:** refresh token (hạn 7 ngày) dùng làm access token: REST và Agent đều trả **401**.
- **N5:** `POST /auth/logout` trả **204**; sau đó dùng lại cookie refresh cũ trả **401**.
- **N6:** khoá user xong, token cũ gọi API trả **401 ngay** (bản cũ còn dùng được tới 15 phút), refresh bị từ chối, Agent từ chối; mở khoá thì token dùng lại được.

(Đã đối chứng: chạy script này với bản gốc chưa sửa thì có 4 mục KHÔNG ĐẠT.)

Kiểm thêm trên giao diện:
- **N5:** đăng nhập → bấm tên ở góc phải → **Đăng xuất** → quay về trang đăng nhập. Bấm nút Back của trình duyệt hoặc vào thẳng http://localhost:3000/dashboard → bị đưa về trang đăng nhập, không tự đăng nhập lại.
- **N6:** user đăng nhập trong cửa sổ ẩn danh; admin khoá user; user bấm F5 hoặc thao tác bất kỳ → bị đưa về trang đăng nhập.

#### 12. T5 – Kênh lạ không chèn được dữ liệu hay đóng phiên của người khác
1. Mở một phiên `giu-phien.js <cổng> 120` như mục 6.
2. Trong log Server (cửa sổ 1 hoặc Console của Eclipse), tìm dòng mới nhất có `SESSION_OPENED streamId=...`, copy chuỗi streamId (dạng `xxxxxxxx-xxxx-...`).
3. Mở terminal khác:
   ```powershell
   node kiem-thu\chen-stream.js <streamId>
   ```
4. Mong đợi:
   - Cửa sổ 2 (web-gia) **không** in `/CHEN-TRAI-PHEP`.
   - giu-phien vẫn in `nhận phản hồi` đều đặn, không bị đóng.
   - Log Server có 2 dòng `Từ chối frame: stream=... không thuộc kênh gửi` và `Bỏ qua CLOSE: stream=... không thuộc kênh gửi`.

#### 13. T11 – Build giao diện
```powershell
cd ztna-portal-ui
```
```powershell
npm run build
```
Mong đợi: kết thúc bằng `built in ...`, không có `error TS`.

#### 14. N7 – Secret và mật khẩu đọc từ biến môi trường
- Chạy Server không đặt `JWT_SECRET` → có cảnh báo `JWT_SECRET chưa đặt ... dùng secret ngẫu nhiên`. Khởi động lại Server thì người dùng phải đăng nhập lại; Portal tự lấy token mới nhờ refresh, đăng nhập lại không gặp lỗi 500.
- Đặt secret (≥ 32 ký tự) trước khi chạy Server:
  ```powershell
  $env:JWT_SECRET = "mot-chuoi-bi-mat-dai-it-nhat-32-ky-tu-123"
  ```
  Chạy lại Server trong cùng cửa sổ → **không** còn cảnh báo.
- Đổi DB bằng biến môi trường (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`). Ví dụ đặt `$env:DB_NAME = "ztna_db_thu"` rồi chạy lại Server → MySQL có thêm database `ztna_db_thu`.
- Danh sách đầy đủ: file `.env.example`.

Lưu ý PowerShell: `$env:DB_PASSWORD = ""` **xoá** biến chứ không đặt thành rỗng. Muốn mật khẩu rỗng thì dùng tham số `--spring.datasource.password=` như ở B1.

#### 15. T13 – Docker cho máy Server (chỉ khi máy có Docker Desktop)
**Chưa được kiểm**, vì máy dùng để sửa lỗi không có Docker. Nên thử trên máy nhóm trưởng trước buổi demo.
1. Tắt Server và MySQL XAMPP đang chạy (trùng cổng).
2. Ở thư mục gốc:
   ```powershell
   Copy-Item .env.example .env
   ```
   Mở `.env`, đặt `DB_PASSWORD` và `JWT_SECRET` (≥ 32 ký tự).
3. Chạy:
   ```powershell
   docker compose up -d --build
   ```
4. Mong đợi: `docker compose ps` có 3 dịch vụ `mysql`, `server`, `web` đang chạy; mở http://localhost:3000 đăng nhập được. Connector và Agent chạy như ở B1.
5. Tắt: `docker compose down` (dữ liệu vẫn giữ trong volume `ztna_mysql`).

### B3. Thử trên 2 máy trong LAN (nên làm trước buổi demo)

- **Máy Server:** chạy cửa sổ 1 và 5 (hoặc Docker), mở firewall cho cổng 443, 8443, 9443, 3000. Lấy IP bằng `ipconfig`.
- **Máy Connector:** chạy `web-gia.js`, rồi chạy connector với `SERVER_URL=ws://<IP-Server>:8443/connector`. Ứng dụng `localhost:8082` lúc này là localhost **của máy connector**.
- **Máy người dùng:** chạy Agent với tham số `ws://<IP-Server>:9443/app`, mở `http://<IP-Server>:3000`, đăng nhập user, mở ứng dụng.
- Script cũng chạy được từ máy khác: `$env:SERVER = "http://<IP-Server>:443"` trước khi chạy `kiem-token.js` hoặc `mo-app.js`.

### B4. Dọn dẹp

- Ctrl+C ở từng cửa sổ terminal để dừng Server, Connector, Agent, web-gia, giao diện.
- Xoá rule `R-test` nếu không cần nữa. Đổi lại cổng ứng dụng thành 8082 nếu đã đổi ở mục 7.
