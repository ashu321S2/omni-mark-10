const BACKEND_BASE = "";
const API_BASE = "/api";
let currentViewingPostId = null;

const el = (id) => document.getElementById(id);
const escapeHtml = (s) => s ? String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;") : "";
const formatTime = (v) => { try { return new Date(v).toLocaleString(); } catch (e) { return v; } };

// ---------------- AUTH HELPERS ----------------

function getAuthHeader() {
    const token = localStorage.getItem('accessToken');
    return token ? { 'Authorization': 'Bearer ' + token } : {};
}

function getLoggedInUsername() {
    try {
        const token = localStorage.getItem('accessToken');
        if (!token) return null;
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.sub || payload.username || null;
    } catch (e) { return null; }
}

// ---------------- UI COMPONENTS ----------------

function createInteractionRow(post) {
    const isOwner = getLoggedInUsername() === post.authorUsername;
    // We use || 0 to ensure that even if the backend sends null, the UI shows 0
    return `
        <div class="interaction-row-container">
            <button class="btn-stat-box" onclick="window.likePostDirectly(event, '${post.id}')">
                ❤️ <span id="like-count-${post.id}">${post.likes || 0}</span>
            </button>
            <button class="btn-stat-box" onclick="event.stopPropagation(); window.openPostModalById('${post.id}')">
                💬 <span>${post.comments || 0}</span>
            </button>
            ${isOwner ? `
            <button class="btn-stat-box danger-hover" onclick="window.deletePostDirectly(event, '${post.id}')" title="Delete Post?">
                🗑️
            </button>` : ''}
        </div>
    `;
}

// ---------------- POST ACTIONS ----------------

async function fetchPosts() {
    try {
        const r = await fetch(`${API_BASE}/posts`, { 
            headers: getAuthHeader() 
        });
        
        if (r.status === 401) return handleLogout();
        
        const body = await r.json();
        
        // CRITICAL FIX: Your backend returns a Page object. 
        // We must extract the array from body.content.
        const posts = body.content || (Array.isArray(body) ? body : []);
        
        if (posts.length === 0) {
            console.log("No posts found in database.");
        }

        renderCarousel(posts.slice(0, 6)); 
        renderArchive(posts);
    } catch (e) { 
        console.error("CONNECTION_ERROR", e); 
    }
}

async function submitPost() {
    const titleVal = el('title').value.trim();
    const contentVal = el('content').value.trim();
    const imageFile = el('postImage').files[0];
    const msg = el('postsMessage');

    if (!titleVal || !contentVal) {
        msg.textContent = "CRITICAL: TITLE & CONTENT REQUIRED";
        return;
    }

    const formData = new FormData();
    formData.append('title', titleVal);
    formData.append('content', contentVal);
    if (imageFile) formData.append('image', imageFile);

    try {
        msg.textContent = "UPLOADING...";
        const r = await fetch(`${API_BASE}/posts`, {
            method: "POST",
            headers: getAuthHeader(),
            body: formData
        });

        if (r.ok) {
            el('title').value = '';
            el('content').value = '';
            el('postImage').value = '';
            msg.textContent = "";
            closeModal();
            fetchPosts();
        } else {
            const err = await r.json();
            msg.textContent = "UPLOAD ERROR: " + (err.message || r.status);
        }
    } catch (e) { msg.textContent = "CONNECTION FAILURE"; }
}

window.deletePostDirectly = async function(event, postId) {
    if (event) event.stopPropagation();
    if (!confirm("Permanently delete this post?")) return;

    try {
        const r = await fetch(`${API_BASE}/posts/${postId}`, {
            method: 'DELETE',
            headers: getAuthHeader()
        });
        if (r.ok || r.status === 204) {
            fetchPosts();
        } else {
            alert("Delete failed. You may not be the owner.");
        }
    } catch (e) { console.error("DEL_POST_ERR", e); }
};

window.likePostDirectly = async function(event, postId) {
    if (event) event.stopPropagation();
    try {
        const r = await fetch(`${API_BASE}/posts/${postId}/like`, {
            method: 'POST',
            headers: getAuthHeader()
        });
        if (r.ok) fetchPosts(); 
    } catch (e) { console.error("LIKE_ERROR", e); }
};

// ---------------- COMMENT ACTIONS ----------------

async function fetchComments(postId) {
    const container = el('modalCommentsList');
    const currentUser = getLoggedInUsername();
    container.innerHTML = '<p class="label-text">Decrypting Signals...</p>';

    try {
        const r = await fetch(`${API_BASE}/posts/${postId}/comments`, { headers: getAuthHeader() });
        const comments = await r.json();
        container.innerHTML = comments.length ? '' : '<p class="cyber-para" style="opacity:0.5;">No Comments</p>';

        comments.forEach(c => {
            const isOwner = currentUser === c.authorUsername;
            const div = document.createElement('div');
            div.className = 'comment-item';
            div.innerHTML = `
                <div style="flex:1">
                    <span class="label-text" style="color:var(--accent); font-size:0.6rem;">@${c.authorUsername}</span>
                    <p class="cyber-para" style="font-size:0.85rem; margin:5px 0;">${escapeHtml(c.content)}</p>
                </div>
                ${isOwner ? `<button onclick="window.deleteComment('${postId}', '${c.id}')" class="btn-delete-small">Delete?</button>` : ''}
            `;
            container.appendChild(div);
        });
    } catch (e) { 
        container.innerHTML = '<p class="label-text" style="color:red;">LINK FAILURE.</p>'; 
    }
}

window.deleteComment = async function(postId, commentId) {
    if (!confirm("Delete this comment?")) return;
    try {
        const r = await fetch(`${API_BASE}/posts/comments/${commentId}`, { 
            method: 'DELETE', 
            headers: getAuthHeader() 
        });
        if (r.ok || r.status === 204) {
            fetchComments(postId); 
            fetchPosts(); 
        }
    } catch (e) { console.error("DEL_COMMENT_ERR", e); }
};

// ---------------- RENDERING ----------------
function renderCarousel(posts) {

const carousel = el('mainCarousel');

if (!carousel) return;

carousel.innerHTML = '';

carousel.style.setProperty('--item-count', posts.length || 1);



posts.forEach((post, i) => {

const item = document.createElement('div');

item.className = 'carousel-item';

item.style.setProperty('--i', i);

item.innerHTML = `

<div class="card">

<div class="card-image-wrapper">

${post.imageBase64 ? `<img src="${post.imageBase64}" class="box-img">` : ``}




</div>

<div class="card-content">

<h2 class="cyber-title">${escapeHtml(post.title)}</h2>

${createInteractionRow(post)}

<div class="post-date-stamp">${formatTime(post.createdAt)}</div>

</div>

</div>`;

item.onclick = (e) => {

if(!e.target.closest('button')) openPostModal(post);

};

carousel.appendChild(item);

});

}



function renderArchive(posts) {

const list = el('postsList');

if (!list) return;

list.innerHTML = '';

posts.forEach(post => {

const card = document.createElement('article');

card.className = 'archive-card';

card.innerHTML = `

<div class="archive-img-wrapper">

${post.imageBase64 ? `<img src="${post.imageBase64}" class="box-img">` : ``}



</div>

<div class="archive-content">

<h3 class="cyber-title">${escapeHtml(post.title)}</h3>

${createInteractionRow(post)}

<div class="post-date-stamp">${formatTime(post.createdAt)}</div>

</div>`;

card.onclick = (e) => {

if(!e.target.closest('button')) openPostModal(post);

};

list.appendChild(card);

});

}
// ---------------- MODAL HELPERS ----------------

function openPostModal(post) {
    currentViewingPostId = post.id;
    el('modalTitle').textContent = post.title;
    el('modalContent').textContent = post.content;
    el('modalAuthor').textContent = post.authorUsername || 'ANON';
    el('modalDate').textContent = formatTime(post.createdAt);
	if (post.imageBase64) {
	    el('modalImageContainer').style.display = 'block';
	    el('modalImage').src = post.imageBase64;
	} else {
	    el('modalImageContainer').style.display = 'none';
	}

    el('postModal').classList.add('active');
    document.body.style.overflow = "hidden";
    fetchComments(post.id);
}

window.openPostModalById = async function(id) {
    try {
        const r = await fetch(`${API_BASE}/posts/${id}`, { headers: getAuthHeader() });
        const post = await r.json();
        openPostModal(post);
    } catch (e) { console.error("MODAL_FETCH_ERR", id); }
};

function closeModal() {
    el('postModal').classList.remove('active');
    el('createArea').classList.remove('active');
    document.body.style.overflow = "";
}

function handleLogout() {
    localStorage.removeItem('accessToken');
    window.location = '/index.html';
}

// ---------------- EVENT LISTENERS ----------------

document.addEventListener('DOMContentLoaded', () => {
    fetchPosts();
    el('btnSubmitPost').onclick = submitPost;
    
    el('modalCommentSubmit').onclick = async () => {
        const input = el('modalCommentInput');
        const content = input.value.trim();
        if (!content || !currentViewingPostId) return;
        
        try {
            const r = await fetch(`${API_BASE}/posts/${currentViewingPostId}/comments`, {
                method: 'POST',
                headers: { ...getAuthHeader(), 'Content-Type': 'application/json' },
                body: JSON.stringify({ content })
            });
            
            if (r.ok) {
                input.value = '';
                fetchComments(currentViewingPostId);
                fetchPosts(); // Sync main page counts
            } else {
                const err = await r.json();
                alert("Comment failed: " + err.message);
            }
        } catch (e) { console.error("COMMENT_POST_ERR", e); }
    };

    el('btnNewPost').onclick = (e) => { e.preventDefault(); el('createArea').classList.add('active'); };
    el('btnCancelPost').onclick = closeModal;
    el('closeModal').onclick = closeModal;
    el('btnLogout').onclick = handleLogout;
    
    // Backdrop click to close
    document.querySelectorAll('.post-modal-backdrop').forEach(b => {
        b.onclick = closeModal;
    });
});