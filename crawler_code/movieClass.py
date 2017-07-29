#-*- coding: UTF-8 -*-
import os
import json

class movie_class:
    def __init__(self):
        self.movie_obj = { 'movies': [] }
        pass

    def addMovie(self, movie_title, movie_image, movie_page_link, movie_second_title):
        cur_movie_arr = self.movie_obj["movies"]
        cur_movie_arr.append({
            'm_idx': len(cur_movie_arr),
            'm_t': movie_title,
            'm_img': movie_image,
            'm_p_l': movie_page_link,
            'm_s_t': movie_second_title
        })

    def dictToJson(self):
        return json.dumps(self.movie_obj)
