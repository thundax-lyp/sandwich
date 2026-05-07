package com.github.thundax.modules.member.security;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.encrypt.Md5Helper;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.service.MemberService;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class MemberSpringAuthenticationProvider implements AuthenticationProvider {

    private static final String MEMBER_PERMISSION = "member";

    private final MemberService memberService;
    private final String defaultPassword;

    public MemberSpringAuthenticationProvider(
            MemberService memberService, @Value("${sandwish.default-password:12312321321}") String defaultPassword) {
        this.memberService = memberService;
        this.defaultPassword = defaultPassword;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = String.valueOf(authentication.getPrincipal());
        String password = String.valueOf(authentication.getCredentials());

        Member member = memberService.getById(EntityIdCodec.toDomain(Long.valueOf(username)));
        if (member == null) {
            throw new BadCredentialsException("用户名或密码错误。");
        }
        if (!member.isActive()) {
            throw new DisabledException("会员状态不可用，请联系管理员。");
        }
        if (!Md5Helper.encrypt(defaultPassword).equals(Md5Helper.encrypt(password))) {
            throw new BadCredentialsException("用户名或密码错误。");
        }

        return new UsernamePasswordAuthenticationToken(
                new MemberSpringPrincipal(member),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(MEMBER_PERMISSION)));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
