#-*- coding: UTF-8 -*-
import os
import json

class movie_detail_class:
    def __init__(self):
        self.movie_obj = {}

    def get_movie_obj(self):
        return self.movie_obj

    def setMovie(self, movie_videos, uuid = "", page_link = "", img = "", movie_info = "", view_number = "",
        movie_type = "", movie_date = "", movie_total_number = "", cur_movie_play = "", last_updated = "1911-01-01"):
        if uuid not in self.movie_obj:
            self.movie_obj[uuid] =  {
                 'uuid': uuid
                ,'page_link' : page_link
                ,'movie_date' : movie_date
                ,'movie_videos' : movie_videos
                ,'last_updated': last_updated
            }
            return self.movie_obj[uuid]
        else:
            return {}

    def setSeason(self, season_sites, season_name = ""):
        cur_dict = {
             'season_name' : season_name
            ,'season_sites' : season_sites
        }
        return cur_dict

    def setVideo(self, video_mirrors, video_name = ""):
        cur_dict = {
             'video_name' : video_name
            ,'video_mirrors' : video_mirrors
        }
        return cur_dict

    def setVideoMirror(self, video_mirror_name = "", video_mirror_url = ""):
        cur_dict = {
             'video_mirror_name' : video_mirror_name
            ,'video_mirror_url' : video_mirror_url
        }
        return cur_dict

    def dictToJson(self, input_dict):
        return json.dumps(input_dict)