# 📱 FinanceMe - Trợ lý Tài chính & Quản lý Thuế TNCN (Đề tài 10)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-43853D?style=for-the-badge&logo=node.js&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)

**FinanceMe** là ứng dụng di động hỗ trợ người dùng quản lý thu chi cá nhân, tự động đọc biến động số dư từ tin nhắn SMS ngân hàng, tính toán thuế thu nhập cá nhân (TNCN) và đồng bộ dữ liệu đa nền tảng.

Dự án được xây dựng dựa trên kiến trúc **MVVM (Model-View-ViewModel)**, áp dụng triệt để cơ chế **Offline-first** giúp người dùng trải nghiệm mượt mà ngay cả khi không có kết nối mạng.

---

## 🎯 Chức năng nổi bật (Core Features)

- 🔒 **Bảo mật sinh trắc học:** Đăng nhập bằng Vân tay/FaceID thay vì mật khẩu truyền thống.
- ⚡ **Offline-First & Auto Sync:** Mọi giao dịch được lưu ngay lập tức vào **Room Database** (Local) để đảm bảo UI không bị block. Khi có mạng, hệ thống tự động đồng bộ (Push/Pull) lên **MongoDB** qua RESTful API.
- 📩 **Smart SMS Parser:** Tự động lắng nghe tin nhắn SMS từ các ngân hàng (MB Bank, Vietcombank, Techcombank, MoMo,...), bóc tách số tiền và tạo giao dịch tự động.
- 📊 **UI/UX hiện đại:** Sử dụng 100% **Jetpack Compose** với các animation mượt mà (AnimatedContent, SwipeToDismiss, Canvas Chart).
- 🧮 **Tính thuế TNCN lũy tiến:** Tích hợp engine tính thuế chuẩn quy định pháp luật Việt Nam (Tự động trừ gia cảnh, tính thuế 7 bậc).

---

## 🛠️ Công nghệ & Kiến trúc (Tech Stack & Architecture)

### 1. Nền tảng Ứng dụng (Android Client)
- **Ngôn ngữ:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material Design 3)
- **Kiến trúc:** Clean Architecture + MVVM
- **Dependency Injection:** Dagger - Hilt
- **Asynchronous/Reactive:** Kotlin Coroutines & Flow (StateFlow)
- **Local Database:** Room Persistence Library
- **Networking:** Retrofit2, OkHttp3

### 2. Máy chủ (Backend Server)
- **Framework:** Node.js + Express.js
- **Database:** MongoDB + Mongoose
- **Authentication:** JWT (JSON Web Token) + Bcrypt (Hash Password)

---

## 🔄 Luồng dữ liệu (Data Flow)

Dự án xử lý dữ liệu theo cơ chế **Single Source of Truth** (SSOT), trong đó Room Database đóng vai trò là nguồn dữ liệu gốc cho UI.

**Quy trình Thêm một giao dịch (Offline-first Concept):**
1. **User / SMS Receiver** tạo một giao dịch mới.
2. **ViewModel** nhận dữ liệu, gọi `Repository` thực hiện lưu vào **Room DB** (Trạng thái `isSynced = false`).
3. UI (Compose) lập tức cập nhật thông qua `StateFlow` (App phản hồi < 10ms).
4. `ViewModel` kích hoạt hàm `syncNow()`. Gửi danh sách các giao dịch chưa đồng bộ (`isSynced = false`) lên Backend thông qua **Retrofit**.
5. **Node.js API** nhận dữ liệu, ghi vào **MongoDB**.
6. Nếu thành công, Backend trả về Response. Android cập nhật lại trạng thái `isSynced = true` trong Room DB.

---

## 🗄️ Cấu trúc Cơ sở dữ liệu (Database Schema)

### 1. MongoDB (Cloud)
**Collection `users`:**
- `_id`: ObjectId
- `name`: String
- `email`: String (Unique)
- `password`: String (Hashed)
- `avatarUri`: String
- `createdAt`: Date

**Collection `transactions`:**
- `_id`: ObjectId
- `userId`: ObjectId (Ref -> users)
- `amount`: Number
- `type`: String (Enum: "INCOME", "EXPENSE")
- `category`: String
- `source`: String (MB Bank, MoMo, Tiền mặt...)
- `note`: String
- `date`: Number (Timestamp)

### 2. Room Database (Local Android)
**Table `transactions_table`:**
Lưu trữ cấu trúc tương tự MongoDB nhưng bổ sung thêm field:
- `id`: PrimaryKey (Auto Generate)
- `isSynced`: Boolean (Cờ đánh dấu giao dịch đã được đẩy lên server hay chưa)

---

## 🌐 API Endpoints (Backend RESTful)

| Phương thức | Endpoint | Chức năng | Phân quyền |
|---|---|---|---|
| `POST` | `/api/auth/register` | Đăng ký tài khoản | Public |
| `POST` | `/api/auth/login` | Đăng nhập, cấp JWT Token | Public |
| `GET` | `/api/rates` | Lấy tỷ giá ngoại tệ / Vàng SJC | Public |
| `POST` | `/api/transactions/sync` | Đồng bộ giao dịch (Xóa cũ, ghi đè mới) | Bearer Token |
| `POST` | `/api/notifications/sync`| Đồng bộ danh sách thông báo | Bearer Token |

---

## 🚀 Hướng dẫn cài đặt & Chạy dự án

### 1. Chạy Backend (Node.js)
```bash
# Di chuyển vào thư mục backend
cd backend-finance-me

# Cài đặt thư viện
npm install

# Tạo file .env và cấu hình (Sửa lại chuỗi kết nối MongoDB của bạn)
MONGO_URI=mongodb+srv://<username>:<password>@cluster...
JWT_SECRET=my_super_secret_key
PORT=3000

# Khởi chạy server
npm start