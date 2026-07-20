package com.bullboard.service;

import com.bullboard.repository.ArticleRepository;
import com.bullboard.repository.MemberRepository;
import com.bullboard.repository.CommentRepository;
import com.bullboard.domain.Member;
import com.bullboard.dto.LoginRequest;
import com.bullboard.dto.MemberRequest;
import com.bullboard.dto.MemberUpdateRequest;
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

        Member foundMember = memberRepository.findByEmail(normalizedEmail);
        if (foundMember != null && !foundMember.getId().equals(id)) {
            throw new ApiException(HttpStatus.CONFLICT);
        }

        Member foundNickname = memberRepository.findByNickname(normalizedNickname);
        if (foundNickname != null && !foundNickname.getId().equals(id)) {
            throw new ApiException(HttpStatus.CONFLICT);
        }

        String introduction = request.getIntroduction() == null
                ? "" : request.getIntroduction().trim();
        member.updateProfile(
                normalizedNickname,
                normalizedEmail,
                introduction
        );

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
