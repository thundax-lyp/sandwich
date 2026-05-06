import { Navigate, RouterProvider, createBrowserRouter } from "react-router-dom";
import { AdminLayout } from "../layouts/admin-layout";
import { DashboardPage } from "../pages/dashboard/dashboard-page";
import { LoginPage } from "../pages/auth/login-page";
import { DepartmentPage } from "../pages/system/department-page";
import { DictionaryPage } from "../pages/system/dictionary-page";
import { MenuPage } from "../pages/system/menu-page";
import { RolePage } from "../pages/system/role-page";
import { SystemLogPage } from "../pages/system/system-log-page";
import { UserPage } from "../pages/system/user-page";
import { StorageObjectPage } from "../pages/storage/storage-object-page";
import { ProtectedRoute } from "./protected-route";

const router = createBrowserRouter([
    {
        path: "/login",
        element: <LoginPage />
    },
    {
        path: "/",
        element: <ProtectedRoute />,
        children: [
            {
                element: <AdminLayout />,
                children: [
                    {
                        index: true,
                        element: <Navigate to="/dashboard" replace />
                    },
                    {
                        path: "dashboard",
                        element: <DashboardPage />
                    },
                    {
                        path: "system/users",
                        element: <UserPage />
                    },
                    {
                        path: "system/departments",
                        element: <DepartmentPage />
                    },
                    {
                        path: "system/roles",
                        element: <RolePage />
                    },
                    {
                        path: "system/menus",
                        element: <MenuPage />
                    },
                    {
                        path: "system/dictionaries",
                        element: <DictionaryPage />
                    },
                    {
                        path: "system/logs",
                        element: <SystemLogPage />
                    },
                    {
                        path: "storage/objects",
                        element: <StorageObjectPage />
                    }
                ]
            }
        ]
    }
]);

export function AppRouter() {
    return <RouterProvider router={router} />;
}
