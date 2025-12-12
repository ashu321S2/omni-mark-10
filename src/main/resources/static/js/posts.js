// posts.js - updated frontend (optimistic comment count updates)
const API_BASE = "/api";

function el(id){ return document.getElementById(id); }
function escapeHtml(s){ if (!s) return ""; return String(s).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,">"); }
function formatTime(v){ try { return new Date(v).toLocaleString(); } catch(e){ return v; } }

function currentUsernameFromToken(){
  try {
    const t = localStorage.getItem('accessToken');
    if (!t) return null;
    const p = JSON.parse(atob(t.split('.')[1]));
    return p.sub || p.username || null;
  } catch(e){ return null; }
}

// ---------------- POSTS ----------------
async function fetchPosts(page=0, size=20) {
  const list = el('postsList');
  list.innerHTML = 'Loading posts...';
  const token = localStorage.getItem('accessToken');

  try {
    const r = await fetch(`${API_BASE}/posts?page=${page}&size=${size}`, {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    });
    const txt = await r.text();
    let body; try{ body = JSON.parse(txt); } catch(e){ body = txt; }

    if (!r.ok) {
      if (r.status === 401 || r.status === 403) { localStorage.removeItem('accessToken'); window.location = '/index.html'; return; }
      list.innerHTML = `<p>Failed to load posts: ${r.status}</p>`; return;
    }

    const posts = Array.isArray(body) ? body : (body.content || []);
    renderPosts(posts);
  } catch (e) {
    console.error('fetchPosts error', e);
    el('postsList').innerHTML = 'Network error while loading posts.';
  }
}

function renderPosts(posts) {
  const list = el('postsList');
  list.innerHTML = '';
  if (!posts || posts.length === 0) { list.innerHTML = '<p>No posts yet</p>'; return; }
  const me = currentUsernameFromToken();

  posts.forEach(post => {
    const card = document.createElement('div'); card.className = 'post-card';
    card.innerHTML = `
      <div class="post-meta">
        <div><span class="post-author">${escapeHtml(post.authorUsername || 'Unknown')}</span>
          <span style="color:#999;margin-left:8px">${formatTime(post.createdAt)}</span>
        </div>
        <div class="post-actions">
          <button class="btn-like" data-id="${post.id}">Like (${post.likes||0})</button>
          <button class="btn-comment" data-id="${post.id}">Comments (${post.comments||0})</button>
          <button class="btn-share" data-id="${post.id}">Share</button>
          ${me === post.authorUsername ? `<button class="btn-edit" data-id="${post.id}">Edit</button><button class="btn-delete" data-id="${post.id}">Delete</button>` : ''}
        </div>
      </div>
      <div class="post-content"><h4>${escapeHtml(post.title)}</h4><p>${escapeHtml(post.content)}</p></div>

      <div class="comments-section hidden" id="comments-section-${post.id}">
        <div class="comments-list" id="comments-list-${post.id}">Loading comments...</div>
        <div class="comment-input-row" id="comment-input-row-${post.id}">
          <textarea id="comment-input-${post.id}" placeholder="Write a comment..."></textarea>
          <button id="comment-submit-${post.id}">Comment</button>
        </div>
      </div>
    `;
    list.appendChild(card);

    // handlers
    card.querySelectorAll('.btn-like').forEach(b => b.addEventListener('click', ()=> likePost(post.id)));
    card.querySelectorAll('.btn-comment').forEach(b => b.addEventListener('click', (e) => toggleComments(post.id, e.currentTarget)));
    card.querySelectorAll('.btn-share').forEach(b => b.addEventListener('click', ()=> navigator.clipboard?.writeText(location.origin + '/posts.html#' + post.id).then(()=>alert('Link copied'))));
    card.querySelectorAll('.btn-edit').forEach(b => b.addEventListener('click', ()=> startEditById(post.id)));
    card.querySelectorAll('.btn-delete').forEach(b => b.addEventListener('click', ()=> deletePost(post.id)));

    // comment submit
    const submitBtn = card.querySelector(`#comment-submit-${post.id}`);
    if (submitBtn) {
      submitBtn.addEventListener('click', async () => {
        const textarea = el(`comment-input-${post.id}`);
        const content = (textarea.value || '').trim();
        if (!content) return;
        const added = await postComment(post.id, content);
        if (added) {
          // optimistic UI already handled inside postComment
          textarea.value = '';
        }
      });
    }

    // hide comment input if not authenticated
    if (!me) {
      const inputRow = card.querySelector(`#comment-input-row-${post.id}`);
      if (inputRow) inputRow.style.display = 'none';
    }
  });
}

// ---------------- COMMENTS ----------------
async function toggleComments(postId, btnEl) {
  const section = el(`comments-section-${postId}`);
  if (!section) return;
  const isHidden = section.classList.contains('hidden');
  if (isHidden) {
    section.classList.remove('hidden');
    btnEl && btnEl.classList.add('toggle-on');
    await fetchCommentsAndRender(postId);
  } else {
    section.classList.add('hidden');
    btnEl && btnEl.classList.remove('toggle-on');
  }
}

async function fetchCommentsAndRender(postId) {
  const listEl = el(`comments-list-${postId}`);
  if (!listEl) return;
  listEl.innerHTML = 'Loading comments...';
  const token = localStorage.getItem('accessToken');

  try {
    console.log(`GET /api/posts/${postId}/comments (token present: ${!!token})`);
    const r = await fetch(`${API_BASE}/posts/${postId}/comments`, {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    });
    const txt = await r.text();
    let body; try{ body = JSON.parse(txt); } catch(e){ body = txt; }

    if (!r.ok) {
      const serverMsg = (body && (body.message || body.error)) ? (body.message || body.error) : `Status ${r.status}`;
      listEl.innerHTML = `<p>Unable to load comments: ${serverMsg}</p>`;
      return;
    }

    const comments = Array.isArray(body) ? body : [];
    if (comments.length === 0) { listEl.innerHTML = '<p>No comments yet.</p>'; return; }

    // render comments
    listEl.innerHTML = '';
    const me = currentUsernameFromToken();
    comments.forEach(c => {
      const d = document.createElement('div'); d.className = 'comment';
      const canDelete = me && (me === c.authorUsername);
      d.innerHTML = `
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div><strong>${escapeHtml(c.authorUsername||'Unknown')}</strong>
            <span style="color:#777;margin-left:8px;font-size:12px">${formatTime(c.createdAt)}</span>
          </div>
          <div>${canDelete ? `<button class="comment-del" data-id="${c.id}" data-post="${postId}">Delete</button>` : ''}</div>
        </div>
        <div style="margin-top:6px">${escapeHtml(c.content)}</div>
      `;
      listEl.appendChild(d);

      if (canDelete) {
        d.querySelector('.comment-del').addEventListener('click', async (e) => {
          if (!confirm('Delete this comment?')) return;
          const comId = e.currentTarget.dataset.id;
          await deleteComment(postId, comId);
        });
      }
    });

  } catch (e) {
    console.error('fetchComments error', e);
    listEl.innerHTML = '<p>Network error while loading comments.</p>';
  }
}

async function postComment(postId, content) {
  const token = localStorage.getItem('accessToken');
  if (!token) { alert('Please login'); return null; }

  try {
    const r = await fetch(`${API_BASE}/posts/${postId}/comments`, {
      method:'POST',
      headers:{ 'Content-Type':'application/json', 'Authorization':'Bearer ' + token },
      body: JSON.stringify({ content })
    });

    const txt = await r.text();
    let body; try{ body = JSON.parse(txt); } catch(e){ body = txt; }

    console.log('POST comment ->', r.status, body);

    if (!r.ok) {
      alert('Failed to add comment: ' + (body?.message || body));
      return null;
    }

    // optimistic UI: increment comments badge immediately
    try {
      const commentBtn = document.querySelector(`.btn-comment[data-id="${postId}"]`);
      if (commentBtn) {
        const m = commentBtn.textContent.match(/Comments?\s*\((\d+)\)/i);
        if (m) {
          const n = parseInt(m[1], 10) + 1;
          commentBtn.textContent = `Comments (${n})`;
        }
      }
    } catch (e) { console.warn('Could not update comment badge immediately', e); }

    // refresh comments and posts so server authoritative numbers are shown
    await fetchCommentsAndRender(postId);
    await fetchPosts();
    return body;
  } catch (e) {
    console.error('postComment error', e);
    alert('Network error while posting comment');
    return null;
  }
}

async function deleteComment(postId, commentId) {
  const token = localStorage.getItem('accessToken');
  if (!token) { alert('Please login'); return; }

  try {
    console.log(`DELETE /api/posts/comments/${commentId}`);
    const r = await fetch(`${API_BASE}/posts/comments/${commentId}`, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ' + token }
    });

    const txt = await r.text();
    let body; try{ body = JSON.parse(txt); } catch(e){ body = txt; }
    console.log('DELETE comment ->', r.status, body);

    if (r.status === 204 || r.status === 200) {
      // decrement DOM badge immediately
      try {
        const commentBtn = document.querySelector(`.btn-comment[data-id="${postId}"]`);
        if (commentBtn) {
          const m = commentBtn.textContent.match(/Comments?\s*\((\d+)\)/i);
          if (m) {
            const n = Math.max(0, parseInt(m[1], 10) - 1);
            commentBtn.textContent = `Comments (${n})`;
          }
        }
      } catch (e) { console.warn('Could not decrement badge', e); }

      await fetchCommentsAndRender(postId);
      await fetchPosts();
      return;
    }

    if (r.status === 401) { alert('Not authenticated'); localStorage.removeItem('accessToken'); window.location='/index.html'; return; }
    if (r.status === 403) { alert('Not authorized to delete this comment'); return; }
    if (r.status === 404) { alert('Comment not found'); await fetchCommentsAndRender(postId); return; }

    alert('Failed to delete comment: ' + (body?.message || body));
  } catch (e) {
    console.error('deleteComment error', e);
    alert('Network error while deleting comment');
  }
}

// ---------------- other post actions ----------------
async function submitPost(){
  const title = (el('title')||{}).value.trim();
  const content = (el('content')||{}).value.trim();
  const msg = el('postsMessage');
  if (!title || !content) { if (msg) msg.textContent='Title & content required'; return; }
  const token = localStorage.getItem('accessToken');

  try {
    const r = await fetch(`${API_BASE}/posts`, {
      method:'POST',
      headers:{ 'Content-Type':'application/json', 'Authorization':'Bearer ' + token },
      body: JSON.stringify({ title, content })
    });
    const txt = await r.text();
    let body; try{ body = JSON.parse(txt);}catch(e){ body = txt; }
    if (!r.ok) { if (msg) msg.textContent='Failed to post: ' + (body?.message||r.status); return; }
    if (msg){ msg.style.color='green'; msg.textContent='Posted'; setTimeout(()=>msg.textContent='',1200); }
    el('title').value=''; el('content').value=''; await fetchPosts(); hideCreateArea();
  } catch(e){ console.error('submitPost error', e); if (msg) msg.textContent='Network error'; }
}

async function likePost(id){
  const token = localStorage.getItem('accessToken');
  try {
    const r = await fetch(`${API_BASE}/posts/${id}/like`, { method:'POST', headers:{ 'Authorization':'Bearer '+token }});
    if (!r.ok){ if (r.status===401){ localStorage.removeItem('accessToken'); window.location='/index.html'; } return; }
    await fetchPosts();
  } catch(e){ console.error('like err', e); }
}

async function startEditById(id){
  try {
    const token = localStorage.getItem('accessToken');
    const r = await fetch(`${API_BASE}/posts/${id}`, { headers:{ 'Authorization':'Bearer ' + token }});
    if (!r.ok) { console.error('fetch post failed', r.status); return; }
    const post = await r.json();
    el('title').value = post.title; el('content').value = post.content;
    el('createHeading').textContent = 'Edit Post'; el('btnSubmitPost').textContent = 'Save Changes';
    el('createArea').classList.remove('hidden'); window._editingPostId = id;
  } catch(e){ console.error('startEdit error', e); }
}

async function saveChanges(){
  const id = window._editingPostId;
  if (!id) return;
  const token = localStorage.getItem('accessToken');
  const title = el('title').value.trim();
  const content = el('content').value.trim();
  try {
    const r = await fetch(`${API_BASE}/posts/${id}`, {
      method:'PUT',
      headers:{ 'Content-Type':'application/json', 'Authorization':'Bearer ' + token },
      body: JSON.stringify({ title, content })
    });
    if (!r.ok) { console.error('save failed', r.status); return; }
    window._editingPostId = null; el('btnSubmitPost').textContent = 'Post'; el('createHeading').textContent = 'Create Post';
    el('title').value=''; el('content').value=''; await fetchPosts(); hideCreateArea();
  } catch(e){ console.error('save err', e); }
}

async function deletePost(id){
  if (!confirm('Delete post?')) return;
  const token = localStorage.getItem('accessToken');
  try {
    const r = await fetch(`${API_BASE}/posts/${id}`, { method:'DELETE', headers:{ 'Authorization':'Bearer '+token }});
    if (!r.ok) { console.error('delete failed', r.status); return; }
    await fetchPosts();
  } catch(e){ console.error('delete err', e); }
}

function showCreateArea(){ el('createArea').classList.remove('hidden'); el('title').focus(); }
function hideCreateArea(){ el('createArea').classList.add('hidden'); el('title').value=''; el('content').value=''; el('createHeading').textContent='Create Post'; el('btnSubmitPost').textContent='Post'; window._editingPostId = null; }

// ---------------- bootstrap ----------------
document.addEventListener('DOMContentLoaded', () => {
  const user = (function getUserFromToken(){ try{ const t=localStorage.getItem('accessToken'); if(!t) return null; const p=JSON.parse(atob(t.split('.')[1])); return { username: p.sub||p.username, email: p.email }; } catch(e){ return null; } })();
  if (user) {
    el('profileUsername').textContent = user.username;
    el('profileEmail').textContent = user.email || '';
  }

  el('btnNewPost')?.addEventListener('click', ()=> showCreateArea());
  el('btnCancelPost')?.addEventListener('click', ()=> hideCreateArea());
  el('btnSubmitPost')?.addEventListener('click', async () => {
    if (window._editingPostId) await saveChanges(); else await submitPost();
  });
  el('profileBtn')?.addEventListener('click', ()=> el('profileMenu').classList.toggle('hidden'));
  el('btnLogout')?.addEventListener('click', ()=> { localStorage.removeItem('accessToken'); window.location='/index.html'; });

  fetchPosts();
});
