package com.github.thundax.modules.auth.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.modules.auth.controller.response.MemberLoginStatusResponse;
import org.junit.Test;

public class MemberLoginInterfaceAssemblerTest {

    @Test
    public void shouldBuildAnonymousLoginStatusResponse() {
        MemberLoginStatusResponse response = MemberLoginInterfaceAssembler.toLoginStatusResponse(null);

        assertFalse(response.getLoggedIn());
        assertNull(response.getMemberId());
        assertNull(response.getMessage());
    }

    @Test
    public void shouldBuildLoggedInStatusResponse() {
        MemberLoginStatusResponse response = MemberLoginInterfaceAssembler.toLoginStatusResponse("member-1");

        assertTrue(response.getLoggedIn());
        assertEquals("member-1", response.getMemberId());
        assertNull(response.getMessage());
    }

    @Test
    public void shouldBuildLoginFailureResponse() {
        MemberLoginStatusResponse response = MemberLoginInterfaceAssembler.toLoginFailureResponse("用户名或密码错误。");

        assertFalse(response.getLoggedIn());
        assertNull(response.getMemberId());
        assertEquals("用户名或密码错误。", response.getMessage());
    }

    @Test
    public void shouldBuildLogoutResponse() {
        MemberLoginStatusResponse response = MemberLoginInterfaceAssembler.toLogoutResponse();

        assertFalse(response.getLoggedIn());
        assertNull(response.getMemberId());
        assertEquals("退出成功", response.getMessage());
    }
}
