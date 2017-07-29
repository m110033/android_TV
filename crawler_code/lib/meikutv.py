#!/usr/bin/env python
# -*- coding: utf8 -*-

import json
import sys
import os
from datetime import datetime, timedelta

from common.common import (create_folder, str_between, cleanhtml, STORE_SITE, DEBUG_SITE)
from movieClass import movie_class
from common.request import create_opener

movie_obj = movie_class()
today_date = datetime.now().strftime("%Y-%m-%d")

def crawl_meikutv(debug_mode, main_logger):
    site_name = "/meikutv"

    site_list = [
        'http://www.meikutv.club/Korea/%E9%9F%93%E5%9C%8B%E7%B6%9C%E8%97%9D/ON%E6%AA%94'
    ]

    site_name_list = [
        'kr_variety'
    ]

    for index, cur_site_name in enumerate(site_name_list):
        # Reset the dict
        if debug_mode:
            cur_directory = "%s/%s" % (DEBUG_SITE, site_name)
        else:
            cur_directory = "%s/%s" % (STORE_SITE, site_name)

        cur_site_link = site_list[index]

        if debug_mode:
            movie_obj.reset(cur_directory + '/' + cur_site_name + '_debug.json')
    else:
        movie_obj.reset(cur_directory + '/' + cur_site_name + '.json')

        try:
            uuid_list = []

            main_logger.debug("Target Site Link: %s" % (cur_site_link))
            # Get source
            opener = create_opener()
            response = opener.open(cur_site_link)
            html = response.read()

            htmlCode = str_between(html, "views-bootstrap-grid-plugin-style", "</section>").strip()
            htmlCode = htmlCode.split("field-content")
            htmlCode.pop(0)

            if len(htmlCode) > 0:
                for data in htmlCode:
                    title = str_between(data, "alt=\"", "\"").strip();

                    if title == "":
                        continue

                    realLink = str_between(data, "<a href=\"", "\"");
                    video_uuid = str_between(realLink, "http://www.meikutv.club/series-page/").strip();
                    img = "http:" + str_between(data, "<img src=\"", "\"");
                    intro = str_between(data, "</p>", "</p>").strip().replace("<p>", "");
                    intro = cleanhtml(intro).strip()
                    cur_movie_json_obj = movie_obj.getMovie(video_uuid)

                    main_logger.debug("Title: %s\nLink:%s\nIMG:%s\nUUID:%s\nINFO:%s" % (title, realLink, img, video_uuid, intro))

                    if video_uuid not in uuid_list: uuid_list.append(video_uuid)

                    cur_movie_dict = movie_obj.setMovie(
                        uuid = video_uuid,
                        title = title,
                        page_link = realLink,
                        img = img,
                        movie_info = intro,
                        view_number = "0",
                        movie_type = "meikutv",
                        movie_videos = [])

                    response = opener.open(realLink)
                    video_html = response.read()

                    htmlCode = str_between(video_html, "view-id-all_episodes", "</section>")
                    htmlblocks = htmlCode.split('field-content')
                    htmlblocks.pop(0)

                    cur_season_dict = movie_obj.setSeason(season_name = title, season_sites = [])

                    for cur_str in htmlblocks:
                        video_title = str_between(cur_str, "<h4>", "</h4>").strip()
                        if video_title == "": continue
                        video_link = str_between(cur_str, "<a href=\"", "\">").strip()

                        main_logger.debug("\nVideo Title: %s\nVideo Link:%s" % (video_title, video_link))

                        cur_video_dict = movie_obj.setVideo(video_name = video_title, video_mirrors = [])
                        cur_mirror_dict = movie_obj.setVideoMirror( video_mirror_name = site_name,  video_mirror_url = video_link)
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
        except Exception as e: 
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            main_logger.debug("%s, %s, %s, %s" % (exc_type, fname, exc_tb.tb_lineno, str(e)))

        json_str = movie_obj.dictToJson()

        create_folder(cur_directory)

        if debug_mode:
            filePtr = open(cur_directory + '/' + cur_site_name + '_debug.json', 'w')
            filePtr.write(json_str)
            filePtr.close()
        else:
            filePtr = open(cur_directory + '/' + cur_site_name + '.json', 'w')
            filePtr.write(json_str)
            filePtr.close()