#!/usr/bin/env python3
"""
Dream Share 自动化测试脚本
覆盖：注册、登录、梦境CRUD、分类筛选、评论、点赞、关注、讨论组、通知、搜索、设置、上传、管理后台
"""

import json
import hashlib
import time
import sys
import os
import subprocess
import urllib.parse
import requests

BASE = "http://10.245.181.124:8080"

# 全局状态
TEST_NUM = 0
PASSED = 0
FAILED = 0
ERRORS = []
USERS = {}  # 存储测试用户 token
DREAM_ID = None
DISC_ID = None
COMMENT_ID = None

def md5(s):
    return hashlib.md5(s.encode()).hexdigest()

def count():
    global TEST_NUM
    TEST_NUM += 1
    return TEST_NUM

def ok(name, detail=""):
    global PASSED
    PASSED += 1
    msg = f"  [PASS] #{TEST_NUM} {name}"
    if detail:
        msg += f" ({detail})"
    print(msg)

def fail(name, got, expected=None):
    global FAILED
    FAILED += 1
    msg = f"  [FAIL] #{TEST_NUM} {name}"
    if expected is not None:
        msg += f"  got={str(got)[:200]}  expected={expected}"
    else:
        msg += f"  got={str(got)[:300]}"
    ERRORS.append(msg)
    print(msg)

def curl(method, path, data=None, user=None, body_file=None):
    """用 curl 调用 API"""
    token = ""
    if user and user in USERS:
        token = USERS[user].get("token", "")

    url = BASE + path
    cmd = ["curl", "-s", "-X", method, url]

    if token:
        cmd.extend(["-H", f"Authorization: Bearer {token}"])

    # GET 请求的 data 作为 query params（需 URL encode）
    if method == "GET" and data:
        query = "&".join(f"{k}={urllib.parse.quote(str(v))}" for k, v in data.items())
        sep = "&" if "?" in url else "?"
        url += sep + query

    if method != "GET" and data and not body_file:
        json_str = json.dumps(data, ensure_ascii=False)
        cmd.extend(["-H", "Content-Type: application/json", "-d", json_str])

    if body_file:
        cmd.extend(["-F", f"file=@{body_file}"])

    try:
        r = subprocess.run(cmd, capture_output=True, text=True, timeout=15)
        if r.stdout.strip():
            return json.loads(r.stdout), r.returncode
        # 空响应 — 返回原始 stdout 和 code
        return {"code": r.returncode, "message": f"empty response ({r.stdout!r})"}, r.returncode
    except json.JSONDecodeError:
        return {"code": r.returncode, "message": f"non-JSON: {r.stdout[:200]}", "stderr": r.stderr[:200]}, r.returncode
    except Exception as e:
        return {"error": str(e)}, 0

def approve_dream(dream_id, admin_token=None):
    """审核通过指定梦境，使其出现在Feed中"""
    if admin_token is None:
        admin_token = USERS.get("admin", {}).get("token", "")
    if not admin_token:
        return False
    url = f"{BASE}/api/admin/audits"
    # 找到该梦境的审核记录ID
    r = subprocess.run(
        ["curl", "-s", "-X", "GET", f"{url}?page=1&size=50&targetType=DREAM",
         "-H", f"Authorization: Bearer {admin_token}"],
        capture_output=True, text=True, timeout=5
    )
    d = json.loads(r.stdout) if r.stdout.strip() else {}
    records = d.get("data", {}).get("records", [])
    for rec in records:
        if rec.get("targetId") == dream_id:
            # 通过审核
            r2 = subprocess.run(
                ["curl", "-s", "-X", "POST", f"{url}/{rec['id']}/approve",
                 "-H", f"Authorization: Bearer {admin_token}"],
                capture_output=True, text=True, timeout=5
            )
            return r2.returncode == 0
    return False

def run_cleanup():
    """清理测试数据"""
    print("\n========== 清理测试数据 ==========")

    # 删除测试讨论组
    if DISC_ID:
        n = count()
        d, code = curl("DELETE", f"/api/discussions/{DISC_ID}", user="ycs")
        if d.get("code") == 200:
            ok(f"清理: 删除测试讨论组 {DISC_ID}")
        else:
            fail(f"清理: 删除讨论组", d)

    # 删除测试用户 (通过 settings/delete-account)
    for uname in ["testuser1", "testuser2"]:
        if uname in USERS:
            n = count()
            d, code = curl("POST", "/api/settings/delete-account", data={"password": "123456"}, user=uname)
            if d.get("code") == 200:
                ok(f"清理: 删除测试用户 {uname}")
            else:
                fail(f"清理: 删除用户 {uname}", d)


# ============ 1. 注册测试 ============
def test_register():
    print("\n========== 1. 注册测试 ==========")

    ts = int(time.time())

    # 1.1 正常注册
    n = count()
    uname1 = f"testuser_{ts}"
    d, code = curl("POST", "/api/auth/register", data={
        "username": uname1,
        "nickname": "测试用户A",
        "password": "123456"
    })
    if d.get("code") == 200 and d.get("data", {}).get("token"):
        USERS["testuser1"] = {"token": d["data"]["token"], "userId": d["data"]["userId"], "password": "123456", "username": uname1}
        ok("正常注册")
    else:
        fail("正常注册", d)

    # 1.2 注册填手机号 (随机 phone 避免冲突)
    n = count()
    phone = f"139{ts % 10000000:07d}"
    d, code = curl("POST", "/api/auth/register", data={
        "username": f"testuser2_{ts}",
        "nickname": "测试用户B",
        "password": "123456",
        "phone": phone
    })
    if d.get("code") == 200 and d.get("data", {}).get("token"):
        USERS["testuser2"] = {"token": d["data"]["token"], "userId": d["data"]["userId"], "password": "123456", "phone": phone}
        ok("注册带手机号")
    else:
        fail("注册带手机号", d)

    # 1.3 密码太短
    n = count()
    d, code = curl("POST", "/api/auth/register", data={
        "username": f"short_{ts}",
        "nickname": "短密码",
        "password": "123"
    })
    if d.get("code") != 200:
        ok("密码太短被拒绝")
    else:
        fail("密码太短被拒绝", d)


# ============ 2. 登录测试 ============
def test_login():
    print("\n========== 2. 登录测试 ==========")

    # 2.1 ycs 登录 (原始密码 ycs584520)
    n = count()
    d, code = curl("POST", "/api/auth/login", data={"username": "ycs", "password": "ycs584520"})
    if d.get("code") == 200 and d.get("data", {}).get("token"):
        USERS["ycs"] = {"token": d["data"]["token"], "userId": d["data"]["userId"], "role": d["data"]["role"]}
        ok("用户名登录 (ycs)")
    else:
        fail("用户名登录 (ycs)", d)

    # 2.2 admin 登录
    n = count()
    d, code = curl("POST", "/api/auth/login", data={"username": "admin", "password": "admin123"})
    if d.get("code") == 200 and d.get("data", {}).get("token"):
        USERS["admin"] = {"token": d["data"]["token"], "userId": d["data"]["userId"], "role": d["data"]["role"]}
        ok("admin 登录")
        if d["data"]["role"] == 1:
            ok("admin role=1 正确")
        else:
            fail("admin role", d["data"]["role"], 1)
    else:
        fail("admin 登录", d)

    # 2.3 密码错误
    n = count()
    d, code = curl("POST", "/api/auth/login", data={"username": "ycs", "password": "wrongpass"})
    if d.get("code") != 200:
        ok("密码错误被拒绝")
    else:
        fail("密码错误被拒绝", d)

    # 2.4 不存在的用户
    n = count()
    d, code = curl("POST", "/api/auth/login", data={"username": "not_exist_user_xyz", "password": "123456"})
    if d.get("code") != 200:
        ok("不存在的用户被拒绝")
    else:
        fail("不存在的用户被拒绝", d)

    # 2.5 手机号登录 (用注册的用户)
    if "testuser2" in USERS:
        phone = USERS["testuser2"].get("phone", "")
        if phone:
            n = count()
            d, code = curl("POST", "/api/auth/login", data={"username": phone, "password": "123456"})
            if d.get("code") == 200:
                ok("手机号登录")
            else:
                fail("手机号登录", d)


# ============ 3. 分类测试 ============
def test_categories():
    print("\n========== 3. 分类测试 ==========")

    n = count()
    d, code = curl("GET", "/api/categories")
    if d.get("code") == 200 and len(d.get("data", [])) > 0:
        ok(f"分类列表", len(d["data"]))
        USERS["categories"] = d["data"]
    else:
        fail("分类列表", d)


# ============ 4. 梦境 CRUD 测试 ============
def test_dreams():
    global DREAM_ID
    print("\n========== 4. 梦境 CRUD 测试 ==========")

    ts = int(time.time())
    cats = USERS.get("categories", [])
    cat_id = cats[0]["id"] if cats else 1

    # 4.1 创建梦境
    n = count()
    d, code = curl("POST", "/api/dreams", data={
        "categoryId": cat_id,
        "description": f"自动化测试梦境 #{ts}",
        "clarity": 3,
        "dreamDate": "2026-05-14",
        "location": "测试地点",
        "keywords": "测试,自动",
        "isRecurring": 0,
        "tags": "test,auto"
    }, user="ycs")
    if d.get("code") == 200 and d.get("data", {}).get("id"):
        DREAM_ID = d["data"]["id"]
        ok(f"创建梦境", f"id={DREAM_ID}")
        # 审核通过，使梦境出现在Feed中
        if approve_dream(DREAM_ID):
            ok("梦境审核通过")
        else:
            fail("梦境审核通过", "未找到审核记录")
    else:
        fail("创建梦境", d)
        return

    # 4.2 获取梦境详情 (公开接口)
    n = count()
    d, code = curl("GET", f"/api/dreams/{DREAM_ID}")
    if d.get("code") == 200 and d.get("data", {}).get("id") == DREAM_ID:
        detail = d["data"]
        ok("获取梦境详情")
        # 检查关键字段
        checks = {
            "userId": "用户ID",
            "category": "分类名",
            "description": "描述",
            "clarity": "清晰度",
            "nickname": "昵称"
        }
        for key, name in checks.items():
            if key in detail:
                ok(f"详情含{name}", str(detail[key])[:40])
            else:
                fail(f"详情缺{name}", detail)
    else:
        fail("获取梦境详情", d)

    # 4.3 Feed 列表
    n = count()
    d, code = curl("GET", "/api/dreams/feed?page=1&pageSize=10")
    if d.get("code") == 200:
        records = d.get("data", {}).get("records", [])
        ok(f"Feed 列表", len(records))
    else:
        fail("Feed 列表", d)

    # 4.4 按分类筛选
    for cat in cats[:3]:
        n = count()
        d, code = curl("GET", f"/api/dreams/feed?page=1&pageSize=10&categoryId={cat['id']}")
        if d.get("code") == 200:
            records = d.get("data", {}).get("records", [])
            all_match = all(r.get("categoryId") == cat["id"] for r in records)
            if all_match:
                ok(f"分类筛选 ({cat['code']})", f"{len(records)}条,全匹配")
            elif len(records) == 0:
                ok(f"分类筛选 ({cat['code']})", "无数据")
            else:
                fail(f"分类筛选 ({cat['code']})", f"包含非{cat['id']}分类的梦境")
        else:
            fail(f"分类筛选 ({cat['code']})", d)

    # 4.5 我的梦境列表
    n = count()
    d, code = curl("GET", "/api/dreams?page=1&pageSize=10", user="ycs")
    if d.get("code") == 200:
        records = d.get("data", {}).get("records", [])
        uid = USERS["ycs"]["userId"]
        all_mine = all(r.get("userId") == uid for r in records)
        if all_mine:
            ok(f"我的梦境列表", f"{len(records)}条,全属于{uid}")
        else:
            fail("我的梦境列表", "混入他人梦境")
    else:
        fail("我的梦境列表", d)

    # 4.6 更新梦境
    n = count()
    d, code = curl("PUT", f"/api/dreams/{DREAM_ID}", data={
        "description": f"更新后的测试梦境 #{ts}",
        "clarity": 5
    }, user="ycs")
    if d.get("code") == 200:
        ok("更新梦境")
        d2, _ = curl("GET", f"/api/dreams/{DREAM_ID}")
        if d2.get("data", {}).get("clarity") == 5:
            ok("更新已生效")
        else:
            fail("更新未生效", d2.get("data", {}).get("clarity"))
    else:
        fail("更新梦境", d)

    # 4.7 未授权更新
    n = count()
    d, code = curl("PUT", f"/api/dreams/{DREAM_ID}", data={"description": "黑客修改"}, user="testuser2")
    if d.get("code") != 200:
        ok("未授权更新被拒绝")
    else:
        fail("未授权更新他人梦境", d)

    # 4.8 创建梦境 - 另一个用户
    n = count()
    d, code = curl("POST", "/api/dreams", data={
        "categoryId": cat_id,
        "description": f"用户B的测试梦 #{ts}",
        "clarity": 2,
        "dreamDate": "2026-05-14",
        "keywords": "用户B",
        "isRecurring": 0
    }, user="testuser2")
    if d.get("code") == 200:
        dream_b_id = d.get("data", {}).get("id")
        ok("用户B创建梦境", dream_b_id)
        # 审核通过
        if dream_b_id and approve_dream(dream_b_id):
            ok("用户B梦境审核通过")
        elif dream_b_id:
            fail("用户B梦境审核通过", "未找到审核记录")
    else:
        fail("用户B创建梦境", d)


# ============ 5. 点赞测试 ============
def test_likes():
    global DREAM_ID
    print("\n========== 5. 点赞测试 ==========")

    if not DREAM_ID:
        fail("点赞测试", "no dream created")
        return

    # 5.1 用户B 点赞 ycs 的梦
    n = count()
    d, code = curl("POST", f"/api/dreams/{DREAM_ID}/like", user="testuser2")
    if d.get("code") == 200:
        ok("用户B点赞")
    else:
        fail("用户B点赞", d)

    # 5.2 重复点赞
    n = count()
    d, code = curl("POST", f"/api/dreams/{DREAM_ID}/like", user="testuser2")
    if d.get("code") != 200:
        ok("重复点赞被拒绝")
    else:
        fail("重复点赞未拦截", d)

    # 5.3 ycs 也点赞
    n = count()
    d, code = curl("POST", f"/api/dreams/{DREAM_ID}/like", user="ycs")
    if d.get("code") == 200:
        ok("ycs点赞")
    else:
        fail("ycs点赞", d)

    # 5.4 取消点赞
    n = count()
    d, code = curl("POST", f"/api/dreams/{DREAM_ID}/unlike", user="ycs")
    if d.get("code") == 200:
        ok("取消点赞")
    else:
        fail("取消点赞", d)

    # 5.5 梦境 stats
    n = count()
    d, code = curl("GET", f"/api/dreams/{DREAM_ID}/stats")
    if d.get("code") == 200:
        ok("梦境统计", json.dumps(d["data"])[:100])
    else:
        fail("梦境统计", d)


# ============ 6. 关注测试 ============
def test_follow():
    print("\n========== 6. 关注测试 ==========")

    uid1 = USERS.get("ycs", {}).get("userId")
    uid2 = USERS.get("testuser2", {}).get("userId")
    if not uid1 or not uid2:
        fail("关注测试", "missing userId")
        return

    # 6.1 关注
    n = count()
    d, code = curl("POST", f"/api/follow/{uid2}", user="ycs")
    if d.get("code") == 200:
        ok("ycs 关注 testuser2")
    else:
        fail("关注", d)

    # 6.2 重复关注
    n = count()
    d, code = curl("POST", f"/api/follow/{uid2}", user="ycs")
    if d.get("code") != 200:
        ok("重复关注被拒绝")
    else:
        fail("重复关注未拦截", d)

    # 6.3 关注状态
    n = count()
    d, code = curl("GET", f"/api/follow/status/{uid2}", user="ycs")
    if d.get("code") == 200:
        data = d.get("data", {})
        if data.get("isFollowing") == 1:
            ok("关注状态正确", "isFollowing=1")
        else:
            fail("关注状态", data, "isFollowing=1")
    else:
        fail("关注状态", d)

    # 6.4 粉丝列表
    n = count()
    d, code = curl("GET", f"/api/follow/followers/{uid2}?page=1&pageSize=10")
    if d.get("code") == 200:
        ok("粉丝列表", json.dumps(d["data"])[:100])
    else:
        fail("粉丝列表", d)

    # 6.5 取关
    n = count()
    d, code = curl("DELETE", f"/api/follow/{uid2}", user="ycs")
    if d.get("code") == 200:
        ok("取关成功")
    else:
        fail("取关", d)


# ============ 7. 讨论组 + 评论 测试 ============
def test_discussion_comment():
    global DISC_ID, COMMENT_ID
    print("\n========== 7. 讨论组 + 评论 ==========")

    ts = int(time.time())

    # 7.1 创建讨论组
    n = count()
    d, code = curl("POST", "/api/discussions", data={
        "title": f"测试讨论组 #{ts}",
        "description": "自动化测试用讨论组",
        "type": 0
    }, user="ycs")
    if d.get("code") == 200 and d.get("data", {}).get("id"):
        DISC_ID = d["data"]["id"]
        ok("创建讨论组", f"id={DISC_ID}")
    else:
        fail("创建讨论组", d)
        return

    # 7.2 获取讨论组详情
    n = count()
    d, code = curl("GET", f"/api/discussions/{DISC_ID}")
    if d.get("code") == 200:
        data = d.get("data", {})
        ok("讨论组详情", f"title={data.get('title')}")
    else:
        fail("讨论组详情", d)

    # 7.3 添加评论
    n = count()
    d, code = curl("POST", "/api/comments", data={
        "discussionId": DISC_ID,
        "content": "这是一条测试评论"
    }, user="ycs")
    if d.get("code") == 200 and (d.get("data", {}).get("id") or d.get("data", {}).get("commentId")):
        COMMENT_ID = d["data"].get("id") or d["data"].get("commentId")
        ok("添加评论", f"id={COMMENT_ID}")
    else:
        fail("添加评论", d)
        return

    # 7.35 用户B加入讨论组 (必须先加入才能评论)
    n = count()
    d, code = curl("POST", f"/api/discussions/{DISC_ID}/join", user="testuser2")
    if d.get("code") == 200:
        ok("用户B加入讨论组")
    else:
        fail("用户B加入讨论组", d)

    # 7.4 用户B 也评论
    n = count()
    d, code = curl("POST", "/api/comments", data={
        "discussionId": DISC_ID,
        "content": "用户B的评论"
    }, user="testuser2")
    if d.get("code") == 200:
        ok("用户B评论", d.get("data", {}).get("id"))
    else:
        fail("用户B评论", d)

    # 7.5 评论列表
    n = count()
    d, code = curl("GET", f"/api/comments/{DISC_ID}?page=1&pageSize=10&sortBy=newest")
    if d.get("code") == 200:
        records = d.get("data", {}).get("records", [])
        ok("评论列表", f"{len(records)}条")
    else:
        fail("评论列表", d)

    # 7.6 更新评论
    if COMMENT_ID:
        n = count()
        d, code = curl("PUT", f"/api/comments/{COMMENT_ID}", data={"content": "更新后的评论"}, user="ycs")
        if d.get("code") == 200:
            ok("更新评论")
        else:
            fail("更新评论", d)

    # 7.7 评论点赞
    if COMMENT_ID:
        n = count()
        d, code = curl("POST", f"/api/comments/{COMMENT_ID}/like", user="testuser2")
        if d.get("code") == 200:
            ok("评论点赞")
        else:
            fail("评论点赞", d)

    # 7.8 讨论组成员 (用户B已在上面加入)
    n = count()
    d, code = curl("GET", f"/api/discussions/{DISC_ID}/members")
    if d.get("code") == 200:
        members = d.get("data", [])
        if isinstance(members, list):
            ok("讨论组成员", f"{len(members)}人")
        else:
            ok("讨论组成员", str(members)[:80])
    else:
        fail("讨论组成员", d)

    # 7.10 退出讨论组
    n = count()
    d, code = curl("POST", f"/api/discussions/{DISC_ID}/leave", user="testuser2")
    if d.get("code") == 200:
        ok("退出讨论组")
    else:
        fail("退出讨论组", d)

    # 7.11 删除评论
    if COMMENT_ID:
        n = count()
        d, code = curl("DELETE", f"/api/comments/{COMMENT_ID}", user="ycs")
        if d.get("code") == 200:
            ok("删除评论")
        else:
            fail("删除评论", d)


# ============ 8. 通知测试 ============
def test_notifications():
    print("\n========== 8. 通知测试 ==========")

    # 8.1 通知列表
    n = count()
    d, code = curl("GET", "/api/notifications?page=1&pageSize=10", user="ycs")
    if d.get("code") == 200:
        records = d.get("data", {}).get("records", [])
        ok("通知列表", f"{len(records)}条")
    else:
        fail("通知列表", d)

    # 8.2 未读通知数
    n = count()
    d, code = curl("GET", "/api/notifications/unread-count", user="ycs")
    if d.get("code") == 200:
        ok("未读通知数", d.get("data", 0))
    else:
        fail("未读通知数", d)

    # 8.3 按未读筛选
    n = count()
    d, code = curl("GET", "/api/notifications?page=1&pageSize=10&isRead=0", user="ycs")
    if d.get("code") == 200:
        records = d.get("data", {}).get("records", [])
        all_unread = all(r.get("isRead") == 0 for r in records)
        if all_unread:
            ok("未读筛选", f"{len(records)}条未读,全匹配")
        else:
            fail("未读筛选", "包含已读通知")
    else:
        fail("未读筛选", d)

    # 8.4 全部已读
    n = count()
    d, code = curl("PUT", "/api/notifications/read-all", user="ycs")
    if d.get("code") == 200:
        ok("全部已读")
        d2, _ = curl("GET", "/api/notifications/unread-count", user="ycs")
        if d2.get("data", -1) == 0:
            ok("全部已读后未读数为0")
        else:
            fail("全部已读后仍有未读", d2.get("data"))
    else:
        fail("全部已读", d)


# ============ 9. 用户资料 ============
def test_user_profile():
    print("\n========== 9. 用户资料 ==========")

    uid = USERS.get("ycs", {}).get("userId")
    if not uid:
        fail("用户资料", "no ycs userId")
        return

    # 9.1 我的资料
    n = count()
    d, code = curl("GET", "/api/users/me", user="ycs")
    if d.get("code") == 200:
        data = d.get("data", {})
        ok("获取我的资料", f"nickname={data.get('nickname')}")
        if "dreamCount" in data:
            ok("含 dreamCount", data.get("dreamCount"))
        else:
            fail("缺少 dreamCount", data)
    else:
        fail("获取我的资料", d)

    # 9.2 更新资料
    n = count()
    ts = int(time.time())
    d, code = curl("PUT", "/api/users/me", data={"nickname": f"更新昵称_{ts}", "bio": "测试签名"}, user="ycs")
    if d.get("code") == 200:
        ok("更新资料")
    else:
        fail("更新资料", d)

    # 9.3 他人资料
    n = count()
    d, code = curl("GET", f"/api/users/{uid}")
    if d.get("code") == 200:
        ok("获取他人资料", f"nickname={d['data'].get('nickname')}")
    else:
        fail("获取他人资料", d)

    # 9.4 用户梦境列表
    n = count()
    d, code = curl("GET", f"/api/users/{uid}/dreams?page=1&pageSize=5")
    if d.get("code") == 200:
        records = d.get("data", {}).get("records", [])
        ok(f"用户梦境列表", f"{len(records)}条")
    else:
        fail("用户梦境列表", d)


# ============ 10. 搜索 ============
def test_search():
    print("\n========== 10. 搜索 ==========")

    # 10.1 搜索梦境
    n = count()
    d, code = curl("GET", "/api/search", data={"keyword": "测试", "type": "dream", "page": 1, "pageSize": 10}, user="ycs")
    if d.get("code") == 200:
        data = d.get("data", {})
        records = data.get("dreams", [])
        ok("搜索梦境", f"{len(records)}条结果")
        if records:
            has_match = any("测试" in (r.get("description") or "") or "测试" in (r.get("keywords") or "") for r in records)
            if has_match:
                ok("搜索结果包含关键词")
            else:
                fail("搜索结果不含关键词", "未找到匹配项")
    else:
        fail("搜索梦境", d)

    # 10.2 热门标签 (无 keyword 也支持)
    n = count()
    d, code = curl("GET", "/api/search/tags")
    if d.get("code") == 200:
        ok("热门标签", json.dumps(d["data"])[:100])
    else:
        fail("热门标签", d)


# ============ 11. 个人设置 ============
def test_settings():
    print("\n========== 11. 个人设置 ==========")

    # 11.1 获取设置
    n = count()
    d, code = curl("GET", "/api/settings", user="ycs")
    if d.get("code") == 200:
        data = d.get("data", {})
        ok("获取设置", json.dumps(data)[:100])
    else:
        fail("获取设置", d)

    # 11.2 修改设置
    n = count()
    d, code = curl("PUT", "/api/settings", data={"pushEnabled": 0, "isAnonymousEnabled": 1}, user="ycs")
    if d.get("code") == 200:
        ok("修改设置")
    else:
        fail("修改设置", d)

    # 11.3 修改密码 — 跳过，因为会改变 token 导致后续测试 401
    print("  [SKIP] #20 修改密码 (会改变token影响后续测试)")


# ============ 12. 个人统计 ============
def test_stats():
    print("\n========== 12. 个人统计 ==========")

    n = count()
    d, code = curl("GET", "/api/stats/me", user="ycs")
    if d.get("code") == 200:
        data = d.get("data", {})
        ok("个人统计", json.dumps(data)[:150])
    else:
        fail("个人统计", d)


# ============ 13. JWT 验证 ============
def test_jwt():
    print("\n========== 13. JWT 验证 ==========")

    # 13.1 有效 token
    n = count()
    token = USERS.get("ycs", {}).get("token", "")
    d, code = curl("GET", "/api/auth/verify")
    # 手动注入 header 测试 - 使用 curl 函数逻辑
    import subprocess as sp
    url = f"{BASE}/api/auth/verify"
    r = sp.run(["curl", "-s", "-X", "GET", url, "-H", f"Authorization: Bearer {token}"], capture_output=True, text=True, timeout=5)
    d = json.loads(r.stdout) if r.stdout.strip() else {"code": 500}
    if d.get("code") == 200 and d.get("data") == True:
        ok("有效 token 验证通过")
    else:
        fail("有效 token 验证", d)

    # 13.2 无 token 访问受保护接口
    n = count()
    d, code = curl("GET", "/api/users/me")
    if d.get("code") not in (200,):
        ok("无 token 被拒绝", d.get("code"))
    else:
        fail("无 token 仍可访问", d)

    # 13.3 伪造 token
    n = count()
    result = subprocess.run(
        ["curl", "-s", "-w", "\n%{http_code}", "-X", "GET", f"{BASE}/api/users/me", "-H", "Authorization: Bearer fake.token.xyz"],
        capture_output=True, text=True, timeout=5
    )
    code = result.stdout.strip().split("\n")[-1]
    if code != "200":
        ok("伪造 token 被拒绝", code)
    else:
        fail("伪造 token 通过", code)


# ============ 14. 文件上传 ============
def test_upload():
    print("\n========== 14. 文件上传 ==========")

    token = USERS.get("ycs", {}).get("token", "")
    auth_header = f"Authorization: Bearer {token}" if token else ""

    # 14.1 上传有效 PNG
    n = count()
    png_file = "/tmp/test_upload.png"
    # 生成最小有效 PNG (1x1 pixel red)
    subprocess.run(f"printf '\\x89PNG\\r\\n\\x1a\\n\\x00\\x00\\x00\\rIHDR\\x00\\x00\\x00\\x01\\x00\\x00\\x00\\x01\\x08\\x02\\x00\\x00\\x00\\x90wS\\xde\\x00\\x00\\x00\\x0cIDATx\\x9c\\xcf\\x00\\x00\\x00\\x01\\x00\\x01\\x00\\x01\\x00\\xa5\\x08\\x21\\x1c\\x00\\x00\\x00\\x00IEND\\xaeB\\x60\\x3d' > {png_file}", shell=True)

    r = subprocess.run(
        ["curl", "-s", "-F", f"file=@{png_file}", "-H", auth_header, f"{BASE}/api/upload/image"],
        capture_output=True, text=True, timeout=10
    )
    d = json.loads(r.stdout) if r.stdout.strip() else {"code": 500}
    if d.get("code") == 200 and d.get("data", {}).get("url"):
        ok("上传PNG成功", d["data"]["url"])
    else:
        fail("上传PNG", d)

    # 14.2 上传非图片
    txt_file = "/tmp/test_upload.txt"
    subprocess.run(f"echo 'not an image' > {txt_file}", shell=True)
    n = count()
    r = subprocess.run(
        ["curl", "-s", "-F", f"file=@{txt_file}", "-H", auth_header, f"{BASE}/api/upload/image"],
        capture_output=True, text=True, timeout=10
    )
    d = json.loads(r.stdout) if r.stdout.strip() else {"code": 500}
    if d.get("code") != 200:
        ok("非图片被拒绝", d.get("message", d.get("code")))
    else:
        fail("非图片未拦截", d)


# ============ 15. 管理后台 ============
def test_admin():
    print("\n========== 15. 管理后台 ==========")

    admin_token = USERS.get("admin", {}).get("token")
    if not admin_token:
        print("  [SKIP] admin 未登录，跳过管理后台测试")
        return

    # 15.1 Dashboard 统计
    n = count()
    d, code = curl("GET", "/api/admin/stats", user="admin")
    if d.get("code") == 200:
        ok("管理统计", json.dumps(d["data"])[:100])
    else:
        fail("管理统计", d)

    # 15.2 用户列表
    n = count()
    d, code = curl("GET", "/api/admin/users?page=1&pageSize=10", user="admin")
    if d.get("code") == 200:
        records = d.get("data", {}).get("records", [])
        ok(f"管理用户列表", len(records))
    else:
        fail("管理用户列表", d)

    # 15.3 梦境列表
    n = count()
    d, code = curl("GET", "/api/admin/dreams?page=1&pageSize=10", user="admin")
    if d.get("code") == 200:
        records = d.get("data", {}).get("records", [])
        ok(f"管理梦境列表", len(records))
    else:
        fail("管理梦境列表", d)

    # 15.4 审核列表
    n = count()
    d, code = curl("GET", "/api/admin/audits?page=1&pageSize=10", user="admin")
    if d.get("code") == 200:
        ok("审核列表", json.dumps(d["data"])[:100])
    else:
        fail("审核列表", d)

    # 15.5 关键词列表
    n = count()
    d, code = curl("GET", "/api/admin/keywords?page=1&pageSize=10", user="admin")
    if d.get("code") == 200:
        ok("关键词列表", json.dumps(d["data"])[:100])
    else:
        fail("关键词列表", d)

    # 15.6 操作日志
    n = count()
    d, code = curl("GET", "/api/admin/logs?page=1&pageSize=10", user="admin")
    if d.get("code") == 200:
        ok("操作日志", json.dumps(d["data"])[:100])
    else:
        fail("操作日志", d)

    # 15.7 通知管理
    n = count()
    d, code = curl("GET", "/api/admin/notifications?page=1&pageSize=10", user="admin")
    if d.get("code") == 200:
        ok("通知管理", json.dumps(d["data"])[:100])
    else:
        fail("通知管理", d)

    # 15.8 讨论组管理
    n = count()
    d, code = curl("GET", "/api/admin/discussions?page=1&pageSize=10", user="admin")
    if d.get("code") == 200:
        ok("讨论组管理", json.dumps(d["data"])[:100])
    else:
        fail("讨论组管理", d)

    # 15.9 评论管理
    n = count()
    d, code = curl("GET", "/api/admin/comments?page=1&pageSize=10", user="admin")
    if d.get("code") == 200:
        ok("评论管理", json.dumps(d["data"])[:100])
    else:
        fail("评论管理", d)

    # 15.10 非管理员访问管理接口
    n = count()
    d, code = curl("GET", "/api/admin/users?page=1", user="ycs")
    if d.get("code") != 200:
        ok("非管理员访问管理接口被拒绝", d.get("code"))
    else:
        fail("非管理员可访问管理接口", d)


# ============ 16. 边界/异常 ============
def test_edge_cases():
    print("\n========== 16. 边界/异常 ==========")

    # 16.1 不存在的梦境
    n = count()
    d, code = curl("GET", "/api/dreams/99999999")
    if d.get("code") != 200:
        ok("不存在的梦境返回错误", d.get("code"))
    else:
        fail("不存在的梦境返回了数据", d)

    # 16.2 不存在的用户
    n = count()
    d, code = curl("GET", "/api/users/99999999")
    if d.get("code") != 200:
        ok("不存在的用户返回错误", d.get("code"))
    else:
        fail("不存在的用户返回了数据", d)

    # 16.3 大页码
    n = count()
    d, code = curl("GET", "/api/dreams/feed?page=99999&pageSize=10")
    if d.get("code") == 200:
        records = d.get("data", {}).get("records", [])
        if len(records) == 0:
            ok("大页码返回空列表")
        else:
            fail("大页码返回了数据", len(records))
    else:
        fail("大页码请求", d)

    # 16.4 SQL 注入尝试
    n = count()
    d, code = curl("GET", "/api/search", data={"keyword": "' OR 1=1 --", "type": "dream"}, user="ycs")
    # 只要不崩溃就行
    if d.get("code") is not None:
        ok("SQL注入未导致崩溃", d.get("code"))
    else:
        fail("SQL注入导致异常", d)

    # 16.5 关注自己
    uid = USERS.get("ycs", {}).get("userId")
    if uid:
        n = count()
        d, code = curl("POST", f"/api/follow/{uid}", user="ycs")
        if d.get("code") != 200:
            ok("关注自己被拒绝", d.get("message", ""))
        else:
            fail("可以关注自己", d)


# ============ 17. 推荐梦境 ============
def test_recommended():
    print("\n========== 17. 推荐/相似梦境 ==========")

    # 17.1 推荐讨论组
    n = count()
    d, code = curl("GET", "/api/discussions/recommended?page=1&pageSize=10", user="ycs")
    if d.get("code") == 200:
        ok("推荐讨论组", json.dumps(d["data"])[:100])
    else:
        fail("推荐讨论组", d)

    # 17.2 相似梦境
    if DREAM_ID:
        n = count()
        d, code = curl("GET", f"/api/dreams/{DREAM_ID}/similar?limit=5")
        if d.get("code") == 200:
            ok("相似梦境", json.dumps(d["data"])[:150])
        else:
            fail("相似梦境", d)


# ============ 主函数 ============
def main():
    global PASSED, FAILED, TEST_NUM

    print("=" * 60)
    print("  Dream Share 自动化测试")
    print(f"  目标: {BASE}")
    print(f"  时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    # 检查服务是否在线
    try:
        r = requests.get(f"{BASE}/api/categories", timeout=5)
        if r.status_code == 200:
            print("  [OK] 后端服务在线\n")
        else:
            print(f"  [WARN] 后端响应: {r.status_code}\n")
    except Exception as e:
        print(f"  [ERROR] 后端不可达: {e}")
        sys.exit(1)

    # 运行所有测试
    tests = [
        ("注册", test_register),
        ("登录", test_login),
        ("分类", test_categories),
        ("梦境CRUD", test_dreams),
        ("点赞", test_likes),
        ("关注", test_follow),
        ("讨论组+评论", test_discussion_comment),
        ("通知", test_notifications),
        ("用户资料", test_user_profile),
        ("搜索", test_search),
        ("个人设置", test_settings),
        ("个人统计", test_stats),
        ("JWT验证", test_jwt),
        ("文件上传", test_upload),
        ("管理后台", test_admin),
        ("边界/异常", test_edge_cases),
        ("推荐/相似", test_recommended),
    ]

    for name, func in tests:
        try:
            func()
        except Exception as e:
            print(f"  [ERROR] {name} 模块异常: {e}")
            import traceback
            traceback.print_exc()

    # 清理
    run_cleanup()

    # 汇总
    total = PASSED + FAILED
    print("\n" + "=" * 60)
    print(f"  测试结果汇总: 总共 {total} 项")
    print(f"  [PASS] {PASSED} 项通过 ({PASSED*100//total if total else 0}%)")
    print(f"  [FAIL] {FAILED} 项失败")
    print("=" * 60)

    if FAILED > 0:
        print("\n--- 失败详情 ---")
        for err in ERRORS:
            print(f"  {err}")
        print()

    return FAILED


if __name__ == "__main__":
    failed = main()
    sys.exit(1 if failed > 0 else 0)
