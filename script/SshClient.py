#refer to https://github.com/hackersandslackers/paramiko-tutorial
"""Client to handle connections and actions executed against a remote host."""
import sys
import os
from loguru import logger
from os import system
import paramiko
from paramiko import SSHClient, AutoAddPolicy, RSAKey
from paramiko.auth_handler import AuthenticationException, SSHException
from scp import SCPClient, SCPException


logger.add(sys.stderr,
           format="{time} {message}",
           filter="client",
           level="INFO")
logger.add('logs/log_{time:YYYY-MM-DD}.log',
           format="{time} {level} {message}",
           filter="client",
           level="ERROR")


class SshClient:
    """Client to interact with a remote host via SSH & SCP."""

    def __init__(self, host, user, ssh_key_filepath, remote_path):
        self.host = host
        self.user = user
        self.ssh_key_filepath = ssh_key_filepath
        self.remote_path = remote_path
        self.client = None
        self.scp = None
        self.conn = None
        self._upload_ssh_key()

    def _get_ssh_key(self):
        """
        Fetch locally stored SSH key.
        """
        try:
            self.ssh_key = RSAKey.from_private_key_file(self.ssh_key_filepath)
            logger.info(f'Found SSH key at self {self.ssh_key_filepath}')
        except SSHException as error:
            logger.error(error)
        return self.ssh_key

    def _upload_ssh_key(self):
        try:
            system(f'ssh-copy-id -i {self.ssh_key_filepath} {self.user}@{self.host}>/dev/null 2>&1')
            system(f'ssh-copy-id -i {self.ssh_key_filepath}.pub {self.user}@{self.host}>/dev/null 2>&1')
            logger.info(f'{self.ssh_key_filepath} uploaded to {self.host}')
        except FileNotFoundError as error:
            logger.error(error)

    def _connect(self):
        """
        Open connection to remote host.
        """
        if self.conn is None:
            try:
                self.client = SSHClient()
                self.client.load_system_host_keys()
                self.client.set_missing_host_key_policy(AutoAddPolicy())
                self.client.connect(self.host,
                                    username=self.user,
                                    key_filename=self.ssh_key_filepath,
                                    look_for_keys=True,
                                    timeout=5000)
                self.scp = SCPClient(self.client.get_transport())
            except AuthenticationException as error:
                logger.info('Authentication failed: did you remember to create an SSH key?')
                logger.error(error)
                raise error
        return self.client

    def disconnect(self):
        """
        Close ssh connection.
        """
        self.client.close()
        self.scp.close()

    def bulk_upload(self, files):
        """
        Upload multiple files to a remote directory.

        :param files: List of strings representing file paths to local files.
        """
        self.conn = self._connect()
        uploads = [self.upload_file(file) for file in files]
        logger.info(f'Finished uploading {len(uploads)} files to {self.remote_path} on {self.host}')

    def upload_file(self, file):
        """Upload a single file to a remote directory."""
        try:
            self.conn = self._connect()
            self.scp.put(file,
                         recursive=True,
                         remote_path=self.remote_path)
        except SCPException as error:
            logger.error(error)
            raise error
        finally:
            logger.info(f'Uploaded {file} to {self.remote_path}')

    def download_file(self, file):
        """Download file from remote host."""
        self.conn = self._connect()
        self.scp.get(file)

    def execute_commands(self, commands):
        """
        Execute multiple commands in succession.

        :param commands: List of unix commands as strings.
        """
        self.conn = self._connect()
        for cmd in commands:
            stdin, stdout, stderr = self.client.exec_command(cmd)
            stdout.channel.recv_exit_status()
            response = stdout.readlines()
            for line in response:
                logger.info(f'INPUT: {cmd} | OUTPUT: {line}')


def main():
    ssh_key_file = os.getenv('SSH_KEY_FILE')
    client = SshClient("192.168.76.185", 'root', ssh_key_file, '/tmp')
    client.upload_file("./SshClient.py")
    client.execute_commands(['mv /tmp/SshClient.py /tmp/RemoteClient.py'])
    client.download_file('/tmp/RemoteClient.py')
    system('ls -l RemoteClient.py ')
    system(f'rm RemoteClient.py')


if __name__ == "__main__":
    main()