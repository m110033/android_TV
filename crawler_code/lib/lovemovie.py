def crawl_lovemovie():
    # Reset the dict
    if debug_mode:
        cur_directory = debug_site
    else:
        cur_directory = store_site

    if debug_mode:
        movie_obj.reset(cur_directory + '/lovemovie_debug.json')
    else:
        movie_obj.reset(cur_directory + '/lovemovie.json')


    # Login
    url = 'http://www.lm-us.com/'
    request_func(url, "log=us01&pwd=0000&rememberme=forever&redirect_to=%2F&a=login&slog=true&Submit=log+in")

    video_url = 'http://www.lm-us.com/'

    # Get source
    html = request_func(video_url)

    htmlCode = str_between(html, "tg-footer-other-widgets", "footer-socket-wrapper").strip()
    htmlCode = htmlCode.split("href")
    htmlCode.pop(0)
    parser_index = 0

    total_len = len(htmlCode)
    if total_len > 0:
        while parser_index < total_len:
            try:
                data = htmlCode[parser_index]
                page_index_str = str(parser_index + 1)
                main_logger.debug("Parse the movies: " + page_index_str + "/" + str(total_len))

                page_link = str_between(data, "=\"", "\">")
                title = str_between(data, ">", "</a>").strip()
                title = cleanhtml(title)

                html = request_func(page_link)

                video_html = str_between(html, "<article", "</article>")
                video_uuid = str_between(video_html, "id=\"post-", "\"").strip()
                intro = str_between(video_html, "<p>", "</article>").strip()
                intro = cleanhtml(intro)
                img = str_between(video_html, "src=\"", "\"").strip()
                video_type = "Movie Series"
                view_number = "0"


                cur_movie_dict = movie_obj.setMovie(
                    uuid = video_uuid,
                    title = title,
                    page_link = page_link,
                    img = img,
                    movie_info = intro,
                    view_number = view_number,
                    movie_type = video_type,
                    last_updated = today_date,
                    movie_videos = [])

                cur_season_dict = movie_obj.setSeason(season_name = title, season_sites = [])

                video_data = str_between(html, "related_posts_by_taxonomy", "</ul>").strip()
                video_block = video_data.split('<a href=')
                video_block.pop(0)

                if len(video_block) > 0:
                    for cur_video_data in video_block:
                        video_link = str_between(cur_video_data, "\"", "\"").strip()
                        video_title = str_between(cur_video_data, "\">", "</a>").strip()

                        if video_title == "":
                            continue

                        cur_video_dict = movie_obj.setVideo(video_name = video_title, video_mirrors = [])
                        cur_mirror_dict = movie_obj.setVideoMirror( video_mirror_name = "lovemovie",  video_mirror_url = video_link)
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
        filePtr = open(cur_directory + '/lovemovie_debug.json', 'w')
        filePtr.write(json_str)
        filePtr.close()
    else:
        filePtr = open(cur_directory + '/lovemovie.json', 'w')
        filePtr.write(json_str)
        filePtr.close()