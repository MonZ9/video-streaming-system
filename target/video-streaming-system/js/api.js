const BASE = "http://localhost:8080/video_streaming_system_war_exploded";

// 当前登录用户
let CURRENT_USER_ID = null;
let CURRENT_IS_ADMIN = false;

// 当前视频作者
let CURRENT_VIDEO_USER_ID = null;

// ================= 通用请求 =================
function request(url, options = {}) {
    console.log("请求开始:", url);

    return fetch(url, {
        credentials: "include",
        ...options
    })
        .then(res => {
            console.log("响应状态:", res.status);
            if (!res.ok) throw new Error("网络异常");
            return res.json();
        })
        .then(data => {
            console.log("返回数据:", data);
            return data;
        })
        .catch(err => {
            console.error("请求失败:", err);
            throw err;
        });
}

// ================= 获取用户 =================
function loadUser() {
    return request(`${BASE}/api/user/getUserInfo`)
        .then(res => {
            if (res.success) {
                CURRENT_USER_ID = res.data.id;
                CURRENT_IS_ADMIN = res.data.isAdmin; //管理员信息
                console.log("当前用户ID:", CURRENT_USER_ID);
                console.log("是否管理员:", CURRENT_IS_ADMIN);
            } else {
                alert("请先登录");
                window.location.href = "login.html";
            }
        })
        .catch(() => alert("获取用户失败"));
}

//=============注册==========
function register() {
    const username = document.getElementById("reg_username").value;
    const password = document.getElementById("reg_password").value;
    const isAdmin = document.getElementById("reg_admin").value;

    if (!username || !password) {
        alert("用户名或密码不能为空");
        return;
    }

    request(`${BASE}/api/user/register?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}&isAdmin=${isAdmin}`)
        .then(data => {
            alert(data.message);

            if (data.success) {
                // 注册成功跳转登录页
                window.location.href = "login.html";
            }
        })
        .catch(() => {
            alert("注册失败");
        });
}

// ================= 获取视频详情（用于拿作者ID） =================
function loadVideoInfo(videoId) {
    return request(`${BASE}/api/video/getVideo?id=${videoId}`)
        .then(res => {
            if (res.success) {
                CURRENT_VIDEO_USER_ID = res.data.userId; //视频作者
                console.log("视频作者ID:", CURRENT_VIDEO_USER_ID);
            }
        });
}

// ================= 获取视频（只显示3个随机视频） =================
function getVideos() {
    request(`${BASE}/api/video/getAllVideos`)
        .then(data => {
            if (!data.success) {
                alert("请先登录！");
                window.location.href = "login.html";
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
                        <button onclick="deleteVideo(${v.id})">删除</button>
                    </div>
                `;
            });

            document.getElementById("videoList").innerHTML = html;
        })
        .catch(() => alert("获取视频失败"));
}

// ================= 跳转详情 =================
function goToVideo(id) {
    window.location.href = `video.html?id=${id}`;
}

// ================= 删除视频 =================
function deleteVideo(id) {
    if (!confirm("确定删除视频吗？")) return;

    request(`${BASE}/api/video/deleteVideo?id=${id}`)
        .then(res => {
            alert(res.message);
            if (res.success) getVideos();
        })
        .catch(() => alert("删除失败"));
}

// ================= 上传视频 =================
function uploadVideo() {
    const formData = new FormData(document.getElementById("uploadForm"));

    request(`${BASE}/api/video/upload`, {
        method: "POST",
        body: formData
    })
        .then(res => {
            alert(res.message);
            if (res.success) getVideos();
        })
        .catch(() => alert("上传失败"));
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
        .then(data => {
            alert(data.message);
            if (data.success) {
                document.getElementById("comment_input").value = "";
                loadComments(videoId);
            }
        })
        .catch(() => alert("评论失败"));
}

// ================= 加载评论 =================
function loadComments(videoId) {

    // 确保用户信息加载完成
    if (CURRENT_USER_ID === null) {
        setTimeout(() => loadComments(videoId), 200);
        return;
    }

    // 先获取视频作者
    loadVideoInfo(videoId).then(() => {

        request(`${BASE}/api/comment/getCommentsByVideoId?videoId=${videoId}`)
            .then(data => {
                let html = "";

                if (!data.success || !data.data || data.data.length === 0) {
                    html = "<p>暂无评论</p>";
                } else {

                    data.data.forEach(c => {

                        let canDelete = false;

                        // 权限判断（三种人）
                        if (
                            Number(c.userId) === Number(CURRENT_USER_ID) || // 评论作者
                            Number(CURRENT_VIDEO_USER_ID) === Number(CURRENT_USER_ID) || // 视频作者
                            CURRENT_IS_ADMIN === true // 管理员
                        ) {
                            canDelete = true;
                        }

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
            .catch(() => {
                document.getElementById("commentList").innerHTML = "<p>评论加载失败</p>";
            });

    });
}

// ================= 删除评论 =================
function deleteComment(commentId, videoId) {
    if (!confirm("确定删除吗？")) return;

    request(`${BASE}/api/comment/deleteComment?id=${commentId}`)
        .then(data => {
            alert(data.message);
            if (data.success) loadComments(videoId);
        })
        .catch(() => alert("删除失败"));
}

// ================= 退出 =================
function logout() {
    request(`${BASE}/api/user/logout`)
        .then(() => {
            window.location.href = "login.html";
        });
}