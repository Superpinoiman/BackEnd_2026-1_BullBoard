const loadingState = document.getElementById('loadingState');
const loginRequired = document.getElementById('loginRequired');
const profileSection = document.getElementById('profileSection');
const profileForm = document.getElementById('profileForm');
const logoutButton = document.getElementById('logoutButton');
const toast = document.getElementById('toast');

function showToast(message) {
    toast.textContent = message;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2600);
}

async function loadProfile() {
    const response = await fetch('/me');
    loadingState.hidden = true;
    if (!response.ok) {
        loginRequired.hidden = false;
        return;
    }

    const member = await response.json();
    document.getElementById('welcomeNickname').textContent = member.nickname;
    document.getElementById('email').value = member.email;
    document.getElementById('nickname').value = member.nickname;
    profileSection.hidden = false;
    logoutButton.hidden = false;
}

profileForm.addEventListener('submit', async event => {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(profileForm));
    const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d\s])\S{8,}$/;
    if (!passwordPattern.test(data.password)) {
        showToast('비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.');
        return;
    }
    if (data.password !== data.passwordConfirm) {
        showToast('비밀번호와 비밀번호 확인이 일치하지 않습니다.');
        return;
    }

    const response = await fetch('/me', {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        showToast(response.status === 409 ? '이미 사용 중인 이메일 또는 닉네임입니다.' : '입력 정보를 다시 확인해 주세요.');
        return;
    }

    const member = await response.json();
    document.getElementById('welcomeNickname').textContent = member.nickname;
    profileForm.elements.password.value = '';
    profileForm.elements.passwordConfirm.value = '';
    showToast('회원 정보가 저장되었습니다.');
});

logoutButton.addEventListener('click', async () => {
    await fetch('/logout', {method: 'POST'});
    location.href = '/';
});

loadProfile();
