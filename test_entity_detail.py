import requests
import urllib.parse

# 测试实体详情API
novel_id = "e6519d1d-250b-4d44-a3aa-1db25ddb6f5c"
entity_name = "黄蓉"
encoded_name = urllib.parse.quote(entity_name)

url = f"http://localhost:8000/api/novels/{novel_id}/entities/{encoded_name}"

try:
    response = requests.get(url)
    print(f"状态码: {response.status_code}")
    print(f"响应内容: {response.text[:500]}")
except Exception as e:
    print(f"请求失败: {e}")