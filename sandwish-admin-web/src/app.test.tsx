import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import App from "./app";
import { clearPermissions, hasPermission } from "./auth/permission-storage";
import { queryClient } from "./query/query-client";

vi.mock("sm-crypto", () => ({
    sm2: {
        doEncrypt: () => "encrypted-password"
    }
}));

describe("App", () => {
    beforeEach(() => {
        localStorage.clear();
        clearPermissions();
        queryClient.clear();
        window.history.pushState({}, "", "/");
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("redirects protected routes to login without a token", async () => {
        render(<App />);

        expect(await screen.findByRole("heading", { name: "登录" })).toBeInTheDocument();
    });

    it("renders the admin dashboard route", async () => {
        localStorage.setItem("sandwish.admin.accessToken", "test-token");
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.endsWith("/sys/current-user/info")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: 0,
                            message: "success",
                            data: {
                                id: "user-1",
                                loginName: "developer",
                                name: "Developer"
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/menus")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: 0,
                            message: "success",
                            data: [
                                { id: "menu-system", name: "系统管理" },
                                {
                                    id: "menu-user",
                                    parentId: "menu-system",
                                    name: "用户管理",
                                    url: "/system/users"
                                }
                            ]
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/perms")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: 0,
                            message: "success",
                            data: {
                                perms: ["sys:user:view", "sys:user:edit"]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(
                    JSON.stringify({
                        code: 404,
                        message: "not found"
                    }),
                    {
                        headers: { "Content-Type": "application/json" },
                        status: 404
                    }
                )
            );
        });

        render(<App />);

        expect(
            await screen.findByRole("heading", { name: "Dashboard 已就绪" })
        ).toBeInTheDocument();
        expect(await screen.findByText("Developer")).toBeInTheDocument();
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/admin-api/api/sys/current-user/info",
            expect.objectContaining({
                headers: expect.objectContaining({
                    "Access-Token": "test-token"
                }),
                method: "POST"
            })
        );
        expect(screen.getByText("系统管理")).toBeInTheDocument();
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/admin-api/api/sys/current-user/menus",
            expect.objectContaining({
                headers: expect.objectContaining({
                    "Access-Token": "test-token"
                }),
                method: "POST"
            })
        );
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/admin-api/api/sys/current-user/perms",
            expect.objectContaining({
                headers: expect.objectContaining({
                    "Access-Token": "test-token"
                }),
                method: "POST"
            })
        );
        await waitFor(() => expect(hasPermission("sys:user:view")).toBe(true));
        expect(hasPermission("sys:role:edit")).toBe(false);
    });

    it("loads permissions as part of successful login", async () => {
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            if (url.endsWith("/auth/form")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: 0,
                            message: "success",
                            data: {
                                loginToken: "login-form-token",
                                refreshToken: "refresh-token",
                                expireSeconds: 300,
                                publicKey: "public-key"
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/auth/login")) {
                expect(init).toEqual(
                    expect.objectContaining({
                        body: JSON.stringify({
                            loginToken: "login-form-token",
                            userName: "developer",
                            password: "encrypted-password",
                            captcha: "1234"
                        })
                    })
                );

                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: 0,
                            message: "success",
                            data: {
                                token: "login-access-token",
                                refreshToken: "login-refresh-token"
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/perms")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: 0,
                            message: "success",
                            data: {
                                perms: ["sys:user:view", "sys:user:edit"]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/info")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: 0,
                            message: "success",
                            data: {
                                id: "user-1",
                                loginName: "developer",
                                name: "Developer"
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/menus")) {
                return Promise.resolve(
                    new Response(JSON.stringify({ code: 0, message: "success", data: [] }), {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    })
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: 404, message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        render(<App />);

        await userEvent.type(await screen.findByPlaceholderText("请输入后台账号"), "developer");
        await userEvent.type(screen.getByPlaceholderText("请输入密码"), "sandwich");
        await userEvent.type(screen.getByPlaceholderText("验证码"), "1234");
        await userEvent.click(screen.getByRole("button", { name: /登\s*录/ }));

        expect(
            await screen.findByRole("heading", { name: "Dashboard 已就绪" })
        ).toBeInTheDocument();
        expect(localStorage.getItem("sandwish.admin.accessToken")).toBe("login-access-token");
        await waitFor(() => expect(hasPermission("sys:user:view")).toBe(true));
        expect(hasPermission("sys:role:view")).toBe(false);
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/admin-api/api/sys/current-user/perms",
            expect.objectContaining({
                headers: expect.objectContaining({
                    "Access-Token": "login-access-token"
                }),
                method: "POST"
            })
        );
    });

    it("logs out and returns to the login route", async () => {
        localStorage.setItem("sandwish.admin.accessToken", "test-token");
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.endsWith("/sys/current-user/info")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: 0,
                            message: "success",
                            data: {
                                id: "user-1",
                                loginName: "developer",
                                name: "Developer"
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/menus")) {
                return Promise.resolve(
                    new Response(JSON.stringify({ code: 0, message: "success", data: [] }), {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    })
                );
            }

            if (url.endsWith("/sys/current-user/perms")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: 0,
                            message: "success",
                            data: {
                                perms: ["sys:user:view"]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: 0, message: "success", data: true }), {
                    headers: { "Content-Type": "application/json" },
                    status: 200
                })
            );
        });

        render(<App />);

        await userEvent.click(await screen.findByRole("button", { name: /退出登录/ }));

        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/admin-api/api/auth/logout",
            expect.objectContaining({
                body: JSON.stringify({ token: "test-token" }),
                headers: expect.objectContaining({
                    "Access-Token": "test-token"
                }),
                method: "POST"
            })
        );
        expect(localStorage.getItem("sandwish.admin.accessToken")).toBeNull();
        expect(await screen.findByRole("heading", { name: "登录" })).toBeInTheDocument();
    });

    it("clears stale tokens when protected menu loading is unauthorized", async () => {
        localStorage.setItem("sandwish.admin.accessToken", "stale-token");
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            new Response(JSON.stringify({ code: 401, message: "未授权用户" }), {
                headers: { "Content-Type": "application/json" },
                status: 200
            })
        );

        render(<App />);

        expect(await screen.findByRole("heading", { name: "登录" })).toBeInTheDocument();
        expect(localStorage.getItem("sandwish.admin.accessToken")).toBeNull();
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/admin-api/api/sys/current-user/info",
            expect.objectContaining({
                headers: expect.objectContaining({
                    "Access-Token": "stale-token"
                }),
                method: "POST"
            })
        );
    });
});
