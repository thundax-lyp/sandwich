import { render, screen } from "@testing-library/react";
import App from "./App";

describe("App", () => {
    it("renders the admin dashboard route", async () => {
        render(<App />);

        expect(
            await screen.findByRole("heading", { name: "Dashboard 已就绪" })
        ).toBeInTheDocument();
        expect(screen.getByText("系统管理")).toBeInTheDocument();
    });
});
