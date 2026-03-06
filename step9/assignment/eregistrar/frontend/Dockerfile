FROM node:18-alpine
WORKDIR /app
COPY package.json .
RUN npm install
COPY public ./public
COPY src ./src
ENV REACT_APP_API_URL=http://localhost:8080
EXPOSE 3000
CMD ["npm", "start"]
