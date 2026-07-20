package com.bullboard.service;

import com.bullboard.repository.ArticleRepository;
import com.bullboard.repository.MemberRepository;
import com.bullboard.repository.CommentRepository;
import com.bullboard.domain.Member;
import com.bullboard.dto.LoginRequest;
import com.bullboard.dto.AccountUpdateRequest;
import com.bullboard.dto.MemberRequest;
import com.bullboard.dto.MemberUpdateRequest;
import com.bullboard.dto.ProfileUpdateRequest;
import com.bullboard.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository,
                         ArticleRepository articleRepository,
                         CommentRepository commentRepository,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Member createMember(MemberRequest request) {
        validatePasswordConfirm(request);

        Member foundMember = memberRepository.findByEmail(request.getEmail());
        if (foundMember != null) {
            throw new ApiException(HttpStatus.CONFLICT);
        }

        Member foundNickname = memberRepository.findByNickname(request.getNickname());
        if (foundNickname != null) {
            throw new ApiException(HttpStatus.CONFLICT);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Member member = new Member(request.getNickname(), request.getEmail(), encodedPassword);
        return memberRepository.save(member);
    }

    @Transactional
    public Member login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail());
        if (member == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED);
        }

        if (!matchesPassword(request.getPassword(), member.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED);
        }

        if (!isBcryptPassword(member.getPassword())) {
            member.changePassword(passwordEncoder.encode(request.getPassword()));
        }

        return member;
    }

    public Member getMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        return member;
    }

    @Transactional
    public Member updateMember(Long id, MemberUpdateRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));

        String normalizedEmail = request.getEmail().trim();
        String normalizedNickname = request.getNickname().trim();

        validateEmailAvailable(id, normalizedEmail);
        validateNicknameAvailable(id, normalizedNickname);

        String introduction = request.getIntroduction() == null
                ? "" : request.getIntroduction().trim();
        member.updateProfile(normalizedNickname, introduction);
        member.changeEmail(normalizedEmail);

        String newPassword = request.getPassword() == null
                ? "" : request.getPassword();
        if (!newPassword.isBlank()) {
            if (!newPassword.equals(request.getPasswordConfirm())) {
                throw new ApiException(HttpStatus.BAD_REQUEST);
            }
            member.changePassword(passwordEncoder.encode(newPassword));
        }
        return member;
    }

    @Transactional
    public Member updateProfile(Long id, ProfileUpdateRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        String nickname = request.getNickname().trim();
        validateNicknameAvailable(id, nickname);
        String introduction = request.getIntroduction() == null
                ? "" : request.getIntroduction().trim();
        member.updateProfile(nickname, introduction);
        return member;
    }

    @Transactional
    public Member updateAccount(Long id, AccountUpdateRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        String email = request.getEmail().trim();
        validateEmailAvailable(id, email);
        member.changeEmail(email);

        String password = request.getPassword() == null ? "" : request.getPassword();
        String passwordConfirm = request.getPasswordConfirm() == null
                ? "" : request.getPasswordConfirm();
        if (!password.equals(passwordConfirm)) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        }
        if (!password.isBlank()) {
            member.changePassword(passwordEncoder.encode(password));
        }
        return member;
    }

    public boolean isNicknameAvailable(Long id, String nickname) {
        if (nickname == null || nickname.trim().isBlank()
                || nickname.trim().length() > 50) {
            return false;
        }
        Member found = memberRepository.findByNickname(nickname.trim());
        return found == null || found.getId().equals(id);
    }

    private void validateNicknameAvailable(Long id, String nickname) {
        if (!isNicknameAvailable(id, nickname)) {
            throw new ApiException(HttpStatus.CONFLICT);
        }
    }

    private void validateEmailAvailable(Long id, String email) {
        Member found = memberRepository.findByEmail(email);
        if (found != null && !found.getId().equals(id)) {
            throw new ApiException(HttpStatus.CONFLICT);
        }
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (isBcryptPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword != null && storedPassword.equals(rawPassword);
    }

    private boolean isBcryptPassword(String password) {
        return password != null && password.matches("^\\$2[aby]\\$.*");
    }

    private void validatePasswordConfirm(MemberRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));

        articleRepository.anonymizeByMemberId(id);
        commentRepository.anonymizeByMemberId(id);
        memberRepository.delete(member);
    }
}
