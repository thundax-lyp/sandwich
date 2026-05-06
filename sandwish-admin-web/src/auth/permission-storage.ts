let permissionSet = new Set<string>();

export function replacePermissions(permissions: string[]) {
    permissionSet = new Set(permissions);
}

export function clearPermissions() {
    permissionSet = new Set();
}

export function getPermissions() {
    return Array.from(permissionSet);
}

export function hasPermission(permission: string) {
    return permissionSet.has(permission);
}
