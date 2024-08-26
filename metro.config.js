const { getDefaultConfig, mergeConfig } = require("@react-native/metro-config");
const connect = require("connect");
const WebSocket = require("ws");

/**
 * Metro configuration
 * https://facebook.github.io/metro/docs/configuration
 *
 * @type {import('metro-config').MetroConfig}
 */
const config = {
  server: {
    enhanceMiddleware: (metroMiddleware, metroServer) => {
      const wss = new WebSocket.Server({ host: "localhost", port: 4000 });

      let conn = 0;

      wss.on("connection", (socket) => {
        const id = ++conn;
        console.log(`client ${id} connected`);

        socket.on("message", (data) => {
          console.log(
            `received message from client ${id}:`,
            data.toString("utf-8")
          );
        });

        socket.on("close", () => {
          console.log(`client ${id} disconnected`);
        });
      });

      return connect()
        .use(metroMiddleware)
        .use("/async-storage", (req, res) => {
          res.writeHead(200, "OK", { "content-type": "application/json" });
          res.write(JSON.stringify("ok"));
          res.end();
        });
    },
  },
};

module.exports = mergeConfig(getDefaultConfig(__dirname), config);
