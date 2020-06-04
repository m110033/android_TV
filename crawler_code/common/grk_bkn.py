import math

def GetGTK(sKey):
    hash = 5381
    i = 0
    l = len(sKey)
    while(i < l):
        hash += (hash << 5) + ord(sKey[i])
        i += 1
    return (hash & 0x7fffffff)

def GetBkn(pt):
    k = 0
    i = 0
    l = len(pt)
    while(i < l):
        k += int(pt[0] + pt[1] + pt[2] + pt[3], 16)
        k %= 0x4000
        i += 4
    print(k)
    gtk = 0
    i = 0
    l = len(sKey)
    while(i < l):
        gtk += ord(sKey[i])
        gtk %= k;
        i += 1
    return gtk

# 45 - 45 * p = n
# 45 - n - n * p = n1
# 45 - n - n1 - n1 * p = n2
# 45 - n - n1 - n2 - n2 * p = n3

def recv(o, b, p, t):
    if t == 0:
        return 0
    elif t == 1:
        return o - o * b
    else:
        o = o - o * b
        return o * math.pow((1 - p), (t - 1))

sKey = "e9a20b83ef2916723b538597c83465a69148e3ad02f4925f"
# print(GetGTK("1682457036"))
# print(GetBkn("f31c6ea68665576ac14ef0c5885e27b94ca76c02bc1c1867204619d12ad9457a29cceaef13f615739088940cf47b4c9d25da4bc9c5cdc372"))
s = 60
b = 0.5
p = 0.15

a = recv(s, 0.5, 0.15, 6)
r = recv(a, 0.15, 0.15, 2)
print(r)