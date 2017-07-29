def crawl_ikale():
    # Reset the dict
    if debug_mode:
        cur_directory = debug_site
    else:
        cur_directory = store_site

    if debug_mode:
        movie_obj.reset(cur_directory + '/ikale_debug.json')
    else:
        movie_obj.reset(cur_directory + '/ikale.json')

    parser_index = 0
    while True:
        try:
            page_index_str = str(parser_index + 1)
            main_logger.debug("Parse the movies: " + page_index_str + "/ NO")

            # Get Movie List
            url = 'http://movie.i-kale.net/sql.php?tag=getMovie'
            html = request_func(url, "page=" + page_index_str + "&type=all&sort=time")
            json_obj = json.loads(html)

            if len(json_obj["json"]) == 0:
                break

            for cur_movie_obj in json_obj["json"]:
                video_uuid = cur_movie_obj["id"]
                title = cur_movie_obj["name"]
                img = cur_movie_obj["image"]
                video_type = cur_movie_obj["type"]
                view_number = cur_movie_obj["click"]
                year_info = cur_movie_obj["time"]
                date_obj = datetime.strptime(year_info, '%Y-%m-%d %H:%M:%S')
                date_str = datetime.strftime(date_obj, '%Y-%m-%d')
                page_link = 'http://movie.i-kale.net/sql.php?tag=getOneMovie'

                cur_movie_dict = movie_obj.setMovie(
                    uuid = video_uuid,
                    title = title,
                    page_link = page_link,
                    img = img,
                    movie_info = "",
                    view_number = view_number,
                    movie_date = date_str,
                    movie_type = video_type,
                    last_updated = today_date,
                    movie_videos = [])

                videHtml = request_func(page_link, "id=" + video_uuid)
                video_obj = json.loads(videHtml)

                cur_season_dict = movie_obj.setSeason(season_name = title, season_sites = [])

                videHtml = video_obj[0]["url"]
                video_block = videHtml.split('src=')
                video_block.pop(0)

                if len(video_block) > 0:
                    for cur_video_data in video_block:
                        video_link = str_between(cur_video_data, "\"", "\"").strip()
                        video_title = title

                        if video_link.find("openload") == -1:
                            continue

                        cur_video_dict = movie_obj.setVideo(video_name = video_title, video_mirrors = [])
                        cur_mirror_dict = movie_obj.setVideoMirror( video_mirror_name = "ikale",  video_mirror_url = video_link)
                        cur_video_dict["video_mirrors"].append(cur_mirror_dict)

                        try:
                            if len(cur_video_dict["video_mirrors"]) > 0:
                                cur_season_dict["season_sites"].append(cur_video_dict)
                        except NameError:
                            if debug_mode:
                                print "No Videos"

                try:
                    if len(cur_season_dict["season_sites"]) > 0:
                        cur_movie_dict["movie_videos"].append(cur_season_dict)
                except NameError:
                    if debug_mode:
                        print "No Season"

                try:
                    if len(cur_movie_dict["movie_videos"]) > 0:
                        movie_obj.addMovie(cur_movie_dict)
                except NameError:
                    if debug_mode:
                        print "No Season"

                #break
            parser_index = parser_index + 1
            #break
        except Exception, e:
            print "EXCEPT:" + str(e)
            pass

    json_str = movie_obj.dictToJson()
    if debug_mode:
        cur_directory = debug_site
    else:
        cur_directory = store_site
    if debug_mode:
        filePtr = open(cur_directory + '/ikale_debug.json', 'w')
        filePtr.write(json_str)
        filePtr.close()
    else:
        filePtr = open(cur_directory + '/ikale.json', 'w')
        filePtr.write(json_str)
        filePtr.close()
