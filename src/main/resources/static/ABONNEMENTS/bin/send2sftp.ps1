param (
    [Parameter(Mandatory=$true)]
    [string]$LocalFilePath,

    [Parameter(Mandatory=$true)]
    [string]$SftpPath
)

# Import Posh-SSH module
Import-Module Posh-SSH

# Define SFTP server details
$SftpServer = '172.31.120.85'
$Username = 'ADMIN_PREST'
$PrivateKeyPath = 'C:\Users\barci\.ssh\id_rsa' # Specify the path to your private key
#$PrivateKeyPath = 'C:\Users\ADMIN_PREST\.ssh\id_rsa' # Specify the path to your private key

# Establish SFTP session using private key
$Session = New-SFTPSession -ComputerName $SftpServer -Credential $Username -KeyFile $PrivateKeyPath

# Check if the directory exists, and create it if not
if (-not (Test-SFTPPath -SessionId $Session.SessionId -Path $SftpPath)) {
    New-SFTPItem -SessionId $Session.SessionId -Path $SftpPath -ItemType Directory
}

# Upload file to SFTP server
Set-SFTPItem -SessionId $Session.SessionId -Path $LocalFilePath -Destination $SftpPath

# Disconnect the session
Remove-SFTPSession -SessionId $Session.SessionId
