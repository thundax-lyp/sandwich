import fs from "node:fs";
import path from "node:path";
import process from "node:process";

const SOURCE_ROOT = path.resolve("src");
const COMPONENT_ROOT = path.join(SOURCE_ROOT, "components");
const CLASS_NAME_PATTERN = /\.((?:sandwish-[a-z0-9]+)(?:-[a-z0-9]+)*)/g;

const listFiles = (directory, predicate) => {
    if (!fs.existsSync(directory)) {
        return [];
    }

    return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
        const entryPath = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            return listFiles(entryPath, predicate);
        }
        return predicate(entryPath) ? [entryPath] : [];
    });
};

const componentNames = fs
    .readdirSync(COMPONENT_ROOT, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && entry.name.startsWith("sandwish-"))
    .map((entry) => entry.name)
    .sort((left, right) => right.length - left.length);

const readComponentName = (className) => {
    return componentNames.find((componentName) => {
        return className === componentName || className.startsWith(`${componentName}-`);
    });
};

const cssFiles = listFiles(SOURCE_ROOT, (filePath) => filePath.endsWith(".css"));
const violations = [];

cssFiles.forEach((filePath) => {
    const content = fs.readFileSync(filePath, "utf8");
    const normalizedFilePath = filePath.split(path.sep).join("/");

    for (const match of content.matchAll(CLASS_NAME_PATTERN)) {
        const className = match[1];
        const componentName = readComponentName(className);
        if (!componentName) {
            continue;
        }

        const expectedFilePath = path
            .join(COMPONENT_ROOT, componentName, `${componentName}.css`)
            .split(path.sep)
            .join("/");

        if (normalizedFilePath !== expectedFilePath) {
            violations.push(
                `${normalizedFilePath}: .${className} must live in ${expectedFilePath}`
            );
        }
    }
});

if (violations.length > 0) {
    process.stderr.write(
        [
            "ADMIN_WEB_STYLE_COMPONENT_CLASS_LOCATION: component-prefixed CSS classes must live in their component CSS file.",
            ...violations
        ].join("\n") + "\n"
    );
    process.exit(1);
}
