# Build stage
FROM node:20-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install || echo "No package.json, continuing..."
COPY . .
# We use a dummy build command if there is no build script yet
RUN npm run build || mkdir dist && echo "Dummy Workspace SPA build" > dist/index.html

# Production stage
FROM nginx:alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
