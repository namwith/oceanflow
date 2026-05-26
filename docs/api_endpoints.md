# Tài liệu API

## Order

- `POST /api/orders`
  - Mô tả: tạo đơn hàng mới
  - Request body: `customerName`, `address`, `productIds`
  - Response: thông báo thành công

## Product

- `GET /api/products`
  - Mô tả: lấy danh sách sản phẩm
- `GET /api/products/{id}`
  - Mô tả: lấy chi tiết sản phẩm theo ID
