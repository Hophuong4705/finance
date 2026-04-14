# 📱 FinanceMe - Trợ lý Tài chính & Quản lý Thuế TNCN

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-43853D?style=for-the-badge&logo=node.js&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)

**FinanceMe** là ứng dụng di động thông minh hỗ trợ người dùng quản lý thu chi cá nhân, tự động đọc biến động số dư từ tin nhắn SMS ngân hàng, tính toán thuế thu nhập cá nhân (TNCN) chuẩn xác và đồng bộ dữ liệu đa nền tảng.

Dự án được xây dựng dựa trên kiến trúc **Clean Architecture + MVVM**, áp dụng triệt để cơ chế **Offline-First** giúp người dùng trải nghiệm mượt mà, tốc độ phản hồi < 10ms ngay cả khi không có kết nối mạng.

---

## 🎯 Chức năng nổi bật (Core Features)

- ⚡ **Offline-First & Auto Sync:** Mọi giao dịch được lưu ngay lập tức vào **Room Database** (Local DB) để đảm bảo UI không bao giờ bị block. Khi có kết nối mạng, tiến trình ngầm (Background Coroutines) sẽ tự động đồng bộ (Push/Pull) dữ liệu bị kẹt lên **MongoDB** qua RESTful API một cách an toàn.
- 🔒 **Bảo mật Sinh trắc học & Chống Leak State:** Hỗ trợ đăng nhập siêu tốc bằng Vân tay/FaceID (`BiometricPrompt`). Hệ thống tích hợp cơ chế tự động dọn dẹp RAM (`AppGlobalState`) và Local DB khi người dùng đăng xuất hoặc khi phát hiện tài khoản đã bị xóa khỏi Server (Ghost User Prevention).
- 📩 **Smart SMS Parser & High-Priority Notifications:** Tự động lắng nghe tin nhắn SMS từ các ngân hàng (MB Bank, Vietcombank, MoMo,...), bóc tách số tiền và đẩy thông báo nổi (Heads-up Notification v3) kèm âm thanh ngay lập tức, đồng thời lưu trữ log biến động lên Cloud.
- 🎨 **UI/UX Hiện đại & Sắc nét:** Giao diện được code 100% bằng **Jetpack Compose** (Material Design 3). Tối ưu hóa bảng màu chủ đề (Theme & Typography) với độ tương phản cao, đảm bảo hiển thị sắc nét, chống tàng hình chữ trên cả chế độ Light/Dark Mode.
- 🧮 **Engine Tính thuế TNCN Lũy tiến:** Tích hợp bộ máy tính thuế chuẩn quy định pháp luật Việt Nam, tự động trừ gia cảnh cá nhân và người phụ thuộc, tính toán chi tiết qua 7 bậc thuế.
- 💱 **Tỷ giá Ngoại tệ & Vàng:** Liên tục cập nhật tỷ giá USD, EUR và Vàng SJC theo thời gian thực (Real-time).

---

## 🛠️ Công nghệ & Kiến trúc (Tech Stack)

### 1. Nền tảng Ứng dụng (Android Client)
- **Ngôn ngữ:** Kotlin
- **UI Toolkit:** Jetpack Compose, Material 3
- **Kiến trúc:** MVVM (Model-View-ViewModel) + Single Source of Truth
- **Quản lý Dependency:** Dagger - Hilt
- **Bất đồng bộ:** Kotlin Coroutines & Flow (`StateFlow`, `Dispatchers.IO`)
- **Local Database:** Room Persistence Library
- **Networking:** Retrofit2, OkHttp3

### 2. Máy chủ (Backend Server)
- **Framework:** Node.js + Express.js
- **Database:** MongoDB + Mongoose (Sử dụng `bulkWrite` và `upsert` để tối ưu đồng bộ số lượng lớn).
- **Authentication:** JWT (JSON Web Token) + Bcrypt (Hash Password)
- **Logging:** Custom Logger ghi nhận nhật ký thao tác người dùng.

---

## 🔄 Luồng dữ liệu Đồng bộ (Sync Data Flow)

Dự án áp dụng cơ chế **Single Source of Truth** (SSOT), trong đó `Room Database` là nguồn cung cấp dữ liệu duy nhất cho UI.

**Quy trình chuẩn khi Thêm/Xóa một giao dịch:**
1. Người dùng thao tác trên UI.
2. `ViewModel` tiếp nhận, đẩy lệnh xuống `Repository` chạy trên luồng `Dispatchers.IO`.
3. `Repository` lưu/xóa dữ liệu trực tiếp trong **Room DB** (Gắn cờ `isSynced = false`).
4. Giao diện (Compose) lập tức tự cập nhật thông qua luồng `StateFlow` (App phản hồi tức thì).
5. Lệnh `syncNow()` được kích hoạt ngầm. Nó gom tất cả dữ liệu chưa đồng bộ gửi lên Backend thông qua **Retrofit**.
6. **Node.js** nhận dữ liệu, ghi đè vào **MongoDB**, và trả về Response `success`.
7. Android cập nhật lại cờ `isSynced = true` trong Room DB.

*(Đặc biệt: Hệ thống tích hợp sẵn luồng bắt mã lỗi `401 - ACCOUNT_DELETED` từ Server để tự động Kick người dùng và hủy dữ liệu rác nếu tài khoản không còn hợp lệ).*

---

## 🗄️ Cấu trúc Cơ sở dữ liệu (Database Schema)

### 1. MongoDB (Cloud)
** Collection users:

_id: ObjectId

name: String

email: String (Unique)

password: String (Hashed)

avatarUri: String

** Collection transactions:

_id: ObjectId

userId: ObjectId (Ref -> users)

amount: Number

type: String (Enum: "INCOME", "EXPENSE")

source: String (MB Bank, MoMo, Tiền mặt...)

note: String

date: Number (Timestamp - Long)

isSynced: Boolean

** Collection notifications:

_id: ObjectId

userId: ObjectId (Ref -> users)

title: String (Tiêu đề thông báo)

message: String (Nội dung chi tiết biến động)

time: String (Thời gian định dạng "HH:mm dd/MM")

isRead: Boolean (Trạng thái đã đọc hay chưa)

### 2. Room Database (Local Android)
Table transactions_table:
Lưu trữ giao dịch tương tự MongoDB phục vụ cơ chế Offline-first.

Table notifications_table:
Lưu trữ nhật ký thông báo cục bộ để hiển thị ngay lập tức khi người dùng mở màn hình Thông báo.

---

## 🚀 Hướng dẫn cài đặt & Chạy dự án

### 1. Chạy Backend (Node.js)
```bash
# Di chuyển vào thư mục backend
cd backend-finance-me

# Cài đặt thư viện
npm install

# Tạo file .env và cấu hình Database
MONGO_URI=mongodb+srv://<username>:<password>@cluster...
JWT_SECRET=your_jwt_secret
PORT=3000

# Khởi chạy server
npm start
