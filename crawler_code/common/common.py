import re
import os 

CP_CMD = "/bin/cp"
GIT_CMD = "/bin/git"

GIT_PATH = "/mnt/edisk/disk2/git/android_TV"
MAIN_LOG_NAME = "crawler.log"
TMP_DIR_PATH = GIT_PATH + "/crawler_code/logs"
MAIN_LOG_PATH = TMP_DIR_PATH + "/" + MAIN_LOG_NAME

STORE_SITE = GIT_PATH + "/video_site/"
DEBUG_SITE = GIT_PATH + "/video_site/debug/"


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
