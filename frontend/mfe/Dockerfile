# ==============================================================
# iBPMS V1: Dockerfile Multi-Stage para Micro-Frontends (MFE)
# ==============================================================

# ETAPA 1: Construcción (Node.js)
FROM node:20-alpine AS build

# 1.1 Build Admin Remote
WORKDIR /app/admin-remote
COPY admin-remote/package*.json ./
RUN npm install
COPY admin-remote/ .
RUN npm run build

# 1.2 Build Inbox Remote
WORKDIR /app/inbox-remote
COPY inbox-remote/package*.json ./
RUN npm install
COPY inbox-remote/ .
RUN npm run build

# 1.3 Build Host App
WORKDIR /app/host-app
COPY host-app/package*.json ./
RUN npm install
COPY host-app/ .
RUN npm run build

# ETAPA 2: Servidor Web (Nginx - Single Origin)
FROM nginx:alpine

# Limpiar Nginx default
RUN rm -rf /usr/share/nginx/html/*

# Copiar configuración custom (Reverse proxy local para MFE)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Trasladar artefactos distribuidos del Build
COPY --from=build /app/host-app/dist /usr/share/nginx/html/host
COPY --from=build /app/inbox-remote/dist /usr/share/nginx/html/inbox
COPY --from=build /app/admin-remote/dist /usr/share/nginx/html/admin

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
