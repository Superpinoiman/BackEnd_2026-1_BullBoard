const loadingState = document.getElementById('loadingState');
const loginRequired = document.getElementById('loginRequired');
const memberContent = document.getElementById('memberContent');
const profileForm = document.getElementById('profileForm');
const accountForm = document.getElementById('accountForm');
const accountOverlay = document.getElementById('accountOverlay');
const logoutButton = document.getElementById('logoutButton');
const deleteMemberButton = document.getElementById('deleteMemberButton');
const nicknameInput = document.getElementById('nickname');
const nicknameMessage = document.getElementById('nicknameMessage');
const introduction = document.getElementById('introduction');
const toast = document.getElementById('toast');

let currentMember = null;
let verifiedNickname = null;

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
    currentMember = member;
    const intro = member.introduction || '';
    document.getElementById('welcomeNickname').textContent = member.nickname;
    document.getElementById('profileInitial').textContent =
        member.nickname.trim().charAt(0).toUpperCase() || 'B';
    document.getElementById('profileIntroduction').textContent =
        intro || '소개를 작성해 나를 표현해 보세요.';
    nicknameInput.value = member.nickname;
    introduction.value = intro;
    document.getElementById('accountEmail').value = member.email;
    verifiedNickname = member.nickname;
    showNicknameMessage('현재 사용 중인 닉네임입니다.', true);
    updateIntroductionCount();
}

function updateIntroductionCount() {
    document.getElementById('introductionCount').textContent =
        introduction.value.length;
}

function showNicknameMessage(message, available) {
    nicknameMessage.textContent = message;
    nicknameMessage.className =
        `verification-message ${available ? 'available' : 'unavailable'}`;
}

introduction.addEventListener('input', updateIntroductionCount);

nicknameInput.addEventListener('input', () => {
    const nickname = nicknameInput.value.trim();
    if (currentMember && nickname === currentMember.nickname) {
        verifiedNickname = nickname;
        showNicknameMessage('현재 사용 중인 닉네임입니다.', true);
        return;
    }
    verifiedNickname = null;
    nicknameMessage.textContent = '닉네임 중복확인이 필요합니다.';
    nicknameMessage.className = 'verification-message';
});

document.getElementById('nicknameCheckButton').addEventListener('click', async () => {
    const nickname = nicknameInput.value.trim();
    if (!nickname || nickname.length > 50) {
        verifiedNickname = null;
        showNicknameMessage('1자 이상 50자 이하로 입력해 주세요.', false);
        return;
    }
    try {
        const result = await request(
            `/me/nickname-availability?nickname=${encodeURIComponent(nickname)}`
        );
        if (result.available) {
            verifiedNickname = nickname;
            showNicknameMessage('사용할 수 있는 닉네임입니다.', true);
        } else {
            verifiedNickname = null;
            showNicknameMessage('이미 사용 중인 닉네임입니다.', false);
        }
    } catch (error) {
        verifiedNickname = null;
        showToast('닉네임 중복확인에 실패했습니다.');
    }
});

profileForm.addEventListener('submit', async event => {
    event.preventDefault();
    const nickname = nicknameInput.value.trim();
    if (verifiedNickname !== nickname) {
        showToast('닉네임 중복확인을 먼저 해주세요.');
        return;
    }

    try {
        const member = await request('/me/profile', {
            method: 'PUT',
            body: JSON.stringify({
                nickname,
                introduction: introduction.value.trim()
            })
        });
        renderProfile(member);
        showToast('프로필이 저장되었습니다.');
    } catch (error) {
        if (error.status === 409) {
            verifiedNickname = null;
            showNicknameMessage('이미 사용 중인 닉네임입니다.', false);
            showToast('다른 회원이 먼저 사용한 닉네임입니다.');
            return;
        }
        showToast('프로필 저장에 실패했습니다.');
    }
});

function openAccountModal() {
    document.getElementById('accountEmail').value = currentMember.email;
    document.getElementById('accountPassword').value = '';
    document.getElementById('accountPasswordConfirm').value = '';
    accountOverlay.hidden = false;
    document.body.classList.add('modal-open');
    document.getElementById('accountEmail').focus();
}

function closeAccountModal() {
    accountOverlay.hidden = true;
    document.body.classList.remove('modal-open');
}

document.getElementById('accountEditButton').addEventListener('click', openAccountModal);
document.getElementById('accountCloseButton').addEventListener('click', closeAccountModal);
document.getElementById('accountCancelButton').addEventListener('click', closeAccountModal);
accountOverlay.addEventListener('click', event => {
    if (event.target === accountOverlay) {
        closeAccountModal();
    }
});

accountForm.addEventListener('submit', async event => {
    event.preventDefault();
    const password = document.getElementById('accountPassword').value;
    const passwordConfirm =
        document.getElementById('accountPasswordConfirm').value;
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

    try {
        const member = await request('/me/account', {
            method: 'PUT',
            body: JSON.stringify({
                email: document.getElementById('accountEmail').value.trim(),
                password,
                password_confirm: passwordConfirm
            })
        });
        currentMember = member;
        closeAccountModal();
        showToast('회원정보가 저장되었습니다.');
    } catch (error) {
        if (error.status === 409) {
            showToast('이미 사용 중인 이메일입니다.');
            return;
        }
        showToast('회원정보 저장에 실패했습니다.');
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
