#!/usr/bin/env python
# -*- coding: utf8 -*-

import json
from datetime import datetime, timedelta

from common.common import (create_folder, str_between, cleanhtml, STORE_SITE, DEBUG_SITE)
from movieClass import movie_class
from common.request import create_opener

movie_obj = movie_class()
today_date = datetime.now().strftime("%Y-%m-%d")

def crawl_gamer(debug_mode, main_logger):
    # Reset the dict
    if debug_mode:
        cur_directory = DEBUG_SITE
    else:
        cur_directory = STORE_SITE

    video_url = 'https://ani.gamer.com.tw/animeList.php?page=1&c=0'

    # Get source
    opener = create_opener()
    response = opener.open(video_url)
    html = response.read()

    page_num_str = str_between(html, "<div class=\"page_number\">", "/div>").strip()
    page_num_str = str_between(page_num_str, "...<a href=\"?page=", "&").strip()
    page_num = int(page_num_str)

    # uuid_list = []
    for page_index in range(1, page_num + 1):
        page_index_str = str(page_index)

        main_logger.debug("Parse the pages: " + page_index_str + "/" + page_num_str)
        video_url = 'https://ani.gamer.com.tw/animeList.php?page=' + page_index_str + '&c=0'
        # Get source
        response = opener.open(video_url)
        html = response.read()

        htmlCode = str_between(html, "<ul class='anime_list'>", "</ul>").strip()
        htmlCode = htmlCode.split("<i class=\"material-icons\">")
        htmlCode.pop(0)

        if len(htmlCode) > 0:
            for data in htmlCode:
                page_link = "http://ani.gamer.com.tw/" + str_between(data, "<a href=\"", "\">")
                img = str_between(data, "data-bg=\"", "\"></div")
                video_uuid = str_between(data, "animeRef.php?sn=", "\"").strip()

                data_info = str_between(data, "<div class=\"info\">", "/div>")
                data_blocks = data_info.split('<b')
                if len(data_blocks) > 1:
                    title = str_between(data_blocks[1], ">", "</b>").strip()
                    temp_data = str_between(data_blocks[2], "r>", "<").strip()
                    year_info = str_between(temp_data, "年份：", "共").strip()
                    date_obj = datetime.strptime(year_info, '%Y/%m')
                    date_str = datetime.strftime(date_obj, '%Y-%m-%d')
                    ep_info = str_between(temp_data, "共", "集").strip()

                cur_play_number = int(ep_info)

                movie_obj.addMovie(
                    movie_title = title,
                    movie_image = img,
                    movie_page_link = page_link,
                    movie_second_title = date_str
                )
                continue

                # if video_uuid not in uuid_list: uuid_list.append(video_uuid)
                response = opener.open(page_link)
                video_html = response.read()

                htmlCode = str_between(video_html, "<div class=\"anime_name\">", "<div class=\"link\">")
                #title = str_between(htmlCode, "<h1>", "</h1>").strip()
                #video_uuid = str_between(htmlCode, "animefun.want2play(", ", this)").strip()
                intro = str_between(video_html, "<div class='data_intro'>", "<div class=\"link\">").strip()
                intro = cleanhtml(intro)

                data_type = str_between(htmlCode, "<ul class=\"data_type\">", "</ul>").strip()
                data_blocks = data_type.split("<span")
                if len(data_blocks) > 1:
                    video_type = str_between(data_blocks[1], "</span>", "</li>").strip()
                    video_author = str_between(data_blocks[3], "</span>", "</li>").strip()

                acg_score = str_between(htmlCode, "<div class=\"ACG-score\">", "</div>").strip()
                view_number = str_between(acg_score, "<span>", "人</span>")

                cur_movie_dict = movie_obj.setMovie(
                    uuid = video_uuid,
                    title = title,
                    page_link = page_link,
                    img = img,
                    movie_info = intro,
                    view_number = view_number,
                    movie_type = video_type,
                    movie_date = date_str,
                    cur_movie_play = ep_info,
                    last_updated = today_date,
                    movie_videos = [])

                cur_season_dict = movie_obj.setSeason(season_name = title, season_sites = [])

                video_data = str_between(htmlCode, "<section class=\"season\">", "</section>").strip()
                video_block = video_data.split('<a href=')
                video_block.pop(0)

                if len(video_block) == 0:
                    video_link = page_link
                    video_title = "1"
                    cur_video_dict = movie_obj.setVideo(video_name = video_title, video_mirrors = [])
                    cur_mirror_dict = movie_obj.setVideoMirror( video_mirror_name = "gamer",  video_mirror_url = video_link)
                    cur_video_dict["video_mirrors"].append(cur_mirror_dict)
                    try:
                        if len(cur_video_dict["video_mirrors"]) > 0:
                            cur_season_dict["season_sites"].append(cur_video_dict)
                    except:
                        main_logger.debug("No Season")
                else:
                    for cur_video_data in video_block:
                        video_link = "http://ani.gamer.com.tw/animeVideo.php" + str_between(cur_video_data, "\"", "\">").strip()
                        video_title = str_between(cur_video_data, "\">", "</a>").strip()

                        if video_title == "":
                            continue

                        cur_video_dict = movie_obj.setVideo(video_name = video_title, video_mirrors = [])
                        cur_mirror_dict = movie_obj.setVideoMirror( video_mirror_name = "gamer",  video_mirror_url = video_link)
                        cur_video_dict["video_mirrors"].append(cur_mirror_dict)

                        try:
                            if len(cur_video_dict["video_mirrors"]) > 0:
                                cur_season_dict["season_sites"].append(cur_video_dict)
                        except:
                            main_logger.debug("No Season")
                try:
                    if len(cur_season_dict["season_sites"]) > 0:
                        cur_movie_dict["movie_videos"].append(cur_season_dict)
                    else:
                        main_logger.debug("%s, Can't find any google videos" % (video_uuid))
                except:
                    main_logger.debug("No Video")

                try:
                    if len(cur_movie_dict["movie_videos"]) > 0:
                        movie_obj.addMovie(cur_movie_dict)
                except:
                    main_logger.debug("Add movie into obj failed")
                #break
        #break

    # movie_obj.remove_unused_uuid(uuid_list)

    json_str = movie_obj.dictToJson()

    if debug_mode:
        cur_directory = DEBUG_SITE
    else:
        cur_directory = STORE_SITE

    top_dir = cur_directory + '/gamer'
    create_folder(top_dir)

    if debug_mode:
        filePtr = open(top_dir + '/gamer.json', 'w')
        filePtr.write(json_str)
        filePtr.close()
