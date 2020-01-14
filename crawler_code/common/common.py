import re
import os 
from pathlib import Path

GDRIVE_OS = "WINDOWS" # LINUX

CP_CMD = "/bin/cp"
GIT_CMD = "/bin/git"
if GDRIVE_OS == "WINDOWS":
    ###
    # Installation Method
    # 1. Copy lib/gdrive.exe to "C:\Windows\System32\gdrive.exe"
    # 2. Add this path to windows' environment: PATH
    # 3. Use the following cmd to register a0985510 with browser: gdrive.exe update 0B1_1ZUYYMDcrbFgtTlVnNC1PQUE D:\程式相關\程式設計\Python\tvapp\crawler_code\video_site\gamer\gamer.json
    # 4. If use the wrong account to register, go to "C:\Users\eric1\AppData\Roaming\.gdrive\token_v2.json" to delete the token file
   ###
    GDRIVE_CMD = "gdrive.exe"
else:
    GDRIVE_CMD = "/usr/sbin/gdrive"

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