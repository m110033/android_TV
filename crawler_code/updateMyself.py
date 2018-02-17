#!/usr/bin/env python
# -*- coding: utf8 -*-

from subprocess import Popen, PIPE, STDOUT
from datetime import datetime, timedelta

import os
import sys
import time
import logging
import time

from common.common import (create_folder, GIT_PATH, GIT_CMD, TMP_DIR_PATH, MAIN_LOG_PATH, GDRIVE_CMD)
from lib.myself import crawl_myself
from lib.gamer import crawl_gamer
from lib.maplestage import crawl_maplestage
from lib.meikutv import crawl_meikutv
from lib.anime1 import crawl_anime1

debug_mode = False

# Set Log config
main_logger = ""

'''
COMMON FUNCTION
'''
def run_cmd(cmd_str):
    datetime_str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    #print cmd_str

    process = Popen(cmd_str, shell=True, stdout=PIPE, stderr=PIPE)
    output, error = process.communicate()

    # no exception was raised
    if output.strip() != "":
        #print output
        pass
    elif error.strip() != "" and discard_error == True:
        #print error
        pass
    elif error.strip() != "" and discard_error == False:
        #print error
        pass
        sys.exit(0)

    return output

def setup_logger(logger_name, log_file, level=logging.DEBUG):
    l = logging.getLogger(logger_name)
    formatter = logging.Formatter(fmt='%(asctime)s\t%(levelname)s\t%(message)s', datefmt = "%Y-%m-%d %H:%M:%S")
    fileHandler = logging.FileHandler(log_file, mode='a')
    fileHandler.setFormatter(formatter)
    streamHandler = logging.StreamHandler()
    streamHandler.setFormatter(formatter)
    l.setLevel(level)
    l.addHandler(fileHandler)
    l.addHandler(streamHandler)
    return l

def run_cmd(cmd_str, discard_error = True):
    datetime_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    main_logger.info(cmd_str)

    try:
        process = Popen(cmd_str, shell=True, stdout=PIPE, stderr=PIPE)
        #process = Popen("echo 'test'", shell=True, stdout=PIPE, stderr=PIPE)
        output, error = process.communicate()
    except (OSError, CalledProcessError) as exception:
        main_logger.error("Exception occured: " + str(exception))
        main_logger.error("Subprocess failed")
        sys.exit(0)
    else:
        # no exception was raised
        if output.strip() != "":
            main_logger.info(output)
        elif error.strip() != "" and discard_error == True:
            main_logger.error(error)
        elif error.strip() != "" and discard_error == False:
            main_logger.error(error)
            sys.exit(0)

        return output

def gdrive_udpate(reget = True):
    update_list = [ 
        { 'id': '0B1_1ZUYYMDcrbFgtTlVnNC1PQUE', 'path':'/home/git/gdrive/video_site/gamer/gamer.json' },
        { 'id': '0B1_1ZUYYMDcrbG9fZlpLYzFEMWM', 'path':'/home/git/gdrive/video_site/maplestage/drama_kr.json' },
        { 'id': '0B1_1ZUYYMDcrbG0tSWxLNlZpMDQ', 'path':'/home/git/gdrive/video_site/maplestage/drama_cn.json' },
        { 'id': '0B1_1ZUYYMDcrbGh3cmt0STA2b2c', 'path':'/home/git/gdrive/video_site/maplestage/variety_kr.json' },
        { 'id': '0B1_1ZUYYMDcrbGhuSDJTanc2WWc', 'path':'/home/git/gdrive/video_site/maplestage/variety_tw.json' },
        { 'id': '0B1_1ZUYYMDcrbGVENHNOWHRKUkE', 'path':'/home/git/gdrive/video_site/maplestage/variety_cn.json' },
        { 'id': '0B1_1ZUYYMDcrbGRYSDEwZ2ljazA', 'path':'/home/git/gdrive/video_site/maplestage/drama_tw.json' },
        { 'id': '0B1_1ZUYYMDcrbGF6V2otT09GVmc', 'path':'/home/git/gdrive/video_site/maplestage/drama_ot.json' },
        { 'id': '0B1_1ZUYYMDcrbGtOX0hHdE5tOGs', 'path':'/home/git/gdrive/video_site/myself/online_comic.json' },
        { 'id': '0B1_1ZUYYMDcrbGtEbHZ1RFMtdGc', 'path':'/home/git/gdrive/video_site/myself/end_comic.json' }
        { 'id': '17es2XCOZ4sfieNe75lF3ST-zvNkWeSIF', 'path':'/home/git/gdrive/video_site/anime1/anime1.json' }
    ]

    if reget:
        crawl_anime1(debug_mode = debug_mode, main_logger = main_logger)
        crawl_myself(debug_mode = debug_mode, main_logger = main_logger)
        crawl_gamer(debug_mode = debug_mode, main_logger = main_logger)
        crawl_maplestage(debug_mode = debug_mode, main_logger = main_logger)

    for cur_update_item in update_list:
        cur_id = cur_update_item["id"]
        cur_path = cur_update_item["path"]
        
        run_cmd("%s update %s %s" % (GDRIVE_CMD, cur_id, cur_path))
        run_cmd("%s share --role reader --type anyone %s" % (GDRIVE_CMD, cur_id))

def git_push():
    # Change path
    os.chdir(GIT_PATH)

    # Do git pull
    #run_cmd(GIT_CMD + " checkout Develop")
    run_cmd(GIT_CMD + " pull")

    cur_day = datetime.today().weekday()

    now = datetime.now()

    if cur_day == 1 or cur_day == 5:    #禮拜二或禮拜六更新
        firstUpdateTime = now.replace(hour=12, minute=0, second=0, microsecond=0)
        if now < firstUpdateTime:
            # crawl_lovemovie()
            # crawl_vmus()
            # crawl_ikale()
            pass

    today5pm = now.replace(hour=17, minute=0, second=0, microsecond=0)
    today8pm = now.replace(hour=21, minute=0, second=0, microsecond=0)

    if now >= today5pm and now <= today8pm:
        pass
        # crawl_maplestage()

    #每天更新
    #crawl_myself(debug_mode = debug_mode, main_logger = main_logger)
    #crawl_gamer(debug_mode = debug_mode, main_logger = main_logger)
    crawl_maplestage(debug_mode = debug_mode, main_logger = main_logger)
    
    # Do git push
    run_cmd(GIT_CMD + " add -A")
    run_cmd(GIT_CMD + " commit -m \"Daily update the site of videos\"")
    run_cmd(GIT_CMD + " push")

'''
MAIN BLOCK
'''
create_folder(TMP_DIR_PATH)
main_logger = setup_logger('main_builder_logging', MAIN_LOG_PATH, level = logging.DEBUG)

if not debug_mode:
    # git_push()
    gdrive_udpate()
else:
    #crawl_ikale()
    #crawl_lovemovie()
    #crawl_vmus()
    #crawl_meikutv(debug_mode = debug_mode, main_logger = main_logger)
    crawl_myself(debug_mode = debug_mode, main_logger = main_logger)
    #crawl_gamer(debug_mode = debug_mode, main_logger = main_logger)
    #crawl_maplestage(debug_mode = debug_mode, main_logger = main_logger)
    pass
