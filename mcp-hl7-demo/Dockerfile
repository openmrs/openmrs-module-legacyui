FROM node:18-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY mock-server.js .

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s \
  CMD node -e "fetch('http://localhost:3000/health').then(r => r.ok ? process.exit(0) : process.exit(1)).catch(() => process.exit(1))"

CMD ["node", "mock-server.js"]
