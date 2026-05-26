# Thiết kế cơ sở dữ liệu

## Bảng `product`

- `id`: khóa chính
- `name`: tên sản phẩm hải sản
- `batch_code`: mã lô hàng
- `quantity`: số lượng tồn kho
- `price`: đơn giá VNĐ

## Mở rộng

- Bảng `orders` để lưu đơn hàng
- Bảng `order_items` để quản lý chi tiết sản phẩm trong đơn
- Bảng `batch` để quản lý FEFO / ngày sản xuất và hạn sử dụng
