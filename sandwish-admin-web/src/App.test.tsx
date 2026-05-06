import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import App from "./App";

describe("App", () => {
    beforeEach(() => {
        localStorage.clear();
        window.history.pushState({}, "", "/");
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("renders the admin dashboard route", async () => {
        localStorage.setItem("sandwish.admin.accessToken", "test-token");

        render(<App />);

        expect(
            await screen.findByRole("heading", { name: "Dashboard 已就绪" })
        ).toBeInTheDocument();
        expect(screen.getByText("系统管理")).toBeInTheDocument();
    });

    it("logs out and returns to the login route", async () => {
        localStorage.setItem("sandwish.admin.accessToken", "test-token");
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            new Response(JSON.stringify({ code: 0, message: "success", data: true }), {
                headers: { "Content-Type": "application/json" },
                status: 200
            })
        );

        render(<App />);

        await userEvent.click(await screen.findByRole("button", { name: /退出登录/ }));

        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/admin-api/api/auth/logout",
            expect.objectContaining({
                body: JSON.stringify({ token: "test-token" }),
                method: "POST"
            })
        );
        expect(localStorage.getItem("sandwish.admin.accessToken")).toBeNull();
        expect(await screen.findByRole("heading", { name: "登录" })).toBeInTheDocument();
    });
});
