import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClientProvider } from "@tanstack/react-query";
import App from "./app";
import { postJson } from "./api/http";
import { clearPermissions, hasPermission } from "./auth/permission-storage";
import { DepartmentPage } from "./pages/system/department/department-page";
import { DictionaryPage } from "./pages/system/dictionary/dictionary-page";
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
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                id: "1",
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
                            code: "COMMON-00000",
                            message: "success",
                            data: [
                                { id: "10", name: "系统管理", displayParams: "{\"icon\":\"system\"}" },
                                {
                                    id: "11",
                                    parentId: "10",
                                    name: "用户管理",
                                    icon: "users",
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
                            code: "COMMON-00000",
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
                        code: "COMMON-00004",
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
            await screen.findByRole("heading", { name: "仪表盘已就绪" })
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
        expect(await screen.findByText("系统管理")).toBeInTheDocument();
        expect(document.querySelector(".anticon-safety-certificate")).toBeInTheDocument();
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
        const loginExpireAt = Date.now() + 5 * 60 * 1000;
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            if (url.endsWith("/auth/session/pre-auth-session")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                loginToken: "login-form-token",
                                refreshToken: "refresh-token",
                                expiredAt: 1778513052155,
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

            if (url.endsWith("/auth/session/login")) {
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
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                token: "login-access-token",
                                refreshToken: "login-refresh-token",
                                expireAt: loginExpireAt
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
                            code: "COMMON-00000",
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
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                id: "1",
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
                    new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data: [] }), {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    })
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
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
            await screen.findByRole("heading", { name: "仪表盘已就绪" })
        ).toBeInTheDocument();
        expect(localStorage.getItem("sandwish.admin.accessToken")).toBe("login-access-token");
        expect(localStorage.getItem("sandwish.admin.refreshToken")).toBe("login-refresh-token");
        expect(localStorage.getItem("sandwish.admin.accessTokenExpireAt")).toBe(String(loginExpireAt));
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
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                id: "1",
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
                    new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data: [] }), {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    })
                );
            }

            if (url.endsWith("/sys/current-user/perms")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
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
                new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data: true }), {
                    headers: { "Content-Type": "application/json" },
                    status: 200
                })
            );
        });

        render(<App />);

        await userEvent.click(await screen.findByRole("button", { name: /Developer/ }));
        await userEvent.click(await screen.findByRole("menuitem", { name: /退出登录/ }));

        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/admin-api/api/auth/session/logout",
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

    it("renders the department list page", async () => {
        localStorage.setItem("sandwish.admin.accessToken", "test-token");
        localStorage.setItem(
            "sandwish.admin.permissions",
            JSON.stringify(["sys:department:view", "sys:department:edit"])
        );
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.endsWith("/sys/department/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: [
                                {
                                    id: "1",
                                    name: "总部",
                                    shortName: "HQ",
                                    namePath: "总部",
                                    remarks: "核心组织"
                                },
                                {
                                    id: "2",
                                    parentId: "1",
                                    name: "技术部",
                                    shortName: "Tech",
                                    namePath: "总部/技术部"
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

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        render(
            <QueryClientProvider client={queryClient}>
                <DepartmentPage />
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "部门管理" })).toBeInTheDocument();
        expect((await screen.findAllByText("总部")).length).toBeGreaterThan(0);
        expect(await screen.findByText("技术部")).toBeInTheDocument();
        expect(screen.getByText("核心组织")).toBeInTheDocument();
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/admin-api/api/sys/department/list",
            expect.objectContaining({
                body: JSON.stringify({}),
                headers: expect.objectContaining({
                    "Access-Token": "test-token"
                }),
                method: "POST"
            })
        );
    });

    it("renders and filters the dictionary page", async () => {
        localStorage.setItem("sandwish.admin.accessToken", "test-token");
        localStorage.setItem(
            "sandwish.admin.permissions",
            JSON.stringify(["sys:dict:view", "sys:dict:edit"])
        );
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.endsWith("/sys/dict/page")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                pageNo: 1,
                                pageSize: 10,
                                totalPage: 1,
                                totalCount: 2,
                                records: [
                                    {
                                        id: "1",
                                        type: "user_status",
                                        label: "启用",
                                        value: "ENABLED",
                                        remarks: "允许登录"
                                    },
                                    {
                                        id: "2",
                                        type: "user_status",
                                        label: "停用",
                                        value: "DISABLED"
                                    }
                                ]
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
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        render(
            <QueryClientProvider client={queryClient}>
                <DictionaryPage />
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "字典管理" })).toBeInTheDocument();
        expect(await screen.findByText("启用")).toBeInTheDocument();
        expect(screen.getByText("DISABLED")).toBeInTheDocument();

        await userEvent.type(screen.getByPlaceholderText("字典类型"), "user_status");
        await userEvent.click(screen.getByRole("button", { name: /查\s*询/ }));

        await waitFor(() =>
            expect(globalThis.fetch).toHaveBeenLastCalledWith(
                "/admin-api/api/sys/dict/page",
                expect.objectContaining({
                    body: JSON.stringify({
                        type: "user_status",
                        pageNo: 1,
                        pageSize: 10
                    }),
                    headers: expect.objectContaining({
                        "Access-Token": "test-token"
                    }),
                    method: "POST"
                })
            )
        );
    });

    it("clears stale tokens when protected menu loading is unauthorized", async () => {
        localStorage.setItem("sandwish.admin.accessToken", "stale-token");
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            new Response(JSON.stringify({ code: "COMMON-00002", message: "未授权用户" }), {
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

    it("refreshes the access token before expireAt is within 60 seconds", async () => {
        localStorage.setItem("sandwish.admin.accessToken", "expiring-token");
        localStorage.setItem("sandwish.admin.refreshToken", "refresh-token");
        localStorage.setItem("sandwish.admin.accessTokenExpireAt", String(Date.now() + 30 * 1000));
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            if (url.endsWith("/auth/session/token/refresh")) {
                expect(init).toEqual(
                    expect.objectContaining({
                        body: JSON.stringify({
                            clientId: "admin-api",
                            refreshToken: "refresh-token"
                        })
                    })
                );

                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                token: "refreshed-access-token",
                                refreshToken: "rotated-refresh-token",
                                expireAt: 1778514052155
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
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                id: "1",
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
                    new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data: [] }), {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    })
                );
            }

            if (url.endsWith("/sys/current-user/perms")) {
                return Promise.resolve(
                    new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data: { perms: [] } }), {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    })
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        render(<App />);

        expect(
            await screen.findByRole("heading", { name: "仪表盘已就绪" })
        ).toBeInTheDocument();
        expect(localStorage.getItem("sandwish.admin.accessToken")).toBe("refreshed-access-token");
        expect(localStorage.getItem("sandwish.admin.refreshToken")).toBe("rotated-refresh-token");
        expect(localStorage.getItem("sandwish.admin.accessTokenExpireAt")).toBe("1778514052155");
        await waitFor(() =>
            expect(globalThis.fetch).toHaveBeenCalledWith(
                "/admin-api/api/sys/current-user/info",
                expect.objectContaining({
                    headers: expect.objectContaining({
                        "Access-Token": "refreshed-access-token"
                    }),
                    method: "POST"
                })
            )
        );
    });

    it("waits for an in-flight token refresh before sending another request", async () => {
        localStorage.setItem("sandwish.admin.accessToken", "old-token");
        localStorage.setItem("sandwish.admin.refreshToken", "refresh-token");
        localStorage.setItem("sandwish.admin.accessTokenExpireAt", String(Date.now() + 5 * 60 * 1000));
        let resolveRefresh: (response: Response) => void = () => undefined;
        const refreshResponse = new Promise<Response>((resolve) => {
            resolveRefresh = resolve;
        });
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            if (url.endsWith("/sys/current-user/info")) {
                return Promise.resolve(
                    new Response(JSON.stringify({ code: "COMMON-00002", message: "未授权用户" }), {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    })
                );
            }

            if (url.endsWith("/auth/session/token/refresh")) {
                return refreshResponse;
            }

            if (url.endsWith("/sys/current-user/menus")) {
                expect(init).toEqual(
                    expect.objectContaining({
                        headers: expect.objectContaining({
                            "Access-Token": "new-token"
                        })
                    })
                );
                return Promise.resolve(
                    new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data: [] }), {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    })
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data: {} }), {
                    headers: { "Content-Type": "application/json" },
                    status: 200
                })
            );
        });

        const infoRequest = postJson("/sys/current-user/info").catch(() => null);
        await waitFor(() =>
            expect(globalThis.fetch).toHaveBeenCalledWith(
                "/admin-api/api/auth/session/token/refresh",
                expect.any(Object)
            )
        );
        const menuRequest = postJson("/sys/current-user/menus");

        expect(globalThis.fetch).not.toHaveBeenCalledWith(
            "/admin-api/api/sys/current-user/menus",
            expect.any(Object)
        );
        resolveRefresh(
            new Response(
                JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        token: "new-token",
                        refreshToken: "new-refresh-token",
                        expireAt: Date.now() + 5 * 60 * 1000
                    }
                }),
                {
                    headers: { "Content-Type": "application/json" },
                    status: 200
                }
            )
        );

        await expect(menuRequest).resolves.toEqual([]);
        await infoRequest;
    });
});
