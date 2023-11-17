import paramiko

def download_package_via_ssh(host, username, password, remote_path, local_path):
    # Create an SSH client
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    try:
        # Connect to the remote server
        ssh.connect(host, username=username, password=password)

        # Open an SFTP session on the SSH connection
        sftp = ssh.open_sftp()

        # Download the package from the remote server
        sftp.get(remote_path, local_path)

        print(f"Package downloaded from {host}:{remote_path} to {local_path}")

    except Exception as e:
        print(f"Error: {e}")

    finally:
        # Close the SFTP session and the SSH connection
        sftp.close()
        ssh.close()


"""
# pip install paramiko

host = 'your_remote_server'
username = 'your_username'
password = 'your_password'
remote_path = '/path/to/remote/package.tar.gz'
local_path = '/path/to/local/downloaded/package.tar.gz'

download_package_via_ssh(host, username, password, remote_path, local_path)        
"""