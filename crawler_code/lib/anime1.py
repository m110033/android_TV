#!/usr/bin/env python
# -*- coding: utf8 -*-

import json
import re
from datetime import datetime, timedelta

from common.common import (create_folder, str_between, cleanhtml, STORE_SITE, DEBUG_SITE)
from movieClass import movie_class
from common.request import create_opener

movie_obj = movie_class()
today_date = datetime.now().strftime("%Y-%m-%d")

def crawl_anime1(debug_mode, main_logger):
    # Reset the dict
    if debug_mode:
        cur_directory = DEBUG_SITE
    else:
        cur_directory = STORE_SITE

    video_url = 'https://anime1.me'

    date_map = {"冬": 1, "春": 4, "夏": 7, "秋": 10}
    
    # Get source
    opener = create_opener()
    response = opener.open(video_url)
    html = response.read()

    htmlCode = str_between(html, "<tbody class=\"row-hover\">", "</tbody>").strip()
    regObj = re.findall('<a href="([a-zA-Z0-9/=?]+)">(.+?)</a>.*?<td class="column-3">([0-9]+)</td><td class="column-4">(.+?)</td>', htmlCode)
    for cur_match in regObj:
        page_link = "%s%s" % (video_url, cur_match[0])
        title = cur_match[1]
        year_info = "%s/%s" % (cur_match[2], date_map[cur_match[3]])
        date_obj = datetime.strptime(year_info, '%Y/%m')
        date_str = datetime.strftime(date_obj, '%Y-%m-%d')
        movie_obj.addMovie(
            movie_title = title,
            movie_image = '',
            movie_page_link = page_link,
            movie_second_title = date_str
        )

    json_str = movie_obj.dictToJson()

    if debug_mode:
        cur_directory = DEBUG_SITE
    else:
        cur_directory = STORE_SITE

    top_dir = cur_directory + '/anime1'
    create_folder(top_dir)

    filePtr = open(top_dir + '/anime1.json', 'w')
    filePtr.write(json_str)
    filePtr.close()
