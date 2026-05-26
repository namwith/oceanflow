# Seafood Ecommerce

Dự án gồm 3 phần chính:

- `backend-api/`: Java Spring Boot
- `frontend-web/`: React + TailwindCSS
- `devops-infra/`: Docker Compose, PostgreSQL, Nginx

## Chạy nhanh

1. Mở terminal vào `seafood-ecommerce/devops-infra`
2. Chạy `docker compose up --build`

## Cấu trúc

- `backend-api/src/main/java/com/seafood`: config, controller, dto, entity, exception, repository, service
- `frontend-web/src`: components, context, hooks, pages, services, utils
- `devops-infra`: docker-compose, database init, nginx proxy

## Ghi chú

- Backend chạy mặc định `http://localhost:8080`
- Frontend dùng Vite và TailwindCSS
