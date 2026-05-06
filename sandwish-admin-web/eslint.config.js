import js from "@eslint/js";
import prettier from "eslint-config-prettier";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import globals from "globals";
import path from "node:path";
import tseslint from "typescript-eslint";

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
                                message: "Frontend source file names must use kebab-case."
                            });
                        }
                    }
                };
            }
        }
    }
};

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
            local: localRules,
            "react-hooks": reactHooks,
            "react-refresh": reactRefresh
        },
        rules: {
            ...reactHooks.configs.recommended.rules,
            "local/kebab-case-file-name": "error",
            "no-restricted-syntax": [
                "error",
                {
                    selector: "FunctionDeclaration",
                    message: "Use arrow functions by default for frontend methods."
                }
            ],
            "react-refresh/only-export-components": ["warn", { allowConstantExport: true }]
        }
    },
    prettier
);
