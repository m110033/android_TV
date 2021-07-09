#!/usr/bin/env python
# -*- coding: utf8 -*-

import json
from datetime import datetime, timedelta

from common.common import (create_folder, str_between, cleanhtml, STORE_SITE, DEBUG_SITE)
from movieClass import movie_class
from common.request import request_func
from bs4 import BeautifulSoup

movie_obj = movie_class()
today_date = datetime.now().strftime("%Y-%m-%d")

def crawl_gamer(debug_mode, main_logger):
    # Reset the dict
    if debug_mode:
        cur_directory = DEBUG_SITE
    else:
        cur_directory = STORE_SITE

    video_url = 'https://ani.gamer.com.tw/animeList.php?page=1&c=0'
    html = request_func(video_url)
    page_num_str = str_between(html, "<div class=\"page_number\">", "/div>").strip()
    page_num_str = str_between(page_num_str, "...<a href=\"?page=", "&").strip()
    page_num = int(page_num_str)

    # uuid_list = []
    for page_index in range(1, page_num + 1):
        page_index_str = str(page_index)

        main_logger.debug("Parse the pages: " + page_index_str + "/" + page_num_str)
        video_url = 'https://ani.gamer.com.tw/animeList.php?page=' + page_index_str + '&c=0'

        # Get source
        html = request_func(video_url)
        html = html.replace('</i>', '')
        soup = BeautifulSoup(html, 'html.parser')
        for atags in soup.find_all('a', {'class': 'theme-list-main'}):
            page_link = "http://ani.gamer.com.tw/" + atags.get('href').strip()
            title = atags.find('p', {'class': 'theme-name'}).getText()
            img = str_between(atags.find('div', {'class': 'theme-img-bg'}).get('style'), '"', '"')
            yearTxt = atags.find('p', {'class': 'theme-time'}).getText() + "共"
            year_info = str_between(yearTxt, "年份：", "共").strip()
            try:
                date_obj = datetime.strptime(year_info, '%Y/%m')
                date_str = datetime.strftime(date_obj, '%Y-%m-%d')
            except:
                date_str = "1911-01-01"
            # print({
            #     "title": title,
            #     "img": img,
            #     "page_link": page_link,
            #     "date_str": date_str
            # })

            movie_obj.addMovie(
                movie_title = title,
                movie_image = img,
                movie_page_link = page_link,
                movie_second_title = date_str
            )
            # break
        # break
    json_str = movie_obj.dictToJson()

    if debug_mode:
        cur_directory = DEBUG_SITE
    else:
        cur_directory = STORE_SITE

    top_dir = cur_directory / 'gamer'
    create_folder(top_dir)

    filePtr = open(top_dir / 'gamer.json', 'w')
    filePtr.write(json_str)
    filePtr.close()
