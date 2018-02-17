#!/usr/bin/env python
# -*- coding: utf8 -*-

import json
import sys
import urllib, urllib2
from datetime import datetime, timedelta

from common.common import (create_folder, str_between, cleanhtml, STORE_SITE, DEBUG_SITE)
from movieClass import movie_class
from common.request import request_func

today_date = datetime.now().strftime("%Y-%m-%d")

def crawl_maplestage(debug_mode, main_logger):
    site_list = [
        #Variety
        ('variety', 'tw'),
        ('variety', 'cn'),
        ('variety', 'kr'),
        #Drama
        ('drama', 'tw'),
        ('drama', 'cn'),
        ('drama', 'kr'),
        ('drama', 'ot')
    ]

    # Reset the dict
    if debug_mode:
        cur_directory = DEBUG_SITE
    else:
        cur_directory = STORE_SITE

    parser_index = 0
    total_parser_index = str(len(site_list))
    for cur_site in site_list:
        parser_index = parser_index + 1
        page_index_str = str(parser_index)

        site_type = cur_site[0]
        site_name = cur_site[1]

        query_header = '{"queries":[{"name":"shows","query":{"sort":"top","take":99999,'
        query_footer = '}}]}';
        query_data = query_header + '"type":"' + site_type + '","region":"' + site_name + '"' + query_footer
        movie_obj = movie_class()
        
        try:
            '''
            POST:
                http://maplestage.com/v1/query
            HEADERS:
                User-Agent: Mozilla/5.0
                Content-Type: application/json
            PARAMS:
                {"queries":[{"name":"shows","query":{"sort":"top","take":99999,"type":"variety","region":"tw"}}]}
            '''
            req = urllib2.Request('http://maplestage.com/v1/query', query_data, {'User-Agent': 'Mozilla/5.0', 'Content-Type': 'application/json'})
            response = urllib2.urlopen(req)
            html = response.read()

            json_obj = json.loads(html)

            if len(json_obj["shows"]) == 0:
                break

            cur_movie_index = 0
            total_movide_index = str(len(json_obj["shows"]))

            for cur_movie_obj in json_obj["shows"]:
                cur_movie_index = cur_movie_index + 1
                cur_movie_index_str = str(cur_movie_index)

                slug = cur_movie_obj["slug"]
                video_uuid = slug
                title = cur_movie_obj["name"]
                img = cur_movie_obj["cover"]
                year_info = cur_movie_obj["year"]
                last_updated = cur_movie_obj["updatedAt"]
                date_str = "1911-01-01"
                if last_updated is not None:
                    date_obj = datetime.strptime(last_updated, '%Y-%m-%dT%H:%M:%S.%fZ')
                    date_str = datetime.strftime(date_obj, '%Y-%m-%d')
                    page_link = 'http://maplestage.com/show/' + urllib.quote(title.encode('utf-8'))
                
                main_logger.debug("Parse the sites: " + page_index_str + "/" + total_parser_index + " => " + cur_movie_index_str + "/" + total_movide_index + ", title " + title)

                # cal timestamp
                dt = datetime.strptime(date_str, '%Y-%m-%d')
                cur_movie_play = str(int((dt - datetime(1970, 1, 1)).total_seconds()))

                movie_obj.addMovie(
                    movie_title = title,
                    movie_image = img,
                    movie_page_link = page_link,
                    movie_second_title = date_str
                )
                continue

                '''
                if movie_obj.needUpdateMovie(video_uuid, cur_movie_play):
                    pass
                else:
                    if debug_mode:
                        print ("No need to update ('uuid', 'last_update', 'page_link') <=> (%s, %s, %s)" % (video_uuid, last_updated, page_link))
                    continue
                '''  

                # videoHtml = request_func(page_link)
                # videoHtml = str_between(videoHtml, 'var pageData =', ';').strip()
                # temp_video_obj = json.loads(videoHtml)
                # for cur_video_obj in temp_video_obj["props"]:
                    # if cur_video_obj["name"] == "show":
                        # video_obj = cur_video_obj
                        # break
                # movie_info = video_obj["value"]["info"]
                # video_type = video_obj["value"]["type"]
                # view_number = video_obj["value"]["totalViews"]
                # created_date = video_obj["value"]["createdAt"]
                # date_obj = datetime.strptime(created_date, '%Y-%m-%dT%H:%M:%S.%fZ')
                # created_date_str = datetime.strftime(date_obj, '%Y-%m-%d')

                cur_movie_dict = movie_obj.setMovie(
                    uuid = video_uuid,
                    title = title,
                    page_link = page_link,
                    img = img,
                    movie_info = '',
                    movie_date = date_str,
                    movie_type = site_type,
                    last_updated = date_str,
                    cur_movie_play = cur_movie_play,
                    movie_videos = [])

                cur_season_dict = movie_obj.setSeason(season_name = title, season_sites = [])

                query_header = '{"queries":[{"name":"episodes","query":{"sort":"top","take":100,'
                query_footer = '}}]}';
                query_data = query_header + '"type":"' + site_type + '","slug":"' + slug.encode('utf-8') + '"' + query_footer

                main_logger.debug("URL:%s\nQuery Data:%s" % ('http://maplestage.com/v1/query', query_data))

                req = urllib2.Request('http://maplestage.com/v1/query', query_data, {'User-Agent': 'Mozilla/5.0', 'Content-Type': 'application/json'})
                response = urllib2.urlopen(req)
                videoHtml = response.read()
                video_json_obj = json.loads(videoHtml)

                if len(video_json_obj["episodes"]) > 0:
                    for cur_video_data in video_json_obj["episodes"]:
                        video_link = "http://maplestage.com/" + cur_video_data["href"]
                        video_title = cur_video_data["title"].strip()

                        cur_video_dict = movie_obj.setVideo(video_name = video_title, video_mirrors = [])
                        cur_mirror_dict = movie_obj.setVideoMirror( video_mirror_name = "maplestage",  video_mirror_url = video_link)
                        cur_video_dict["video_mirrors"].append(cur_mirror_dict)

                        try:
                            if len(cur_video_dict["video_mirrors"]) > 0:
                                cur_season_dict["season_sites"].append(cur_video_dict)
                        except:
                            main_logger.debug("No Season")
                try:
                    if len(cur_season_dict["season_sites"]) > 0:
                        cur_movie_dict["movie_videos"].append(cur_season_dict)
                except:
                    main_logger.debug("No Video")

                try:
                    if len(cur_movie_dict["movie_videos"]) > 0:
                        movie_obj.addMovie(cur_movie_dict)
                except:
                    main_logger.debug("Add movie into obj failed")
                #break

            json_str = movie_obj.dictToJson()
            if debug_mode:
                cur_directory = DEBUG_SITE
            else:
                cur_directory = STORE_SITE

            top_dir = cur_directory + '/maplestage'
            create_folder(top_dir)

            filePtr = open(top_dir + '/' + site_type + '_' + site_name + '.json', 'w')
            filePtr.write(json_str)
            filePtr.close()
            #break
        except Exception as e: 
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            main_logger.debug("%s, %s, %s, %s" % (exc_type, fname, exc_tb.tb_lineno, str(e)))
