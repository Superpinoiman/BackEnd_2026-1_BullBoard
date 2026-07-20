const loadingState = document.getElementById('loadingState');
const loginRequired = document.getElementById('loginRequired');
const memberContent = document.getElementById('memberContent');
const profileForm = document.getElementById('profileForm');
const logoutButton = document.getElementById('logoutButton');
const deleteMemberButton = document.getElementById('deleteMemberButton');
const introduction = document.getElementById('introduction');
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

async function request(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        }
    });
    if (!response.ok) {
        const error = new Error('요청 처리에 실패했습니다.');
        error.status = response.status;
        throw error;
    }
    return response.status === 204 ? null : response.json();
}

async function loadProfile() {
    try {
        const member = await request('/me');
        renderProfile(member);
        loadingState.hidden = true;
        memberContent.hidden = false;
        logoutButton.hidden = false;
        await loadMyArticles(0);
    } catch (error) {
        loadingState.hidden = true;
        if (error.status === 401) {
            loginRequired.hidden = false;
            return;
        }
        showToast('회원 정보를 불러오지 못했습니다.');
    }
}

function renderProfile(member) {
    const intro = member.introduction || '';
    document.getElementById('welcomeNickname').textContent = member.nickname;
    document.getElementById('profileInitial').textContent =
        member.nickname.trim().charAt(0).toUpperCase() || 'B';
    document.getElementById('profileIntroduction').textContent =
        intro || '소개를 작성해 나를 표현해 보세요.';
    document.getElementById('email').value = member.email;
    document.getElementById('nickname').value = member.nickname;
    introduction.value = intro;
    updateIntroductionCount();
}

function updateIntroductionCount() {
    document.getElementById('introductionCount').textContent =
        introduction.value.length;
}

introduction.addEventListener('input', updateIntroductionCount);

profileForm.addEventListener('submit', async event => {
    event.preventDefault();
    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;
    const passwordPattern =
        /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d\s])\S{8,}$/;

    if (password && !passwordPattern.test(password)) {
        showToast('비밀번호는 영문, 숫자, 특수문자를 포함해 8자 이상이어야 합니다.');
        return;
    }
    if (password !== passwordConfirm) {
        showToast('비밀번호와 비밀번호 확인이 일치하지 않습니다.');
        return;
    }

    const body = {
        email: document.getElementById('email').value.trim(),
        nickname: document.getElementById('nickname').value.trim(),
        introduction: introduction.value.trim(),
        password,
        password_confirm: passwordConfirm
    };

    try {
        const member = await request('/me', {
            method: 'PUT',
            body: JSON.stringify(body)
        });
        renderProfile(member);
        document.getElementById('password').value = '';
        document.getElementById('passwordConfirm').value = '';
        showToast('회원정보가 저장되었습니다.');
    } catch (error) {
        if (error.status === 409) {
            showToast('이미 사용 중인 이메일 또는 닉네임입니다.');
            return;
        }
        showToast('입력한 정보를 다시 확인해 주세요.');
    }
});

async function loadMyArticles(page) {
    try {
        const result = await request(`/me/articles?page=${Math.max(page, 0)}&size=10`);
        if (!result.content.length && page > 0 && result.total_pages > 0) {
            await loadMyArticles(result.total_pages - 1);
            return;
        }
        renderMyArticles(result.content);
        renderPagination(result);
    } catch (error) {
        showToast('작성한 글을 불러오지 못했습니다.');
    }
}

function renderMyArticles(articles) {
    const list = document.getElementById('myArticleList');
    list.replaceChildren();
    if (!articles.length) {
        const empty = document.createElement('div');
        empty.className = 'article-empty';
        empty.textContent = '아직 작성한 글이 없습니다.';
        list.append(empty);
        return;
    }

    articles.forEach(article => {
        const link = document.createElement('a');
        link.className = 'my-article';
        link.href = `/posts?boardId=${article.board_id}&articleId=${article.id}`;

        const summary = document.createElement('span');
        const title = document.createElement('span');
        title.className = 'my-article-title';
        title.textContent = article.title;
        const meta = document.createElement('span');
        meta.className = 'my-article-meta';
        meta.textContent = `${article.board_name} · 조회 ${article.view_count}`;
        summary.append(title, meta);

        const date = document.createElement('span');
        date.className = 'my-article-date';
        date.textContent = formatDate(article.created_date);
        link.append(summary, date);
        list.append(link);
    });
}

function renderPagination(pageInfo) {
    const pagination = document.getElementById('myArticlePagination');
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
        button.addEventListener('click', () => loadMyArticles(page));
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

deleteMemberButton.addEventListener('click', async () => {
    const confirmed = confirm(
        '정말 회원탈퇴하시겠습니까?\n작성한 게시글과 댓글은 알 수 없음으로 남습니다.'
    );
    if (!confirmed) {
        return;
    }
    try {
        await request('/me', {method: 'DELETE'});
        location.href = '/';
    } catch (error) {
        showToast('회원탈퇴 처리에 실패했습니다.');
    }
});

logoutButton.addEventListener('click', async () => {
    await fetch('/logout', {method: 'POST'});
    location.href = '/';
});

loadProfile();
