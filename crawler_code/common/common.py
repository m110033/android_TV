import re
import os 
from pathlib import Path

GDRIVE_OS = "LINUX" #"WINDOWS" # LINUX

CP_CMD = "/bin/cp"
GIT_CMD = "/bin/git"
if GDRIVE_OS == "WINDOWS":
    ###
    # GDRRIVE LIB: https://github.com/prasmussen/gdrive
    # Installation Method
    # 1. Copy lib/gdrive.exe to "C:\Windows\System32\gdrive.exe"
    # 2. Add this path to windows' environment: PATH
    # 3. Set the AUTH
    # How to get authenticated: https://github.com/prasmussen/gdrive/wiki
    # Go to https://developers.google.com/oauthplayground.
    # Under the Drive API v3, select one of the following APIs:
    # https://www.googleapis.com/auth/drive.readonly
    # https://www.googleapis.com/auth/drive
    # Click Authorize APIs
    # Click Exchange Authorization Codes for Token
    # Copy the resulting Access token will allow you to run gdrive with --access-token. For some reason the Refresh token does not seem to work with --refresh-token.
    # Example: gdrive.exe --access-token ACCESS_TOKEN C:\Eric\github\crawler_code\video_site\gamer\gamer.json
   ###
    GDRIVE_CMD = "gdrive.exe"
else:
    GDRIVE_CMD = "/usr/bin/gdrive"

def cleanhtml(raw_html):
    cleanr = re.compile('<.*?>')
    cleantext = re.sub(cleanr, '', raw_html)
    return cleantext.strip()

def str_between(str, first, last = ""):
    try:
        start = str.index( first ) + len( first )
        if last == "":
            return str[start:]
        else:
            end = str.index( last, start )
            return str[start:end]
    except ValueError:
        return ""

def create_folder(folder_name):
    if not os.path.exists(folder_name):
        os.makedirs(folder_name)

GIT_PATH = "/mnt/edisk/disk2/git/android_TV"
GD_PATH = Path(os.path.abspath(__file__)).parents[1]
LOG_PATH = GD_PATH / "logs"
create_folder(LOG_PATH)
MAIN_LOG_PATH = LOG_PATH / "crawler.log"
GDRIVE_ID_PATH = GD_PATH / "client_secret.json"
STORE_SITE = GD_PATH / "video_site"
DEBUG_SITE = GD_PATH / "video_site" / "debug"
