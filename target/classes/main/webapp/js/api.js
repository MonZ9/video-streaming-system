const BASE = "http://localhost:8080/video_streaming_system_war_exploded";

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
// ================= 获取当前用户 =================
function loadUser() {
    return request(`${BASE}/api/user/getUserInfo`)
        .then(res => {

            if (res.success) {

                // 挂载到 window，避免重复声明变量
                window.CURRENT_USER_ID = res.data.id;
                window.CURRENT_IS_ADMIN = res.data.isAdmin;

                console.log("当前用户ID:", window.CURRENT_USER_ID);
                console.log("是否管理员:", window.CURRENT_IS_ADMIN);
                console.log("user接口返回：", res);

                // 控制管理员按钮显示
                const adminBtn = document.getElementById("adminBtn");
                if (adminBtn) {
                    adminBtn.style.display = res.data.isAdmin === true ? "inline-block" : "none";
                }

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

    if (!username || !password) {
        alert("用户名或密码不能为空");
        return;
    }

    request(`${BASE}/api/user/register?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`)
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

// ================= 点赞视频 =================
function likeVideo(videoId) {

    request(`${BASE}/api/like/likeVideo?videoId=${videoId}`)
        .then(res => {
            alert(res.message || "操作完成");
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
                // 由页面自己重新加载视频列表（调用页面的 getVideos）
                if (typeof getVideos === 'function') {
                    getVideos();
                }
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
                // 由页面自己重新加载视频列表
                if (typeof getVideos === 'function') {
                    getVideos();
                }
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

    if (window.CURRENT_USER_ID === null || window.CURRENT_USER_ID === undefined) {
        setTimeout(() => loadComments(videoId), 200);
        return;
    }

    // ⭐ 支持前端排序：time / hot
    const sort = document.querySelector('input[name="sort"]:checked')?.value || "time";

    request(`${BASE}/api/comment/getCommentsByVideoId?videoId=${videoId}&sort=${sort}`)
        .then(data => {

            let html = "";

            if (!data.success || !data.data || data.data.length === 0) {
                html = "<p>暂无评论</p>";
            } else {

                data.data.forEach(c => {

                    // ⭐ 判断是否可以删除（RBAC）
                    let canDelete =
                        Number(c.userId) === Number(window.CURRENT_USER_ID) ||
                        Number(window.CURRENT_VIDEO_USER_ID) === Number(window.CURRENT_USER_ID) ||
                        window.CURRENT_IS_ADMIN === true;

                    let deleteBtn = canDelete
                        ? `<button onclick="deleteComment(${c.id})" style="color:red;margin-left:5px;">删除</button>`
                        : "";

                    let createdAt = c.createdAt ? new Date(c.createdAt).toLocaleString() : "";

                    html += `
                        <div class="comment-item">
                            <div class="comment-user">
                                ${c.username || ("用户" + c.userId)}
                                <span style="color:#999;font-size:12px;margin-left:10px;">
                                    ${createdAt}
                                </span>
                            </div>
                            <p>${c.content}</p>
                            <div>
                                <span class="like-count">👍 ${c.likeCount || 0}</span>
                                <button onclick="likeComment(${c.id})">点赞</button>
                                ${deleteBtn}
                            </div>
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

// ================= 点赞评论 =================
function likeComment(commentId) {

    request(`${BASE}/api/like/likeComment?commentId=${commentId}`)
        .then(res => {

            alert(res.message || "操作完成");

            if (res.success) {
                const videoId = new URLSearchParams(window.location.search).get("id");
                loadComments(videoId);
            }
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

// ================= 申请管理员 =================
function applyAdmin() {

    if (!window.CURRENT_USER_ID) {
        alert("请先登录");
        return;
    }

    request(`${BASE}/api/admin/apply`)
        .then(res => {
            alert(res.message || "操作完成");
        })
        .catch(err => {
            console.error("申请失败:", err);
            alert("申请失败");
        });
}

// ====== 管理员审批 ======
function goAdmin() {
    window.location.href = "admin.html";
}

function updateUIByRole(roleId) {

    const adminBtn = document.getElementById("adminBtn");
    const applyBtn = document.querySelector("button[onclick='applyAdmin()']");

    if (adminBtn) {
        adminBtn.style.display = (roleId === 1) ? "inline-block" : "none";
    }

    if (applyBtn) {
        applyBtn.style.display = (roleId === 0) ? "inline-block" : "none";
    }
}

// 备用点赞视频详情函数（可能在 video.html 内重写）
function likeVideoDetail() {
    const videoId = new URLSearchParams(window.location.search).get("id");
    request(`${BASE}/api/like/likeVideo?videoId=${videoId}`)
        .then(res => {
            alert(res.message || "操作完成");
            if (res.success && typeof loadVideoDetail === 'function') {
                loadVideoDetail();
            }
        })
        .catch(() => alert("点赞失败"));
}