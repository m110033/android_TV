#-*- coding: UTF-8 -*-
import os
import json
import collections
import uuid

class movie_class:
    def __init__(self):
        self.movie_obj = collections.OrderedDict({ 'movies': [] })
        pass

    def addMovie(self, movie_title, movie_image, movie_page_link, movie_second_title):
        new_arr = [x for x in self.movie_obj["movies"] if x.get('m_t') != movie_title]
        new_arr.insert(0, {
            'm_idx': str(uuid.uuid4()),
            'm_t': movie_title,
            'm_img': movie_image,
            'm_p_l': movie_page_link,
            'm_s_t': movie_second_title
        })
        self.movie_obj["movies"] = new_arr

    def dictToJson(self):
        return json.dumps(self.movie_obj)
