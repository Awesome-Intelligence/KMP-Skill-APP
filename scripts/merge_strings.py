"""把 kit 的 strings.xml 合并进目标项目 —— 只补缺失的 key，不覆盖已有的。

用法：python merge_strings.py <kit strings.xml> <目标 strings.xml>
目标文件不存在就整份复制过去。
"""
import os, re, sys

def r(p):
    with open(p, encoding="utf-8") as f: return f.read()
def w(p, t):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8", newline="\n") as f: f.write(t)

if len(sys.argv) != 3:
    print(__doc__); sys.exit(1)

src, dst = sys.argv[1], sys.argv[2]
if not os.path.exists(src):
    print("跳过：%s 不存在" % src); sys.exit(0)
if not os.path.exists(dst):
    w(dst, r(src)); print("新建 %s" % dst); sys.exit(0)

have = set(re.findall(r"<string\s+name=\"([^\"]+)\"", r(dst)))
missing = []
for m in re.finditer(r"<string\s+name=\"([^\"]+)\"([^>]*)>(.*?)</string>", r(src), re.S):
    if m.group(1) not in have:
        missing.append("    <string name=\"%s\"%s>%s</string>" % (m.group(1), m.group(2), m.group(3)))
if not missing:
    print("%s：无需补全" % dst); sys.exit(0)
txt = r(dst)
idx = txt.rfind("</resources>")
txt = txt[:idx] + "\n" + "\n".join(missing) + "\n" + txt[idx:]
w(dst, txt)
print("%s：补了 %d 条" % (dst, len(missing)))
