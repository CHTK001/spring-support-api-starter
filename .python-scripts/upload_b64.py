#!/usr/bin/env python3
"""Upload file via SSH using paramiko SFTP."""
import paramiko, sys, os

host = sys.argv[1]
port = int(sys.argv[2]) if len(sys.argv) > 2 else 22
local_path = sys.argv[3]
remote_path = sys.argv[4] if len(sys.argv) > 4 else "upload.tar.gz"
password = os.environ.get("SSH_PASS", "")

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
try:
    client.connect(host, port=port, username=os.environ.get("SSH_USER", "root"), password=password, timeout=30)
    sftp = client.open_sftp()

    # Check local file exists
    if not os.path.exists(local_path):
        print(f"Local file not found: {local_path}")
        sys.exit(1)
    print(f"Local file size: {os.path.getsize(local_path)}")

    # Check remote dir
    remote_dir = os.path.dirname(remote_path)
    try:
        sftp.stat(remote_dir)
        print(f"Remote dir exists: {remote_dir}")
    except:
        print(f"Remote dir NOT FOUND: {remote_dir}, trying to create")
        sftp.mkdir(remote_dir)

    sftp.put(local_path, remote_path)
    print(f"Uploaded to {remote_path}")

    # Verify
    sftp.stat(remote_path)
    print("Verify OK")
    sftp.close()
finally:
    client.close()
