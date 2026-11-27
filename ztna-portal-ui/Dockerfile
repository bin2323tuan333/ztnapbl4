# Build giao diện React rồi phục vụ file tĩnh bằng nginx ở cổng 3000.
# Trình duyệt tự gọi API tại http://<hostname đang mở>:443 nên không cần cấu hình địa chỉ server.
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
RUN printf 'server {\n  listen 3000;\n  root /usr/share/nginx/html;\n  location / { try_files $uri /index.html; }\n}\n' > /etc/nginx/conf.d/default.conf
EXPOSE 3000
