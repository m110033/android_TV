#!/usr/bin/env python
# -*- coding: utf8 -*-

from subprocess import Popen, PIPE, STDOUT
from datetime import datetime, timedelta
from pathlib import PurePath

import os
import sys
import time
import logging
import time
import shutil
import schedule

from common.common import (create_folder, GIT_PATH, GIT_CMD, MAIN_LOG_PATH, GDRIVE_CMD, GD_PATH, STORE_SITE, GDRIVE_OS)
from lib.myself import crawl_myself
from lib.gamer import crawl_gamer
from lib.laughseejapan import crawl_laughseejapan

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
        
def gdrive_update(reget = True):
    update_list = [ 
        { 'id': '11vw7GG9JtQfSbxRekzKDUfwBBFt1h0Sa', 'path':'gamer/gamer.json' },
        { 'id': '1_7xK1R-rDeETXHiMFDiXVIU9UASzOgnc', 'path':'myself/online_comic.json' },
        { 'id': '1HVozodmZLLfaUsvO4kPPAV9nrudINjql', 'path':'myself/end_comic.json' }
    ]

    if reget:
        # crawl_anime1(debug_mode = debug_mode, main_logger = main_logger)
        # crawl_maplestage(debug_mode = debug_mode, main_logger = main_logger)
        crawl_myself(debug_mode = debug_mode, main_logger = main_logger)
        crawl_gamer(debug_mode = debug_mode, main_logger = main_logger)
        # crawl_laughseejapan(debug_mode = debug_mode, main_logger = main_logger)
        # crawl_jikzy(debug_mode = debug_mode, main_logger = main_logger, depth = 6)

    for update_item in update_list:
        __id = update_item["id"]
        __path = PurePath(STORE_SITE)
        for path in update_item["path"].split('/'):
            __path = PurePath(__path, path)
        __path = str(__path)

        if os.path.exists(__path):
            run_cmd("%s --service-account credential.json update %s %s" % (GDRIVE_CMD, __id, __path))
            run_cmd("%s --service-account credential.json share --role reader --type anyone %s" % (GDRIVE_CMD, __id))

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
    # crawl_myself(debug_mode = debug_mode, main_logger = main_logger)
    # crawl_gamer(debug_mode = debug_mode, main_logger = main_logger)
    # crawl_maplestage(debug_mode = debug_mode, main_logger = main_logger)
    
    # Do git push
    run_cmd(GIT_CMD + " add -A")
    run_cmd(GIT_CMD + " commit -m \"Daily update the site of videos\"")
    run_cmd(GIT_CMD + " push")

'''
MAIN BLOCK
'''
main_logger = setup_logger('main_builder_logging', MAIN_LOG_PATH, level = logging.DEBUG)

if not debug_mode:
    # git_push()

    # Clear json
    shutil.rmtree(STORE_SITE, ignore_errors=True)
    os.makedirs(STORE_SITE)
    
    # define schedule
    schedule.every(30).minutes.do(gdrive_update)

    # Update json
    while True:
        schedule.run_pending()
        time.sleep(1)
else:
    # crawl_ikale()
    # crawl_lovemovie()
    # crawl_vmus()
    # crawl_meikutv(debug_mode = debug_mode, main_logger = main_logger)
    # crawl_myself(debug_mode = debug_mode, main_logger = main_logger)
    crawl_gamer(debug_mode = debug_mode, main_logger = main_logger)
    # crawl_maplestage(debug_mode = debug_mode, main_logger = main_logger)
    # crawl_laughseejapan(debug_mode = debug_mode, main_logger = main_logger)
    # crawl_jikzy(debug_mode = debug_mode, main_logger = main_logger)
    pass
