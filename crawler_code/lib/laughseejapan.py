#!/usr/bin/env python
# -*- coding: utf8 -*-

import json
import sys
import os
import re
from datetime import datetime, timedelta
from bs4 import BeautifulSoup

from common.common import (create_folder, str_between, cleanhtml, STORE_SITE, DEBUG_SITE)
from movieClass import movie_class
from movieDetailClass import movie_detail_class
from common.request import request_func

today_date = datetime.now().strftime("%Y-%m-%d")

hosturl = "https://www.laughseejapan.com"
hosttype = ['variety', 'drama', 'anime']

def crawl_laughseejapan(debug_mode, main_logger):
    for index, host_type in enumerate(hosttype):
        # Reset the dict
        if debug_mode:
            cur_directory = DEBUG_SITE
        else:
            cur_directory = STORE_SITE

        movie_obj = movie_class()
        movie_detail_obj = movie_detail_class()

        for year in range(datetime.now().year, 2010, -1):
            video_url = "%s/%s?y=%s" % (hosturl, host_type, year)
            print("[crawl_laughseejapan] - %s" % video_url)        
            # Get source
            html = request_func(video_url)
            soup = BeautifulSoup(html, 'html5lib')
            query = soup.find_all('div', {'class': re.compile(r'.*watch-index-dark-theme')})
            print(len(query))
            for divtag in query:
                page_link = divtag.find('a').get("href").strip()
                title = divtag.find('h4').text.strip()
                img = divtag.find('img').get('data-src').strip()
                date_str = "%s-01-01" % (year)
                movie_obj.addMovie(
                    movie_title = title,
                    movie_image = img,
                    movie_page_link = page_link,
                    movie_second_title = date_str
                )
            if host_type == "variety":
                break
        json_str = movie_obj.dictToJson()
        if debug_mode:
            cur_directory = DEBUG_SITE
        else:
            cur_directory = STORE_SITE
        top_dir = cur_directory / 'laughseejapan'
        create_folder(top_dir)
        filePtr = open("%s/%s.json" % (top_dir, host_type), 'w')
        filePtr.write(json_str)
        filePtr.close()