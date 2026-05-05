package com.github.thundax.common.test.architecture;

import static org.junit.Assert.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class NamingArchitectureRuleSupport {

    private static final String ARCHITECTURE_ROLE_SUFFIXES =
            ".*(Mapper|Converter|Assembler|DAO|Service|Controller|Repository|Facade|Gateway|Adapter|Client|Handler"
                    + "|Processor|Manager|Factory)";
    private static final String GENERIC_HELPER_NAMES = "(List|Object|Data|Common|Base|Generic)Helper";
    private static final Pattern SERVICE_QUERY_SETTER_DECLARATION_PATTERN =
            Pattern.compile("\\bpublic\\s+void\\s+set[A-Z][A-Za-z0-9_]*\\s*\\(");

    private NamingArchitectureRuleSupport() {}

    public static void assertHelperNamesBounded(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (javaClass.getSimpleName().matches(GENERIC_HELPER_NAMES)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue("Helper names must bind a concrete boundary: " + violations, violations.isEmpty());
    }

    public static void assertToolPackagesOutOfArchitectureRoleNames(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isToolPackage(javaClass) && javaClass.getSimpleName().matches(ARCHITECTURE_ROLE_SUFFIXES)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue("Tool packages must not use architecture role suffixes: " + violations, violations.isEmpty());
    }

    public static void assertDaoInterfaceMethodNames(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isDaoInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (!isDaoPortMethodShape(method)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "DAO interface methods should use getById/getByXxx/list/listByIds/page/count/deleteById/batchXxx "
                        + "naming: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertServiceInterfaceMethodNames(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (!isServiceMethodShape(method)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "Service methods should use getById/getByXxx/list/listByIds/page/count/deleteById/batchXxx "
                        + "for generic access and business verbs for workflows: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertServiceQueryObjectsUnderServiceQueryPackage(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isServiceQueryObject(javaClass) && !isInServiceQueryPackage(javaClass)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "Service query objects must be placed under "
                        + "com.github.thundax.modules.{module}.service.query: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertServiceQueryObjectsDeclareNoSetters(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isServiceQuerySource)
                    .filter(NamingArchitectureRuleSupport::containsServiceQuerySetter)
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        }

        assertTrue(
                "Service query objects must only define query fields; request-to-query conversion belongs in "
                        + "InterfaceAssembler, so service query source must not declare setXxx methods: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertInterfaceAssemblerPublicMethodsStatic(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!javaClass.getSimpleName().endsWith("InterfaceAssembler")) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (method.getModifiers().contains(JavaModifier.PUBLIC)
                        && !method.getModifiers().contains(JavaModifier.STATIC)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue("InterfaceAssembler public methods must be static: " + violations, violations.isEmpty());
    }

    public static void assertInterfaceAssemblersDoNotWrapEntityIdConversion(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!javaClass.getSimpleName().endsWith("InterfaceAssembler")) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (method.getModifiers().contains(JavaModifier.PUBLIC) && "toEntityId".equals(method.getName())) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue("InterfaceAssembler must not wrap EntityId conversion: " + violations, violations.isEmpty());
    }

    private static boolean isToolPackage(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return packageName.contains(".utils")
                || packageName.contains(".collection")
                || packageName.contains(".web.request")
                || packageName.contains(".web.response");
    }

    private static boolean isDaoInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Dao")
                && javaClass.getPackageName().contains(".dao");
    }

    private static boolean isServiceInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Service")
                && javaClass.getPackageName().contains(".service");
    }

    private static boolean isDaoPortMethodShape(JavaMethod method) {
        String name = method.getName();
        if (isNonStandardIdsListName(name) || name.startsWith("find")) {
            return false;
        }
        return name.equals("count")
                || name.equals("list")
                || name.equals("page")
                || name.equals("deleteAll")
                || name.startsWith("getBy")
                || name.startsWith("list")
                || name.startsWith("count")
                || name.startsWith("insert")
                || name.startsWith("update")
                || name.startsWith("deleteBy")
                || name.startsWith("batch")
                || isDaoBusinessActionName(name);
    }

    private static boolean isServiceMethodShape(JavaMethod method) {
        String name = method.getName();
        if (isNonStandardIdsListName(name) || name.startsWith("find")) {
            return false;
        }
        return name.equals("add")
                || name.equals("count")
                || name.equals("list")
                || name.equals("page")
                || name.equals("update")
                || name.startsWith("add")
                || name.startsWith("getBy")
                || name.startsWith("list")
                || name.startsWith("count")
                || name.startsWith("deleteBy")
                || name.startsWith("batch")
                || name.startsWith("insert")
                || name.startsWith("update")
                || isServiceBusinessActionName(name);
    }

    private static boolean isNonStandardIdsListName(String name) {
        return name.endsWith("ByIds") && !name.equals("listByIds");
    }

    private static boolean isDaoBusinessActionName(String name) {
        return name.equals("active")
                || name.equals("canSend")
                || name.equals("deleteBusiness")
                || name.equals("deleteBusinessByBusiness")
                || name.equals("deleteMenuRole")
                || name.equals("deleteRoleMenu")
                || name.equals("deleteRoleUser")
                || name.equals("deleteUserRole")
                || name.equals("getContentById")
                || name.equals("getDictionaryRevision")
                || name.equals("getMultipartPart")
                || name.equals("getMultipartSessionByUploadId")
                || name.equals("getUidByToken")
                || name.equals("isChildOf")
                || name.equals("markSent")
                || name.equals("moveTreeNode")
                || name.equals("tokenExists")
                || name.equals("touch");
    }

    private static boolean isServiceBusinessActionName(String name) {
        return name.equals("abortMultipartUpload")
                || name.equals("canReadContent")
                || name.equals("completeMultipartUpload")
                || name.equals("createPublicKey")
                || name.equals("createSession")
                || name.equals("decrypt")
                || name.equals("deleteSign")
                || name.equals("encrypt")
                || name.equals("getAccountLoginName")
                || name.equals("getContent")
                || name.equals("getDictionaryRevision")
                || name.equals("getPasswordCredential")
                || name.equals("getPrivateKey")
                || name.equals("getSession")
                || name.equals("initMultipartUpload")
                || name.equals("isChildOf")
                || name.equals("isPermitted")
                || name.equals("moveTreeNode")
                || name.equals("release")
                || name.equals("reloadAll")
                || name.equals("removeReferences")
                || name.equals("sign")
                || name.equals("touch")
                || name.equals("uploadMultipartPart")
                || name.equals("verifySign");
    }

    private static boolean isServiceQueryObject(JavaClass javaClass) {
        String simpleName = javaClass.getSimpleName();
        return simpleName.endsWith("Query") && !"Query".equals(simpleName);
    }

    private static boolean isInServiceQueryPackage(JavaClass javaClass) {
        return javaClass.getPackageName().contains(".service.query");
    }

    private static boolean isServiceQuerySource(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        return value.contains("/modules/") && value.contains("/service/query/") && value.endsWith("Query.java");
    }

    private static boolean containsServiceQuerySetter(Path path) {
        return SERVICE_QUERY_SETTER_DECLARATION_PATTERN
                .matcher(ArchitectureSourceSupport.readSourceWithoutComments(path))
                .find();
    }
}
