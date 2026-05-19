import js from "@eslint/js";
import prettier from "eslint-config-prettier";
import boundaries from "eslint-plugin-boundaries";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import globals from "globals";
import fs from "node:fs";
import path from "node:path";
import tseslint from "typescript-eslint";

const SERVICE_METHOD_VERBS = [
    "page",
    "list",
    "get",
    "add",
    "create",
    "remove",
    "change",
    "sort",
    "move",
    "upload",
    "download",
    "reset",
    "login",
    "logout",
    "refresh",
    "load",
    "save"
];

const localRules = {
    rules: {
        "kebab-case-file-name": {
            create(context) {
                return {
                    Program(node) {
                        const fileName = path.basename(context.physicalFilename);
                        const extension = path.extname(fileName);
                        const name = fileName.slice(0, -extension.length);

                        if (
                            !/^[a-z0-9]+(?:-[a-z0-9]+)*(?:\.[a-z0-9]+(?:-[a-z0-9]+)*)*$/.test(name)
                        ) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_NAME_FILE_KEBAB_CASE: frontend source file names must use kebab-case."
                            });
                        }
                    }
                };
            }
        },
        "e2e-spec-file-path": {
            create(context) {
                return {
                    Program(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");

                        if (
                            !normalizedFilePath.includes("/e2e/") ||
                            !normalizedFilePath.endsWith(".spec.ts")
                        ) {
                            return;
                        }

                        const isLayoutSpec = /\/e2e\/layout\/[^/]+\.spec\.ts$/.test(
                            normalizedFilePath
                        );
                        const isPageSpec = /\/e2e\/[^/]+\/([^/]+)\/\1\.spec\.ts$/.test(
                            normalizedFilePath
                        );

                        if (!isLayoutSpec && !isPageSpec) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_PATH_E2E_PAGE_SPEC / ADMIN_WEB_PATH_E2E_LAYOUT_SPEC: e2e specs must live in e2e/<module>/<domain>/<domain>.spec.ts or e2e/layout/*.spec.ts."
                            });
                        }
                    }
                };
            }
        },
        "page-style-file": {
            create(context) {
                return {
                    Program(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");
                        const fileName = path.basename(filePath);

                        if (
                            !normalizedFilePath.includes("/src/pages/") ||
                            !fileName.endsWith("-page.tsx")
                        ) {
                            return;
                        }

                        const styleFileName = fileName.replace(/\.tsx$/, ".css");
                        const styleFilePath = path.join(path.dirname(filePath), styleFileName);
                        const requiredImport = `./${styleFileName}`;
                        const hasStyleImport = node.body.some((statement) => {
                            return (
                                statement.type === "ImportDeclaration" &&
                                statement.source.value === requiredImport
                            );
                        });

                        if (!fs.existsSync(styleFilePath)) {
                            context.report({
                                node,
                                message: `ADMIN_WEB_NAME_PAGE_STYLE_FILE: page ${fileName} must have sibling style file ${styleFileName}.`
                            });
                            return;
                        }

                        if (!hasStyleImport) {
                            context.report({
                                node,
                                message: `ADMIN_WEB_NAME_PAGE_STYLE_FILE: page ${fileName} must explicitly import "${requiredImport}".`
                            });
                        }
                    }
                };
            }
        },
        "page-no-parent-relative-import": {
            create(context) {
                return {
                    ImportDeclaration(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");
                        const importPath = node.source.value;

                        if (
                            !normalizedFilePath.includes("/src/pages/") ||
                            !normalizedFilePath.endsWith("-page.tsx") ||
                            typeof importPath !== "string" ||
                            !importPath.startsWith("../")
                        ) {
                            return;
                        }

                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_LAYER_PAGE_NO_PARENT_RELATIVE_IMPORT: page files must use ./ for same page-domain imports and @/ for cross-domain or shared imports."
                        });
                    }
                };
            }
        },
        "page-component-no-external-page": {
            create(context) {
                const readPageDomainRoot = (normalizedFilePath) => {
                    const match = normalizedFilePath.match(/\/src\/pages\/[^/]+\/[^/]+\//);
                    return match?.[0];
                };

                return {
                    ImportDeclaration(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");
                        const importPath = node.source.value;
                        const pageDomainRoot = readPageDomainRoot(normalizedFilePath);

                        if (
                            !pageDomainRoot ||
                            !normalizedFilePath.includes(`${pageDomainRoot}components/`) ||
                            typeof importPath !== "string"
                        ) {
                            return;
                        }

                        if (importPath.startsWith("@/pages/")) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_LAYER_PAGE_COMPONENT_NO_EXTERNAL_PAGE: page components must not import from other page domains."
                            });
                            return;
                        }

                        if (!importPath.startsWith(".")) {
                            return;
                        }

                        const resolvedImportPath = path
                            .resolve(path.dirname(filePath), importPath)
                            .split(path.sep)
                            .join("/");

                        if (!resolvedImportPath.includes(pageDomainRoot)) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_LAYER_PAGE_COMPONENT_NO_EXTERNAL_PAGE: page components may only use relative imports inside their own page domain."
                            });
                        }
                    }
                };
            }
        },
        "page-component-single-export": {
            create(context) {
                const isPascalCase = (name) => /^[A-Z][A-Za-z0-9]*$/.test(name);

                const readExportedName = (specifier) => {
                    if (specifier.exported?.type === "Identifier") {
                        return specifier.exported.name;
                    }
                    if (specifier.local?.type === "Identifier") {
                        return specifier.local.name;
                    }
                    return "";
                };

                return {
                    Program(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");

                        if (
                            !normalizedFilePath.includes("/src/pages/") ||
                            !normalizedFilePath.includes("/components/") ||
                            !normalizedFilePath.endsWith(".tsx")
                        ) {
                            return;
                        }

                        const exportedComponents = [];
                        node.body.forEach((statement) => {
                            if (statement.type !== "ExportNamedDeclaration") {
                                return;
                            }

                            if (statement.declaration?.type === "VariableDeclaration") {
                                statement.declaration.declarations.forEach((declaration) => {
                                    if (
                                        declaration.id.type === "Identifier" &&
                                        isPascalCase(declaration.id.name)
                                    ) {
                                        exportedComponents.push(declaration.id.name);
                                    }
                                });
                            }

                            if (
                                statement.declaration?.type === "FunctionDeclaration" &&
                                statement.declaration.id &&
                                isPascalCase(statement.declaration.id.name)
                            ) {
                                exportedComponents.push(statement.declaration.id.name);
                            }

                            statement.specifiers.forEach((specifier) => {
                                const exportedName = readExportedName(specifier);
                                if (isPascalCase(exportedName)) {
                                    exportedComponents.push(exportedName);
                                }
                            });
                        });

                        if (exportedComponents.length > 1) {
                            context.report({
                                node,
                                message: `ADMIN_WEB_COMPONENT_SINGLE_EXPORT: page component files may export only one PascalCase component; move sibling components to separate files. Found ${exportedComponents.join(", ")}.`
                            });
                        }
                    }
                };
            }
        },
        "service-method-verb-prefix": {
            create(context) {
                const startsWithServiceVerb = (name) => {
                    return SERVICE_METHOD_VERBS.some((verb) => {
                        return (
                            name === verb ||
                            name.startsWith(`${verb}${name.charAt(verb.length).toUpperCase()}`)
                        );
                    });
                };

                const reportInvalidServiceMethod = (node, name) => {
                    if (startsWithServiceVerb(name)) {
                        return;
                    }

                    context.report({
                        node,
                        message: `ADMIN_WEB_NAME_SERVICE_METHOD: service method "${name}" must start with one of ${SERVICE_METHOD_VERBS.join(", ")}.`
                    });
                };

                return {
                    ExportNamedDeclaration(node) {
                        const filePath = context.physicalFilename;
                        if (!filePath.endsWith("-service.ts")) {
                            return;
                        }

                        if (node.declaration?.type === "VariableDeclaration") {
                            node.declaration.declarations.forEach((declaration) => {
                                if (declaration.id.type === "Identifier") {
                                    reportInvalidServiceMethod(declaration.id, declaration.id.name);
                                }
                            });
                        }

                        node.specifiers.forEach((specifier) => {
                            if (specifier.exported?.type === "Identifier") {
                                reportInvalidServiceMethod(
                                    specifier.exported,
                                    specifier.exported.name
                                );
                            }
                        });
                    }
                };
            }
        }
    }
};

const frontendRestrictedSyntax = [
    {
        selector: "FunctionDeclaration",
        message:
            "ADMIN_WEB_NAME_FUNCTION_ARROW: use arrow functions by default for frontend methods."
    },
    {
        selector: "ConditionalExpression > ConditionalExpression",
        message: "ADMIN_WEB_NAME_NO_NESTED_TERNARY: nested ternary expressions are forbidden."
    }
];

export default tseslint.config(
    {
        ignores: ["dist", "node_modules"]
    },
    js.configs.recommended,
    ...tseslint.configs.recommended,
    {
        files: ["**/*.{ts,tsx}"],
        languageOptions: {
            ecmaVersion: 2020,
            globals: {
                ...globals.browser,
                ...globals.vitest
            }
        },
        plugins: {
            boundaries,
            local: localRules,
            "react-hooks": reactHooks,
            "react-refresh": reactRefresh
        },
        settings: {
            "import/resolver": {
                typescript: {
                    project: "./tsconfig.json"
                }
            },
            "boundaries/include": ["src/**/*"],
            "boundaries/elements": [
                { type: "api", pattern: "src/api/*", mode: "full" },
                { type: "auth", pattern: "src/auth/*", mode: "full" },
                { type: "layout", pattern: "src/layouts/*", mode: "full" },
                { type: "shared-component", pattern: "src/components/*", mode: "full" },
                { type: "page", pattern: "src/pages/*/*/*-page.tsx", mode: "full" },
                { type: "page-component", pattern: "src/pages/*/*/components/*", mode: "full" },
                { type: "page-service", pattern: "src/pages/*/*/*-service.ts", mode: "full" },
                { type: "query", pattern: "src/query/*", mode: "full" },
                { type: "router", pattern: "src/router/*", mode: "full" },
                { type: "shared-service", pattern: "src/service/*", mode: "full" },
                { type: "types", pattern: "src/types/*", mode: "full" }
            ]
        },
        rules: {
            ...reactHooks.configs.recommended.rules,
            "boundaries/dependencies": [
                "error",
                {
                    default: "allow",
                    rules: [
                        {
                            from: { type: "shared-component" },
                            disallow: { to: { type: "page-service" } },
                            message:
                                "ADMIN_WEB_LAYER_SHARED_COMPONENT_NO_PAGE_SERVICE: shared components must not import page services."
                        },
                        {
                            from: { type: "api" },
                            disallow: {
                                to: {
                                    type: ["page", "page-component", "layout", "shared-component"]
                                }
                            },
                            message:
                                "ADMIN_WEB_LAYER_API_NO_PAGE: api code must not import pages, layouts, or components."
                        },
                        {
                            from: { type: "auth" },
                            disallow: {
                                to: { type: ["page", "page-component", "layout", "page-service"] }
                            },
                            message:
                                "ADMIN_WEB_LAYER_AUTH_NO_PAGE: auth code must not import pages, layouts, or page services."
                        }
                    ]
                }
            ],
            "local/e2e-spec-file-path": "error",
            "local/kebab-case-file-name": "error",
            "local/page-component-no-external-page": "error",
            "local/page-component-single-export": "error",
            "local/page-no-parent-relative-import": "error",
            "local/page-style-file": "error",
            "local/service-method-verb-prefix": "error",
            "no-restricted-imports": [
                "error",
                {
                    patterns: [
                        {
                            regex: "^\\.\\./\\.\\./",
                            message:
                                "ADMIN_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT: use @/ for imports crossing two or more directories."
                        }
                    ]
                }
            ],
            "no-restricted-syntax": ["error", ...frontendRestrictedSyntax],
            "react-refresh/only-export-components": ["warn", { allowConstantExport: true }]
        }
    },
    {
        files: ["src/**/*.{ts,tsx}"],
        ignores: ["src/api/http.ts"],
        rules: {
            "no-restricted-syntax": [
                "error",
                ...frontendRestrictedSyntax,
                {
                    selector: "CallExpression[callee.name='fetch']",
                    message:
                        "ADMIN_WEB_LAYER_FETCH_ONLY_HTTP: only src/api/http.ts may call fetch directly."
                }
            ]
        }
    },
    prettier
);
