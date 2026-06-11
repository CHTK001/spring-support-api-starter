import os
import select
import socket
import socketserver
import sys
import threading

import paramiko


SSH_HOST = os.environ.get("SPIDER_TEST_SSH_HOST", "8.139.4.229")
SSH_PORT = int(os.environ.get("SPIDER_TEST_SSH_PORT", "22"))
SSH_USER = os.environ.get("SPIDER_TEST_SSH_USER", "root")
SSH_PASSWORD = os.environ.get("SPIDER_TEST_SSH_PASSWORD")
DB_HOST = os.environ.get("SPIDER_TEST_DB_HOST", "172.16.0.40")
DB_PORT = int(os.environ.get("SPIDER_TEST_DB_PORT", "3306"))
LOCAL_HOST = os.environ.get("SPIDER_TEST_LOCAL_HOST", "127.0.0.1")
LOCAL_PORT = int(os.environ.get("SPIDER_TEST_LOCAL_PORT", "13306"))


class ForwardServer(socketserver.ThreadingTCPServer):
    daemon_threads = True
    allow_reuse_address = True


class Handler(socketserver.BaseRequestHandler):
    transport = None

    def handle(self):
        try:
            channel = self.transport.open_channel(
                "direct-tcpip",
                (DB_HOST, DB_PORT),
                self.request.getpeername(),
            )
        except Exception as exc:
            print(f"open channel failed: {exc}", file=sys.stderr, flush=True)
            return
        if channel is None:
            print("open channel failed: channel is none", file=sys.stderr, flush=True)
            return

        try:
            while True:
                readable, _, _ = select.select([self.request, channel], [], [], 60)
                if self.request in readable:
                    data = self.request.recv(16384)
                    if not data:
                        break
                    channel.sendall(data)
                if channel in readable:
                    data = channel.recv(16384)
                    if not data:
                        break
                    self.request.sendall(data)
        finally:
            channel.close()
            self.request.close()


def connect_ssh():
    if not SSH_PASSWORD:
        raise SystemExit("SPIDER_TEST_SSH_PASSWORD is required")
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(
        SSH_HOST,
        port=SSH_PORT,
        username=SSH_USER,
        password=SSH_PASSWORD,
        timeout=15,
        banner_timeout=15,
        auth_timeout=15,
        look_for_keys=False,
        allow_agent=False,
    )
    transport = client.get_transport()
    if transport is None or not transport.is_active():
        raise SystemExit("SSH transport is not active")
    transport.set_keepalive(30)
    return client, transport


def main():
    client, transport = connect_ssh()
    Handler.transport = transport
    server = ForwardServer((LOCAL_HOST, LOCAL_PORT), Handler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    print(
        f"forwarding {LOCAL_HOST}:{LOCAL_PORT} -> {SSH_HOST}:{SSH_PORT} -> {DB_HOST}:{DB_PORT}",
        flush=True,
    )
    try:
        thread.join()
    except KeyboardInterrupt:
        pass
    finally:
        server.shutdown()
        server.server_close()
        client.close()


if __name__ == "__main__":
    main()
