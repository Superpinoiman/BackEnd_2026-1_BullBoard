const backdrop=document.getElementById('modalBackdrop'),toast=document.getElementById('toast');
const showToast=m=>{toast.textContent=m;toast.classList.add('show');setTimeout(()=>toast.classList.remove('show'),2600)};
const openModal=t=>{backdrop.classList.add('visible');document.getElementById('loginModal').classList.toggle('hidden',t!=='login');document.getElementById('signupModal').classList.toggle('hidden',t!=='signup')};
const closeModal=()=>backdrop.classList.remove('visible');
document.querySelectorAll('[data-open]').forEach(b=>b.addEventListener('click',()=>openModal(b.dataset.open)));document.querySelectorAll('[data-close]').forEach(b=>b.addEventListener('click',closeModal));backdrop.addEventListener('click',e=>{if(e.target===backdrop)closeModal()});
async function request(url,data){const res=await fetch(url,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(data)});if(!res.ok)throw new Error(res.status===409?'이미 사용 중인 이메일 또는 닉네임입니다.':'입력 정보를 다시 확인해 주세요.');return res.json()}
function renderAuthenticated(member){document.getElementById('authActions').innerHTML=`<span class="welcome">${member.nickname}님</span><button class="text-button" id="logoutButton">로그아웃</button>`;document.getElementById('logoutButton').addEventListener('click',async()=>{await fetch('/logout',{method:'POST'});location.reload()})}
async function restoreSession(){const response=await fetch('/me');if(response.ok)renderAuthenticated(await response.json())}
document.getElementById('signupForm').addEventListener('submit',async e=>{e.preventDefault();const data=Object.fromEntries(new FormData(e.currentTarget)),passwordPattern=/^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d\s])\S{8,}$/;if(!passwordPattern.test(data.password)){showToast('비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.');return}if(data.password!==data.password_confirm){showToast('비밀번호와 비밀번호 확인이 일치하지 않습니다.');return}try{const member=await request('/members',data);closeModal();renderAuthenticated(member);showToast('회원가입이 완료되었습니다. '+member.nickname+'님 환영합니다!');e.currentTarget.reset()}catch(err){showToast(err.message)}});
document.getElementById('loginForm').addEventListener('submit',async e=>{e.preventDefault();try{const member=await request('/login',Object.fromEntries(new FormData(e.currentTarget)));closeModal();renderAuthenticated(member);showToast('환영합니다, '+member.nickname+'님!')}catch(err){showToast('이메일 또는 비밀번호가 올바르지 않습니다.')}});
restoreSession();

async function loadTrendingArticles(){
    const container=document.getElementById('trendingArticles');
    try{
        const response=await fetch('/articles/trending?size=3');
        if(!response.ok)throw new Error();
        const articles=await response.json();container.replaceChildren();
        if(!articles.length){container.innerHTML='<p class="community-empty">최근 7일간 작성된 게시글이 없습니다.</p>';return}
        articles.forEach((article,index)=>{
            const card=document.createElement('a');card.className='topic-card';card.href=`/posts?boardId=${article.board_id}&articleId=${article.id}`;
            const top=document.createElement('div');top.className='topic-card-top';
            const board=document.createElement('span');board.className='board-badge';board.textContent=article.board_name;
            const rank=document.createElement('span');rank.className='topic-rank';rank.textContent=`TOP ${index+1}`;
            top.append(board,rank);
            const body=document.createElement('div');body.className='topic-card-body';
            if(article.symbol){const tag=document.createElement('span');tag.className='topic-symbol';tag.textContent=`#${article.symbol}`;body.append(tag)}
            const title=document.createElement('h3');title.textContent=article.title;
            body.append(title);
            const footer=document.createElement('footer');
            const author=document.createElement('span');author.className='topic-author';author.textContent=article.author_nickname;
            const likes=document.createElement('span');likes.className='topic-likes';likes.textContent=`♥ ${article.like_count}`;
            const arrow=document.createElement('span');arrow.className='topic-arrow';arrow.textContent='→';
            footer.append(author,likes,arrow);
            card.append(top,body,footer);container.append(card);
        });
    }catch(error){container.innerHTML='<p class="community-empty">인기 게시글을 불러오지 못했습니다.</p>'}
}
loadTrendingArticles();
