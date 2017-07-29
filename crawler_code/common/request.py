import urllib, urllib2, cookielib

def create_opener():
    cj = cookielib.CookieJar()
    opener = urllib2.build_opener(urllib2.HTTPCookieProcessor(cj))
    opener.addheaders = [('User-Agent', 'Mozilla/5.0')]
    return opener

def request_func(url, cur_data = "", header_arr = ""):
    opener = create_opener()

    if len(header_arr) > 0:
        for cur_header in header_arr:
            opener.addheaders.append(cur_header)
    try:
        if cur_data != "":
            response = opener.open(url, data = cur_data)
        else:
            response = opener.open(url)

        html = response.read()
    except urllib2.HTTPError, error:
        html = error.read()

    return html