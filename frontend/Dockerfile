# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run lint
RUN npm run build

# Serve stage
FROM nginx:alpine
# Copy built assets to Nginx html directory
COPY --from=build /app/dist /usr/share/nginx/html
# Copy custom Nginx configuration if needed (otherwise it uses default)
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
