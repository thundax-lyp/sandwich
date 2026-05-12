import { expect, test } from "@playwright/test";
import type { Page } from "@playwright/test";

const expectNoPageHorizontalOverflow = async (page: Page) => {
    await expect
        .poll(async () =>
            page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)
        )
        .toBe(true);

    const metrics = await page.evaluate(() => ({
        scrollWidth: document.documentElement.scrollWidth,
        viewportWidth: window.innerWidth
    }));
    expect(
        metrics.scrollWidth,
        `scrollWidth=${metrics.scrollWidth}, viewport=${metrics.viewportWidth}`
    ).toBeLessThanOrEqual(metrics.viewportWidth);
};

test.describe("admin layout", () => {
    test.beforeEach(async ({ page }) => {
        await page.route("**/admin-api/api/sys/current-user/info", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        id: "1",
                        loginName: "developer",
                        name: "Developer"
                    }
                })
            });
        });
        await page.route("**/admin-api/api/sys/current-user/menus", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: [
                        {
                            id: "10",
                            name: "仪表盘",
                            url: "/dashboard",
                            displayParams: '{"icon":"dashboard"}'
                        },
                        {
                            id: "11",
                            name: "系统管理",
                            displayParams: '{"icon":"system"}'
                        },
                        {
                            id: "12",
                            parentId: "11",
                            name: "用户管理",
                            url: "/system/users",
                            displayParams: '{"icon":"users"}'
                        }
                    ]
                })
            });
        });
        await page.route("**/admin-api/api/sys/current-user/perms", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        perms: ["sys:user:view"]
                    }
                })
            });
        });
        await page.route("**/admin-api/api/sys/dict/page", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        pageNo: 1,
                        pageSize: 10,
                        totalCount: 1,
                        records: [
                            {
                                id: "100",
                                type: "system_status",
                                label: "启用",
                                value: "ENABLED",
                                remarks: "默认状态"
                            }
                        ]
                    }
                })
            });
        });
        await page.route("**/admin-api/api/auth/session/token/refresh", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        token: "test-token",
                        refreshToken: "refresh-token",
                        expireAt: Date.now() + 3600 * 1000
                    }
                })
            });
        });

        await page.addInitScript(() => {
            window.localStorage.setItem("sandwish.admin.accessToken", "test-token");
            window.localStorage.setItem("sandwish.admin.refreshToken", "refresh-token");
            window.localStorage.setItem(
                "sandwish.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
        });
    });

    test("opens and closes the mobile sidebar from the topbar", async ({ page }) => {
        await page.setViewportSize({ width: 390, height: 844 });
        await page.goto("/dashboard");

        const sidebar = page.locator(".sidebar");
        await expect(page.getByRole("heading", { name: "仪表盘已就绪" })).toBeVisible();
        await expect(sidebar).not.toHaveClass(/sidebar-mobile-open/);

        await page.getByLabel("展开菜单").click();
        await expect(sidebar).toHaveClass(/sidebar-mobile-open/);
        await expect(page.getByLabel("关闭菜单")).toBeVisible();

        await page.getByLabel("关闭菜单").click();
        await expect(sidebar).not.toHaveClass(/sidebar-mobile-open/);
    });

    test("closes the mobile sidebar after selecting a menu item", async ({ page }) => {
        await page.setViewportSize({ width: 390, height: 844 });
        await page.goto("/dashboard");

        const sidebar = page.locator(".sidebar");
        await page.getByLabel("展开菜单").click();
        await expect(sidebar).toHaveClass(/sidebar-mobile-open/);

        await page.getByRole("menuitem", { name: "仪表盘" }).click();

        await expect(sidebar).not.toHaveClass(/sidebar-mobile-open/);
    });

    test("keeps the workspace content within the expanded and collapsed desktop widths", async ({
        page
    }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/system/dictionaries");

        const main = page.locator(".admin-main");
        const workspaceContent = page.locator(".dictionary-page");
        await expect(page.getByRole("heading", { name: "字典管理" })).toBeVisible();
        await expect(main).toHaveAttribute("data-sidebar-state", "expanded");
        await expect(main).toHaveCSS("width", "996px");
        await expect(workspaceContent).toHaveCSS("width", "940px");
        await expectNoPageHorizontalOverflow(page);

        await page.getByLabel("收起菜单").click();
        await expect(main).toHaveAttribute("data-sidebar-state", "collapsed");
        await expect(main).toHaveCSS("width", "1156px");
        await expect(workspaceContent).toHaveCSS("width", "1100px");
        await expectNoPageHorizontalOverflow(page);

        await page.getByLabel("展开菜单").click();
        await expect(main).toHaveAttribute("data-sidebar-state", "expanded");
        await expect(main).toHaveCSS("width", "996px");
        await expect(workspaceContent).toHaveCSS("width", "940px");
        await expectNoPageHorizontalOverflow(page);
    });

    test("uses the padded viewport width when the mobile menu is closed or open", async ({
        page
    }) => {
        await page.setViewportSize({ width: 390, height: 844 });
        await page.goto("/system/dictionaries");

        const main = page.locator(".admin-main");
        const workspaceContent = page.locator(".dictionary-page");
        await expect(page.getByRole("heading", { name: "字典管理" })).toBeVisible();
        await expect(main).toHaveAttribute("data-sidebar-state", "closed");
        await expect(main).toHaveCSS("width", "370px");
        await expect(workspaceContent).toHaveCSS("width", "338px");
        await expectNoPageHorizontalOverflow(page);

        await page.getByLabel("展开菜单").click();
        await expect(main).toHaveAttribute("data-sidebar-state", "open");
        await expect(main).toHaveCSS("width", "370px");
        await expect(workspaceContent).toHaveCSS("width", "338px");
        await expectNoPageHorizontalOverflow(page);
    });
});
