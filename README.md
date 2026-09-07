# LibraX - Library Management System (Hệ thống Quản lý Thư viện)

Hệ thống quản lý thư viện của startup **LibraX** được xây dựng và tái cấu trúc bằng **Spring Boot** theo kiến trúc **Modular Monolith**, tinh gọn tối đa và tổ chức theo 3 domain nghiệp vụ: `book`, `member`, và `borrowing`.

---

## 1. Phân tích & Khắc phục các Lỗi Logic Ban Đầu

### 1.1. Lỗi logic trong hàm `canBorrowBook`

* **Đoạn code ban đầu**:
  ```java
  public boolean canBorrowBook(int currentBorrowedByMember) {
      if (currentBorrowedByMember > 5) {
          return false;
      }
      return true; // Sai: khi currentBorrowedByMember = 5 vẫn cho mượn thêm cuốn thứ 6
  }
  ```
* **Nguyên nhân**:
  - `currentBorrowedByMember` là số sách độc giả đang mượn trước khi mượn thêm.
  - Khi độc giả đã mượn 5 cuốn (`currentBorrowedByMember == 5`), biểu thức `5 > 5` trả về `false`, bỏ qua khối `if` và rơi vào `return true`.
  - Kết quả: Độc giả mượn được cuốn thứ 6, vi phạm giới hạn tối đa 5 cuốn.
* **Code đã sửa**:
  ```java
  public boolean canBorrowBook(int currentBorrowedByMember) {
      return currentBorrowedByMember < 5;
  }
  ```

---

### 1.2. Vấn đề biến static `totalBorrowedBooks` (Shared Mutable State)

* **Vấn đề**:
  - **Shared Mutable State**: Biến static chia sẻ trạng thái giữa tất cả các HTTP Request/Thread trong môi trường web đa luồng của Spring Boot $\rightarrow$ Gây **Race Condition** khi nhiều người mượn sách cùng lúc.
  - **Vi phạm đóng gói**: Đặt state ở Controller phá vỡ kiến trúc phân tầng.
  - **Mất dữ liệu theo từng độc giả & sách**: Chỉ đếm gộp toàn bộ thư viện, không biết ai đang mượn sách nào.
  - **Mất dữ liệu khi restart**.
* **Cách khắc phục**:
  - Loại bỏ hoàn toàn biến static `totalBorrowedBooks`.
  - Quản lý trạng thái mượn sách thông qua `BorrowingRepository`:
    ```java
    int currentBorrowed = borrowingRepository.countByMemberIdAndStatus(memberId, BorrowStatus.BORROWED);
    ```

---

## 2. Cấu trúc Package Tinh Gọn (3 Domain $\times$ 3 Tầng)

```
src/main/java/com/example/ex1/
├── Ex1Application.java
├── common/exception/
│   ├── BorrowLimitExceededException.java
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── book/
│   ├── controller/BookController.java
│   ├── model/Book.java
│   ├── repository/BookRepository.java
│   └── service/BookService.java
├── member/
│   ├── controller/MemberController.java
│   ├── model/Member.java
│   ├── repository/MemberRepository.java
│   └── service/MemberService.java
└── borrowing/
    ├── controller/BorrowingController.java
    ├── dto/BorrowRequest.java
    ├── dto/BorrowResponse.java
    ├── model/BorrowStatus.java
    ├── model/BorrowingRecord.java
    ├── repository/BorrowingRepository.java
    └── service/BorrowingService.java
```

---

## 3. Danh sách REST Endpoints Mẫu

* **Domain `book`**:
  - `GET /api/books`: Lấy danh sách tất cả các cuốn sách.
  - `GET /api/books/{id}`: Xem chi tiết 1 cuốn sách.
  - `POST /api/books`: Thêm sách mới.
* **Domain `member`**:
  - `GET /api/members`: Lấy danh sách tất cả độc giả.
  - `GET /api/members/{id}`: Xem thông tin 1 độc giả.
  - `POST /api/members`: Tạo độc giả mới.
* **Domain `borrowing`**:
  - `POST /api/borrowings`: Mượn sách (kiểm tra hạn mức < 5, cập nhật trạng thái sách).
    - Body mẫu: `{"memberId": 1, "bookId": 2}`
  - `GET /api/borrowings/member/{memberId}`: Lấy danh sách sách đang mượn của 1 độc giả.
  - `POST /api/borrowings/{id}/return`: Trả sách đã mượn.

---

## 4. Vì sao cấu trúc này vẫn là Monolithic Architecture chứ chưa phải Microservices?

1. **Chung đơn vị đóng gói (Single Deployment Unit)**: Cả 3 domain nằm chung trong 1 codebase và đóng gói thành **duy nhất 1 file JAR**.
2. **Chung tiến trình chạy (Single Process)**: Ứng dụng chạy trên **1 tiến trình JVM duy nhất**.
3. **Giao tiếp trong bộ nhớ (In-process Calls)**: Các tầng gọi hàm trực tiếp trên RAM qua Dependency Injection (`@RequiredArgsConstructor`), không gọi qua mạng (Network I/O như REST hay gRPC).
4. **Bộ nhớ & CSDL dùng chung**: Không tách riêng database độc lập theo từng service (chưa có *Database-per-Service*).
