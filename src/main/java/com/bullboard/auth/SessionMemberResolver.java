package com.bullboard.auth;

import com.bullboard.constant.SessionConst;
import com.bullboard.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SessionMemberResolver {

    public void login(HttpServletRequest request, Long memberId) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(SessionConst.LOGIN_MEMBER_ID, memberId);
    }

    public Long requireMemberId(HttpServletRequest request) {
        Long memberId = getMemberId(request);
        if (memberId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED);
        }
        return memberId;
    }

    public Long getMemberId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
