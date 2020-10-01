const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
    app.use(createProxyMiddleware("/api", { target: "http://localhost:8080", xfwd: true }));
    app.use(createProxyMiddleware("/admin", { target: "http://localhost:8080", xfwd: true }));
    app.use(createProxyMiddleware("/ws", { target: "ws://localhost:8080", ws: true, xfwd: true }))
};