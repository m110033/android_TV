import http.cookiejar, urllib.request 
from urllib.error import URLError, HTTPError

def create_opener():
    cj = http.cookiejar.CookieJar() 
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    opener.addheaders = [
        ('User-Agent', 'Mozilla/5.0 Gecko/20100101 Firefox/59.0'),
        # ("Accept-Encoding", "gzip, deflate, br"),
        # ("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.5,en;q=0.3"),
        # ("Upgrade-Insecure-Requests", "1")
    ]
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

        html = response.read().decode("utf8")
    except HTTPError as e:
        # do something
        print('Error code: ', e.code)
    except URLError as e:
        # do something
        print('Reason: ', e.reason)

    return html