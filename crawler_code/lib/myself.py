#!/usr/bin/env python
# -*- coding: utf8 -*-

import json
import sys
import os
import re
from datetime import datetime, timedelta

from common.common import (create_folder, str_between, cleanhtml, STORE_SITE, DEBUG_SITE)
from movieClass import movie_class
from movieDetailClass import movie_detail_class
from common.request import request_func

today_date = datetime.now().strftime("%Y-%m-%d")

comic_type = ['online_comic', 'end_comic']
comic_prefix_link = ['http://myself-bbs.com/forum-133-', 'http://myself-bbs.com/forum-113-']

def crawl_myself(debug_mode, main_logger):
    for index, temp_item in enumerate(comic_type):
        # Reset the dict
        if debug_mode:
            cur_directory = DEBUG_SITE
        else:
            cur_directory = STORE_SITE
        
        cur_comic_type = comic_type[index]
        cur_comic_prefix_link = comic_prefix_link[index]

        video_url = cur_comic_prefix_link + '1.html'

        movie_obj = movie_class()
        movie_detail_obj = movie_detail_class()

        # Get source
        html = request_func(video_url)

        page_num_str = str_between(html, "<span title=\"", "\"").strip()
        page_num_str = str_between(page_num_str, " ", " ").strip()
        page_num = int(page_num_str)

        # uuid_list = []
        for page_index in range(1, page_num + 1):
            #if cur_comic_type == "end_comic" and page_index >= 0:
                #太久遠了，更新可能性很低，而且更新會放在最前面的幾頁
                #break;
            page_index_str = str(page_index)
            try:
                main_logger.debug("Parse the pages: " + page_index_str + "/" + page_num_str)

                url = cur_comic_prefix_link + page_index_str + ".html"
                html = request_func(url)

                htmlCode = str_between(html, "ml mlt mtw cl", "</ul>")
                htmlCode = htmlCode.split("<div class=\"c cl\">")

                if len(htmlCode) > 0:
                    for date in htmlCode:
                        mobj = re.search('<a href="(.+)" onclick="atarget\(this\)" title="(.+)">', date)
                        if not mobj:
                            continue
                        linkName = mobj.group(1).strip()
                        title = mobj.group(2).strip()
                        if linkName == "" or title == "":
                            continue
                        video_uuid = str_between(linkName, "thread-", "-1").strip()
                        realLink = "http://myself-bbs.com/" + linkName
                        mobj = re.search("<img src=\"(.+)\" alt", date)
                        img = "http://myself-bbs.com/%s" % (mobj.group(1) if mobj else "")
                        # view_number = str_between(date, "<em title=", "/em>");
                        # view_number = str_between(view_number, "\">", "<").strip();
                        mobj = re.search("<em class=\"xs0\">(.+)</em>", date)
                        if mobj:
                            tmp_data = mobj.group(1)
                            tmpobj = re.search("<span title=\"(.+)\"", tmp_data)
                            tmpdate = tmpobj.group(1) if tmpobj else tmp_data
                            date_arr = tmpdate.split('-')
                            create_date = "%s-%02d-%02d" % (date_arr[0], int(date_arr[1]), int(date_arr[2]))
                        else:
                            create_date = today_date
                        # ep_info = str_between(date, "ep_info\">", "</p>").strip();
                        # cur_play_number = int(str_between(ep_info, " ", " ").strip())
                        # print({
                        #     "title": title,
                        #     "img": img,
                        #     "realLink": realLink,
                        #     "create_date": create_date
                        # })
                        movie_obj.addMovie(
                            movie_title = title,
                            movie_image = img,
                            movie_page_link = realLink,
                            movie_second_title = create_date
                        )
                        continue
                        # if video_uuid not in uuid_list: uuid_list.append(video_uuid)

                        response = opener.open(realLink)
                        video_html = response.read()

                        htmlCode = str_between(video_html, "<div class=\"info_info\">", "</div>")
                        htmlblocks = htmlCode.split('</span>')

                        movie_total_number = video_play_date = video_type = ""
                        if len(htmlblocks) > 1:
                            video_type = str_between(htmlblocks[1], ":", "</li>").strip()
                            video_type = cleanhtml(video_type)
                            video_play_date = str_between(htmlblocks[2], ":", "</li>").strip()
                            date_str = video_play_date[:4] + "-01-01"
                            movie_total_number = str_between(htmlblocks[3], ":", "</li>").strip()
                            video_author = str_between(htmlblocks[4], ":", "</li>").strip()
                        else:
                            date_str = "1911-01-01"
                            video_author = ""

                        intro = str_between(video_html, "<div id=\"info_introduction_text\" style=\"display:none;\">", "</div>")
                        intro = cleanhtml(intro).strip()

                        cur_movie_dict = movie_detail_obj.setMovie(
                            uuid = video_uuid,
                            page_link = realLink,
                            movie_date = date_str,
                            last_updated = today_date,
                            movie_videos = [])

                        htmlCode = str_between(video_html, "<ul class=\"main_list\"><li>", "</div>")
                        htmlblocks = htmlCode.split("<a href=\"javascript:;\"")

                        cur_season_dict = movie_detail_obj.setSeason(season_name = title, season_sites = [])

                        for data in htmlblocks:
                            cur_str = data
                            video_title = str_between(cur_str, ">", "</a>").strip()
                            link_arr = cur_str.split("data-href=")

                            if video_title == "":
                                continue

                            cur_video_dict = movie_detail_obj.setVideo(video_name = video_title, video_mirrors = [])

                            for link in link_arr:
                                if link.find("various fancybox.iframe google") >= 0:
                                    video_link_title = str_between(link, "\">", "</a>").strip()
                                    video_link = str_between(link, "\"", "\" target=\"")
                                    
                                    cur_mirror_dict = movie_detail_obj.setVideoMirror(
                                        video_mirror_name = video_link_title,
                                        video_mirror_url = video_link)

                                    try:
                                        cur_video_dict["video_mirrors"].append(cur_mirror_dict)
                                    except:
                                        main_logger.debug("No Mirror")
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
                        # break
                # break
            except Exception as e: 
                exc_type, exc_obj, exc_tb = sys.exc_info()
                fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
                main_logger.debug("%s, %s, %s, %s" % (exc_type, fname, exc_tb.tb_lineno, str(e)))
                raise

        top_dir = cur_directory / 'myself'
        detail_dir = top_dir / "detail"

        create_folder(cur_directory)
        create_folder(top_dir)

        filePtr = open("%s/%s.json" % (top_dir, cur_comic_type), 'w')
        json_str = movie_obj.dictToJson()
        filePtr.write(json_str)
        filePtr.close()

        total_movie_obj = movie_detail_obj.get_movie_obj()
        if total_movie_obj:
            create_folder(detail_dir)
            for uuid, movie_json in total_movie_obj.iteritems():
                filePtr = open("%s/%s.json" % (detail_dir, uuid), 'w')
                json_str = movie_detail_obj.dictToJson(movie_json)
                filePtr.write(json_str)
                filePtr.close()
