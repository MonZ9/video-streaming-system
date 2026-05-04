const BASE = "http://localhost:8080/video_streaming_system_war_exploded";

// ================= 当前状态 =================
let CURRENT_USER_ID = null;
let CURRENT_IS_ADMIN = false;
let CURRENT_VIDEO_USER_ID = null;

// ================= 通用请求（统一Token版） =================
function request(url, options = {}) {

    const token = localStorage.getItem("token");

    const headers = options.headers || {};

    // ✅ 统一使用 Authorization（避免后端混乱）
    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }

    return fetch(url, {
        ...options,
        headers,
        credentials: "include"
    }).then(res => res.json());
}

// ================= 获取当前用户 =================
function loadUser() {
    return request(`${BASE}/api/user/getUserInfo`)
        .then(res => {

            if (res.success) {
                CURRENT_USER_ID = res.data.id;
                CURRENT_IS_ADMIN = res.data.isAdmin;

                console.log("当前用户ID:", CURRENT_USER_ID);
                console.log("是否管理员:", CURRENT_IS_ADMIN);

            } else {
                alert(res.message || "请先登录");
                localStorage.removeItem("token");
                window.location.href = "login.html";
            }
        })
        .catch(err => {
            console.error("获取用户失败:", err);
            alert("网络异常，无法获取用户信息");
        });
}

// ================= 登录 =================
function login() {

    const username = document.getElementById("login_username").value;
    const password = document.getElementById("login_password").value;

    request(`${BASE}/api/user/login?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`)
        .then(res => {

            console.log("登录返回:", res);

            if (res.success) {

                localStorage.setItem("token", res.token);

                console.log("登录成功，跳转首页");

                window.location.href = "index.html";

            } else {
                alert(res.message || "登录失败");
            }

        })
        .catch(err => {
            console.error("登录异常:", err);
            alert("网络异常");
        });
}

// ================= 注册 =================
function register() {

    const username = document.getElementById("reg_username").value;
    const password = document.getElementById("reg_password").value;
    const isAdmin = document.getElementById("reg_admin").value;

    if (!username || !password) {
        alert("用户名或密码不能为空");
        return;
    }

    request(`${BASE}/api/user/register?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}&isAdmin=${isAdmin}`)
        .then(res => {

            alert(res.message || "操作完成");

            if (res.success) {
                window.location.href = "login.html";
            }
        })
        .catch(err => {
            console.error("注册失败:", err);
            alert("网络异常");
        });
}

// ================= 获取视频（核心修复） =================
function getVideos() {

    request(`${BASE}/api/video/getAllVideos`)
        .then(data => {

            console.log("视频接口返回：", data);

            // ❗ 仅在“明确未登录/过期”时跳转
            if (data.success === false &&
                (data.message === "未登录" || data.message === "登录已过期")) {

                alert("登录已失效，请重新登录");
                localStorage.removeItem("token");
                window.location.href = "login.html";
                return;
            }

            // ❗ 其他错误不跳转
            if (!data.success) {
                alert(data.message || "获取视频失败");
                return;
            }

            let list = data.data || [];

            if (list.length === 0) {
                document.getElementById("videoList").innerHTML = "<p>暂无视频</p>";
                return;
            }

            list.sort(() => Math.random() - 0.5);
            let showList = list.slice(0, 3);

            let html = "";

            showList.forEach(v => {

                html += `
                    <div class="card">
                        <h3 onclick="goToVideo(${v.id})">${v.title}</h3>
                        <video src="${v.url}" controls></video>
                        <p>${v.description || "无描述"}</p>
                        <button onclick="goToVideo(${v.id})">查看详情</button>
                        <button onclick="deleteVideoById(${v.id})">删除</button>
                    </div>
                `;
            });

            document.getElementById("videoList").innerHTML = html;

        })
        .catch(err => {
            console.error("请求失败:", err);
            alert("网络或服务器异常");
        });
}

// ================= 跳转视频 =================
function goToVideo(id) {
    window.location.href = `video.html?id=${id}`;
}

// ================= 删除视频 =================
function deleteVideoById(id) {

    if (!confirm("确定删除这个视频吗？")) return;

    request(`${BASE}/api/video/deleteVideo?id=${id}`)
        .then(res => {

            alert(res.message || "操作完成");

            if (res.success) {
                getVideos();
            }
        })
        .catch(err => {
            console.error("删除失败:", err);
            alert("删除失败");
        });
}

// ================= 上传视频 =================
function uploadVideo() {

    const form = document.getElementById("uploadForm");
    const formData = new FormData(form);

    if (!formData.get("title") || !formData.get("videoFile").name) {
        alert("请填写完整信息");
        return;
    }

    request(`${BASE}/api/video/upload`, {
        method: "POST",
        body: formData
    })
        .then(res => {

            alert(res.message || "上传完成");

            if (res.success) {
                form.reset();
                getVideos();
            }
        })
        .catch(err => {
            console.error("上传失败:", err);
            alert("上传失败");
        });
}

// ================= 评论 =================
function submitComment() {

    const params = new URLSearchParams(window.location.search);
    const videoId = params.get("id");
    const content = document.getElementById("comment_input").value;

    if (!content.trim()) {
        alert("评论不能为空");
        return;
    }

    request(`${BASE}/api/comment/addComment?videoId=${videoId}&content=${encodeURIComponent(content)}`)
        .then(res => {

            alert(res.message || "操作完成");

            if (res.success) {
                document.getElementById("comment_input").value = "";
                loadComments(videoId);
            }
        })
        .catch(err => {
            console.error("评论失败:", err);
            alert("评论失败");
        });
}

// ================= 加载评论 =================
function loadComments(videoId) {

    if (CURRENT_USER_ID === null) {
        setTimeout(() => loadComments(videoId), 200);
        return;
    }

    request(`${BASE}/api/comment/getCommentsByVideoId?videoId=${videoId}`)
        .then(data => {

            let html = "";

            if (!data.success || !data.data || data.data.length === 0) {
                html = "<p>暂无评论</p>";
            } else {

                data.data.forEach(c => {

                    let canDelete =
                        Number(c.userId) === Number(CURRENT_USER_ID) ||
                        Number(CURRENT_VIDEO_USER_ID) === Number(CURRENT_USER_ID) ||
                        CURRENT_IS_ADMIN === true;

                    let deleteBtn = canDelete
                        ? `<button onclick="deleteComment(${c.id}, ${videoId})">删除</button>`
                        : "";

                    html += `
                        <div class="comment-item">
                            <b>${c.username}</b>：${c.content}<br/>
                            <small>${c.createdAt}</small>
                            ${deleteBtn}
                        </div>
                    `;
                });
            }

            document.getElementById("commentList").innerHTML = html;

        })
        .catch(err => {
            console.error("评论加载失败:", err);
            document.getElementById("commentList").innerHTML = "<p>评论加载失败</p>";
        });
}

// ================= 删除评论 =================
function deleteComment(commentId, videoId) {

    if (!confirm("确定删除吗？")) return;

    request(`${BASE}/api/comment/deleteComment?id=${commentId}`)
        .then(res => {

            alert(res.message || "操作完成");

            if (res.success) {
                loadComments(videoId);
            }
        })
        .catch(err => {
            console.error("删除评论失败:", err);
            alert("删除失败");
        });
}

// ================= 退出登录 =================
function logout() {

    localStorage.removeItem("token");

    request(`${BASE}/api/user/logout`)
        .then(() => {
            window.location.href = "login.html";
        })
        .catch(() => {
            window.location.href = "login.html";
        });
}