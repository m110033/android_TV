#!/usr/bin/env python
# -*- coding: utf8 -*-

import json
import sys
import os
import re
from datetime import datetime, timedelta
from bs4 import BeautifulSoup
from opencc import OpenCC

from common.common import (create_folder, str_between, cleanhtml, STORE_SITE, DEBUG_SITE)
from movieClass import movie_class
from movieDetailClass import movie_detail_class
from common.request import request_func

today_date = datetime.now().strftime("%Y-%m-%d")

link_map = [
    {'id': '1', "name": '電影'},
    {'id': '2', "name": '連戲劇'},
    {'id': '4', "name": '動漫'},
    {'id': '12', "name": '陸劇'},
    {'id': '13', "name": '港劇'},
    {'id': '14', "name": '日劇'},
    {'id': '15', "name": '美劇'},
    {'id': '16', "name": '韓劇'}
]

hosturl = "https://www.jikzy.com"
cc = OpenCC('s2tw')

def crawl_jikzy(debug_mode, main_logger, depth = sys.maxsize):
    # Reset the dict
    if debug_mode:
        cur_directory = DEBUG_SITE
    else:
        cur_directory = STORE_SITE
    pages = []
    for index, linkItem in enumerate(link_map):
        movie_obj = movie_class()
        movie_detail_obj = movie_detail_class()
        pageTitle = linkItem.get('name')
        pageId = linkItem.get('id')
        pageNum = 1
        while pageNum <= depth:
            pageUrl = "%s/?m=vod-type-id-%s-pg-%s.html" % (hosturl, pageId, pageNum)
            html = request_func(pageUrl)
            soup = BeautifulSoup(html, 'html5lib')
            print(pageTitle, pageNum, pageUrl)
            dianItems = soup.find_all('tr', {'class': 'DianDian'})
            if len(dianItems) <= 0:
                break
            for pageItem in dianItems:
                data = [ d.strip() for d in pageItem.getText().split('\n') if d.strip() != ""]
                if len(data) <= 0:
                    continue
                page_link = "%s/%s" % (hosturl, pageItem.find('a', {'target': '_blank'}).get('href').strip())
                movie_obj.addMovie(
                    movie_title = cc.convert(data[0]),
                    movie_image = '',
                    movie_page_link = page_link,
                    movie_second_title = data[-1]
                )
            pageNum += 1
        json_str = movie_obj.dictToJson()
        if debug_mode:
            cur_directory = DEBUG_SITE
        else:
            cur_directory = STORE_SITE
        top_dir = cur_directory / 'jikzy'
        create_folder(top_dir)
        filePtr = open("%s/%s.json" % (top_dir, pageTitle), 'w')
        filePtr.write(json_str)
        filePtr.close()