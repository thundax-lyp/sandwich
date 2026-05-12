import { expect, test } from "@playwright/test";

test.describe("admin layout", () => {
    test.beforeEach(async ({ page }) => {
        await page.route("**/admin-api/api/sys/current-user/info", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        id: 1,
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
                            id: 10,
                            name: "系统管理",
                            displayParams: "{\"icon\":\"system\"}"
                        },
                        {
                            id: 11,
                            parentId: 10,
                            name: "用户管理",
                            url: "/system/users",
                            displayParams: "{\"icon\":\"users\"}"
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

        await page.addInitScript(() => {
            window.localStorage.setItem("sandwish.admin.accessToken", "test-token");
            window.localStorage.setItem("sandwish.admin.refreshToken", "refresh-token");
            window.localStorage.setItem("sandwish.admin.accessTokenExpireAt", String(Date.now() + 3600 * 1000));
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
});
