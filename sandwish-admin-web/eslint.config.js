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
        "hook-file-path": {
            create(context) {
                const isHookFilePath = (normalizedFilePath) => {
                    return /\/hooks\/use-[a-z0-9]+(?:-[a-z0-9]+)*\.ts$/.test(normalizedFilePath);
                };

                const isHookName = (name) => /^use[A-Z]/.test(name);

                const reportHookOutsideHookFile = (node, name, normalizedFilePath) => {
                    if (!isHookName(name) || isHookFilePath(normalizedFilePath)) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_PATH_HOOK_FILE: useXxx hook methods must live in hooks/use-<name>.ts."
                    });
                };

                return {
                    Program(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");
                        const fileName = path.basename(filePath);
                        const isHookFile = /^use-[a-z0-9]+(?:-[a-z0-9]+)*\.tsx?$/.test(fileName);

                        if (normalizedFilePath.includes("/hooks/")) {
                            if (!/^use-[a-z0-9]+(?:-[a-z0-9]+)*\.ts$/.test(fileName)) {
                                context.report({
                                    node,
                                    message:
                                        "ADMIN_WEB_PATH_HOOK_FILE: hook files must be named hooks/use-<name>.ts."
                                });
                            }
                            return;
                        }

                        if (isHookFile) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_PATH_HOOK_FILE: use-*.ts hook files must live in a hooks/ directory."
                            });
                        }
                    },
                    FunctionDeclaration(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");
                        const name = node.id?.name;
                        if (name) {
                            reportHookOutsideHookFile(node.id, name, normalizedFilePath);
                        }
                    },
                    VariableDeclarator(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");

                        if (node.id.type === "Identifier") {
                            reportHookOutsideHookFile(node.id, node.id.name, normalizedFilePath);
                        }
                    },
                    ExportNamedDeclaration(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");

                        node.specifiers.forEach((specifier) => {
                            if (specifier.exported?.type === "Identifier") {
                                reportHookOutsideHookFile(
                                    specifier.exported,
                                    specifier.exported.name,
                                    normalizedFilePath
                                );
                            }
                        });
                    }
                };
            }
        },
        "sandwish-component-name": {
            create(context) {
                const isSandwishName = (name) => /^Sandwish[A-Z]/.test(name);

                const reportSandwishNameOutsideSharedComponents = (
                    node,
                    name,
                    normalizedFilePath
                ) => {
                    if (!isSandwishName(name) || normalizedFilePath.includes("/src/components/")) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_SANDWISH_COMPONENT: Sandwish* names may only be defined in src/components/."
                    });
                };

                const checkNamedNode = (node) => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    const name = node.id?.name;
                    if (name) {
                        reportSandwishNameOutsideSharedComponents(
                            node.id,
                            name,
                            normalizedFilePath
                        );
                    }
                };

                return {
                    ClassDeclaration: checkNamedNode,
                    FunctionDeclaration: checkNamedNode,
                    TSEnumDeclaration: checkNamedNode,
                    TSInterfaceDeclaration: checkNamedNode,
                    TSTypeAliasDeclaration: checkNamedNode,
                    VariableDeclarator(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");

                        if (node.id.type === "Identifier") {
                            reportSandwishNameOutsideSharedComponents(
                                node.id,
                                node.id.name,
                                normalizedFilePath
                            );
                        }
                    },
                    ExportNamedDeclaration(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");

                        node.specifiers.forEach((specifier) => {
                            if (specifier.exported?.type === "Identifier") {
                                reportSandwishNameOutsideSharedComponents(
                                    specifier.exported,
                                    specifier.exported.name,
                                    normalizedFilePath
                                );
                            }
                        });
                    }
                };
            }
        },
        "shared-component-css-local": {
            create(context) {
                return {
                    ImportDeclaration(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");
                        const importPath = node.source.value;

                        if (
                            typeof importPath !== "string" ||
                            !normalizedFilePath.includes("/src/components/") ||
                            !importPath.startsWith("../") ||
                            !importPath.endsWith(".css")
                        ) {
                            return;
                        }

                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_LAYER_SHARED_COMPONENT_CSS_LOCAL: shared components must import CSS from their own directory."
                        });
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
        "page-class-name-prefix": {
            create(context) {
                const readPageDomain = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    const match = normalizedFilePath.match(
                        /\/src\/pages\/[^/]+\/([^/]+)\/\1-page\.tsx$/
                    );
                    return match?.[1] ?? "";
                };

                const reportInvalidClassName = (node, className, pageDomain) => {
                    if (
                        className.startsWith(`${pageDomain}-`) ||
                        className.startsWith("sandwish-")
                    ) {
                        return;
                    }

                    context.report({
                        node,
                        message: `ADMIN_WEB_NAME_PAGE_CLASS_PREFIX: page className "${className}" must start with "${pageDomain}-" or "sandwish-".`
                    });
                };

                const checkClassNameText = (node, text, pageDomain) => {
                    text.split(/\s+/)
                        .filter(Boolean)
                        .forEach((className) => {
                            reportInvalidClassName(node, className, pageDomain);
                        });
                };

                return {
                    JSXAttribute(node) {
                        const pageDomain = readPageDomain();
                        if (!pageDomain || node.name.name !== "className" || !node.value) {
                            return;
                        }

                        if (node.value.type === "Literal" && typeof node.value.value === "string") {
                            checkClassNameText(node.value, node.value.value, pageDomain);
                            return;
                        }

                        if (
                            node.value.type !== "JSXExpressionContainer" ||
                            node.value.expression.type !== "TemplateLiteral"
                        ) {
                            return;
                        }

                        node.value.expression.quasis.forEach((quasi) => {
                            checkClassNameText(quasi, quasi.value.cooked ?? "", pageDomain);
                        });
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
        "page-no-external-service": {
            create(context) {
                const readPageDomainRoot = (normalizedFilePath) => {
                    const match = normalizedFilePath.match(/\/src\/pages\/[^/]+\/[^/]+\//);
                    return match?.[0];
                };

                const resolveImportPath = (filePath, importPath) => {
                    if (importPath.startsWith("@/")) {
                        return `/src/${importPath.slice(2)}`;
                    }
                    if (!importPath.startsWith(".")) {
                        return importPath;
                    }
                    return path
                        .resolve(path.dirname(filePath), importPath)
                        .split(path.sep)
                        .join("/");
                };

                return {
                    ImportDeclaration(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");
                        const importPath = node.source.value;
                        const pageDomainRoot = readPageDomainRoot(normalizedFilePath);

                        if (
                            !pageDomainRoot ||
                            typeof importPath !== "string" ||
                            !normalizedFilePath.includes("/src/pages/")
                        ) {
                            return;
                        }

                        const resolvedImportPath = resolveImportPath(filePath, importPath);
                        const normalizedPageDomainRoot = readPageDomainRoot(resolvedImportPath);
                        if (
                            !resolvedImportPath.includes("/src/pages/") ||
                            !resolvedImportPath.endsWith("-service")
                        ) {
                            return;
                        }

                        if (normalizedPageDomainRoot !== pageDomainRoot) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_LAYER_PAGE_NO_EXTERNAL_SERVICE: page domains must not import services from other page domains."
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
        "post-helper-service-only": {
            create(context) {
                return {
                    ImportDeclaration(node) {
                        const importPath = node.source.value;
                        if (
                            importPath !== "@/api/http" &&
                            importPath !== "../api/http" &&
                            importPath !== "./api/http"
                        ) {
                            return;
                        }

                        const importsPostHelper = node.specifiers.some((specifier) => {
                            return (
                                specifier.type === "ImportSpecifier" &&
                                (specifier.imported.name === "postJson" ||
                                    specifier.imported.name === "postFormData")
                            );
                        });
                        if (!importsPostHelper) {
                            return;
                        }

                        const fileName = path.basename(context.physicalFilename);
                        if (fileName.endsWith("-service.ts")) {
                            return;
                        }

                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_LAYER_POST_HELPER_SERVICE_ONLY / ADMIN_WEB_LAYER_QUERY_FN_FROM_SERVICE: postJson and postFormData may only be imported by *-service.ts files."
                        });
                    }
                };
            }
        },
        "shared-service-types-only": {
            create(context) {
                const isSharedServiceFile = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    return /\/src\/service\/[^/]+-service\.ts$/.test(normalizedFilePath);
                };

                const isAllowedSharedTypeImport = (importPath) => {
                    return /(?:^|\/)[^/]+-types$/.test(importPath);
                };

                const isSharedServiceInternalImport = (resolvedImportPath) => {
                    return resolvedImportPath.includes("/src/service/");
                };

                const resolveImportPath = (importPath) => {
                    if (!importPath.startsWith(".")) {
                        return importPath;
                    }
                    return path
                        .resolve(path.dirname(context.physicalFilename), importPath)
                        .split(path.sep)
                        .join("/");
                };

                return {
                    ImportDeclaration(node) {
                        if (!isSharedServiceFile() || typeof node.source.value !== "string") {
                            return;
                        }

                        const importPath = node.source.value;
                        const resolvedImportPath = resolveImportPath(importPath);
                        if (
                            importPath.startsWith("@/pages/") ||
                            resolvedImportPath.includes("/src/pages/")
                        ) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_LAYER_SHARED_SERVICE_TYPES_ONLY: shared services must not import page files."
                            });
                            return;
                        }

                        if (
                            (importPath.startsWith("@/service/") ||
                                isSharedServiceInternalImport(resolvedImportPath)) &&
                            !/(?:^|\/)[^/]+-service$/.test(importPath) &&
                            !/(?:^|\/)[^/]+-service$/.test(resolvedImportPath) &&
                            !isAllowedSharedTypeImport(importPath)
                        ) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_LAYER_SHARED_SERVICE_TYPES_ONLY: shared service types must be imported from *-types.ts boundaries."
                            });
                        }
                    }
                };
            }
        },
        "component-index-export-only": {
            create(context) {
                return {
                    Program(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");
                        if (
                            !normalizedFilePath.includes("/src/components/") ||
                            !normalizedFilePath.endsWith("/index.ts")
                        ) {
                            return;
                        }

                        node.body.forEach((statement) => {
                            if (
                                statement.type === "ExportNamedDeclaration" ||
                                statement.type === "ExportAllDeclaration"
                            ) {
                                return;
                            }

                            context.report({
                                node: statement,
                                message:
                                    "ADMIN_WEB_LAYER_COMPONENT_INDEX_EXPORT_ONLY: component index.ts files may contain export declarations only."
                            });
                        });
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
        },
        "api-contract-type-location": {
            create(context) {
                const isApiContractName = (name) => /(?:Request|Response)$/.test(name);

                const isAllowedFile = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    return (
                        normalizedFilePath.endsWith("-service.ts") ||
                        normalizedFilePath.includes("/src/api/")
                    );
                };

                const reportInvalidApiContractType = (node, name) => {
                    if (!isApiContractName(name) || isAllowedFile()) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_API_CONTRACT_TYPE_LOCATION: XxxRequest/XxxResponse types may only be defined in *-service.ts or src/api/."
                    });
                };

                return {
                    TSInterfaceDeclaration(node) {
                        reportInvalidApiContractType(node.id, node.id.name);
                    },
                    TSTypeAliasDeclaration(node) {
                        reportInvalidApiContractType(node.id, node.id.name);
                    }
                };
            }
        },
        "service-input-type-location": {
            create(context) {
                const isServiceInputName = (name) => /(?:Query|Command)$/.test(name);

                const isAllowedFile = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    return (
                        normalizedFilePath.endsWith("-service.ts") ||
                        normalizedFilePath.endsWith("/src/types/page.ts")
                    );
                };

                const reportInvalidServiceInputType = (node, name) => {
                    if (!isServiceInputName(name) || isAllowedFile()) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_SERVICE_INPUT_TYPE_LOCATION: XxxQuery/XxxCommand types may only be defined in *-service.ts."
                    });
                };

                return {
                    TSInterfaceDeclaration(node) {
                        reportInvalidServiceInputType(node.id, node.id.name);
                    },
                    TSTypeAliasDeclaration(node) {
                        reportInvalidServiceInputType(node.id, node.id.name);
                    }
                };
            }
        },
        "business-data-type-location": {
            create(context) {
                const isBusinessDataName = (name) => /(?:Record|Node)$/.test(name);

                const isAllowedFile = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    return (
                        /\/src\/pages\/[^/]+\/([^/]+)\/\1-types\.ts$/.test(normalizedFilePath) ||
                        /\/src\/service\/[^/]+-types\.ts$/.test(normalizedFilePath)
                    );
                };

                const reportInvalidBusinessDataType = (node, name) => {
                    if (!isBusinessDataName(name) || isAllowedFile()) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_BUSINESS_DATA_TYPE_LOCATION: XxxRecord/XxxNode types may only be defined in a clear *-types.ts boundary."
                    });
                };

                return {
                    TSInterfaceDeclaration(node) {
                        reportInvalidBusinessDataType(node.id, node.id.name);
                    },
                    TSTypeAliasDeclaration(node) {
                        reportInvalidBusinessDataType(node.id, node.id.name);
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
                            disallow: { to: { type: ["page", "page-component", "page-service"] } },
                            message:
                                "ADMIN_WEB_LAYER_SHARED_COMPONENT_NO_PAGE: shared components must not import pages."
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
            "local/component-index-export-only": "error",
            "local/business-data-type-location": "error",
            "local/api-contract-type-location": "error",
            "local/e2e-spec-file-path": "error",
            "local/kebab-case-file-name": "error",
            "local/page-component-no-external-page": "error",
            "local/page-component-single-export": "error",
            "local/page-class-name-prefix": "error",
            "local/page-no-external-service": "error",
            "local/page-no-parent-relative-import": "error",
            "local/page-style-file": "error",
            "local/post-helper-service-only": "error",
            "local/sandwish-component-name": "error",
            "local/service-method-verb-prefix": "error",
            "local/service-input-type-location": "error",
            "local/shared-service-types-only": "error",
            "local/shared-component-css-local": "error",
            "local/hook-file-path": "error",
            "@typescript-eslint/naming-convention": [
                "error",
                {
                    selector: "variableLike",
                    format: ["camelCase", "PascalCase", "UPPER_CASE"],
                    leadingUnderscore: "allow"
                },
                {
                    selector: "method",
                    format: ["camelCase"],
                    leadingUnderscore: "allow"
                },
                {
                    selector: "typeLike",
                    format: ["PascalCase"]
                }
            ],
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
