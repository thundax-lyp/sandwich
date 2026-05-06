import { Navigate, RouterProvider, createBrowserRouter } from "react-router-dom";
import { AdminLayout } from "../layouts/AdminLayout";
import { DashboardPage } from "../pages/dashboard/DashboardPage";
import { DepartmentPage } from "../pages/system/DepartmentPage";
import { DictionaryPage } from "../pages/system/DictionaryPage";
import { MenuPage } from "../pages/system/MenuPage";
import { RolePage } from "../pages/system/RolePage";
import { SystemLogPage } from "../pages/system/SystemLogPage";
import { UserPage } from "../pages/system/UserPage";
import { StorageObjectPage } from "../pages/storage/StorageObjectPage";

const router = createBrowserRouter([
    {
        path: "/",
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
]);

export function AppRouter() {
    return <RouterProvider router={router} />;
}
