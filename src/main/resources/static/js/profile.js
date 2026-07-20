const memberId = Number(location.pathname.split('/').filter(Boolean).pop());
const loadingState = document.getElementById('loadingState');
const notFoundState = document.getElementById('notFoundState');
const profileContent = document.getElementById('profileContent');
const toast = document.getElementById('toast');

function showToast(message) {
    toast.textContent = message;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2600);
}

function formatDate(value) {
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    }).format(new Date(value));
}

async function request(url) {
    const response = await fetch(url);
    if (!response.ok) {
        const error = new Error('요청 처리에 실패했습니다.');
        error.status = response.status;
        throw error;
    }
    return response.json();
}

async function initializeProfile() {
    if (!Number.isInteger(memberId) || memberId < 1) {
        showNotFound();
        return;
    }

    try {
        const profile = await request(`/profiles/${memberId}`);
        document.title = `${profile.nickname} | BullBoard`;
        document.getElementById('profileNickname').textContent = profile.nickname;
        document.getElementById('profileInitial').textContent =
            profile.nickname.trim().charAt(0).toUpperCase() || 'B';
        document.getElementById('profileIntroduction').textContent =
            profile.introduction || '아직 작성한 소개가 없습니다.';
        loadingState.hidden = true;
        profileContent.hidden = false;
        await loadArticles(0);
    } catch (error) {
        if (error.status === 404) {
            showNotFound();
            return;
        }
        loadingState.hidden = true;
        showToast('회원 정보를 불러오지 못했습니다.');
    }
}

function showNotFound() {
    loadingState.hidden = true;
    profileContent.hidden = true;
    notFoundState.hidden = false;
}

async function loadArticles(page) {
    try {
        const result = await request(
            `/profiles/${memberId}/articles?page=${Math.max(page, 0)}&size=10`
        );
        if (!result.content.length && page > 0 && result.total_pages > 0) {
            await loadArticles(result.total_pages - 1);
            return;
        }
        document.getElementById('articleCount').textContent =
            `${result.total_elements}개`;
        renderArticles(result.content);
        renderPagination(result);
    } catch (error) {
        showToast('작성한 게시글을 불러오지 못했습니다.');
    }
}

function renderArticles(articles) {
    const list = document.getElementById('articleList');
    list.replaceChildren();
    if (!articles.length) {
        const empty = document.createElement('div');
        empty.className = 'profile-empty';
        empty.textContent = '작성한 게시글이 없습니다.';
        list.append(empty);
        return;
    }

    articles.forEach(article => {
        const link = document.createElement('a');
        link.className = 'profile-article';
        link.href = `/posts?boardId=${article.board_id}&articleId=${article.id}`;

        const summary = document.createElement('span');
        const title = document.createElement('span');
        title.className = 'article-title';
        title.textContent = article.title;
        const meta = document.createElement('span');
        meta.className = 'article-meta';
        meta.textContent = `${article.board_name} · 조회 ${article.view_count}`;
        summary.append(title, meta);

        const date = document.createElement('span');
        date.className = 'article-date';
        date.textContent = formatDate(article.created_date);
        link.append(summary, date);
        list.append(link);
    });
}

function renderPagination(pageInfo) {
    const pagination = document.getElementById('pagination');
    pagination.replaceChildren();
    if (pageInfo.total_pages <= 1) {
        return;
    }

    const addButton = (label, page, disabled = false, active = false) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = label;
        button.disabled = disabled;
        button.classList.toggle('active', active);
        button.addEventListener('click', () => loadArticles(page));
        pagination.append(button);
    };

    addButton('이전', pageInfo.page - 1, pageInfo.first);
    const start = Math.max(0, Math.min(pageInfo.page - 2, pageInfo.total_pages - 5));
    const end = Math.min(pageInfo.total_pages, start + 5);
    for (let page = start; page < end; page++) {
        addButton(String(page + 1), page, false, page === pageInfo.page);
    }
    addButton('다음', pageInfo.page + 1, pageInfo.last);
}

initializeProfile();
