package com.github.thundax.modules.assist.controller;

import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.common.web.BaseAdminController;
import com.github.thundax.modules.assist.assembler.StorageInterfaceAssembler;
import com.github.thundax.modules.assist.response.StorageTreeNodeResponse;
import com.github.thundax.modules.assist.response.StorageUploadResponse;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.utils.StorageServiceHolder;
import com.github.thundax.modules.storage.utils.StorageUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/api/assist/storage")
public class StorageController extends BaseAdminController {

    private final VltavaProperties.UploadProperties properties;
    private final StorageService storageService;
    private final StorageInterfaceAssembler storageInterfaceAssembler;

    @Autowired
    public StorageController(
            VltavaProperties properties,
            StorageService storageService,
            Validator validator,
            StorageInterfaceAssembler storageInterfaceAssembler) {
        super(validator);
        this.properties = properties.getUpload();
        this.storageService = storageService;
        this.storageInterfaceAssembler = storageInterfaceAssembler;
    }

    @RequestMapping(value = {"", "index"})
    public String index() {
        return "modules/assist/storageIndex";
    }

    // 服务端页面入口保留旧查询适配，不作为本轮核心 API 模型隔离目标。
    @RequestMapping(value = "list")
    public String list(HttpServletRequest request, HttpServletResponse response, Model model) {
        Storage storage = readQuery(request, response);
        Page<Storage> page = storageService.findPage(storage, new Page<>(request, response));

        model.addAttribute("storage", storage);
        model.addAttribute("page", page);
        model.addAttribute("mimeTypeList", storageService.findMimeTypeList());

        return "modules/assist/storageList";
    }

    @RequestMapping(value = "upload")
    public String uploadForm(
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String allowedFileSuffix,
            @RequestParam(required = false) Integer maxFileCount,
            Model model) {
        if (StringUtils.isBlank(theme)) {
            theme = StringUtils.EMPTY;
        }
        if (StringUtils.isBlank(allowedFileSuffix)) {
            allowedFileSuffix = StringUtils.join(properties.getAllowSuffix(), ",");
        }
        if (maxFileCount == null || maxFileCount > properties.getMaxFileCount()) {
            maxFileCount = properties.getMaxFileCount();
        }

        model.addAttribute("theme", theme);
        model.addAttribute("allowedFileSuffix", allowedFileSuffix);
        model.addAttribute("maxFileSize", properties.getMaxFileSize());
        model.addAttribute("maxFileCount", maxFileCount);
        return "modules/assist/storageUpload";
    }

    @ResponseBody
    @RequestMapping(value = "test-upload", method = RequestMethod.POST)
    public StorageUploadResponse uploadTest(MultipartFile file) throws ApiException {
        Storage storage = new Storage();

        storage.setOwnerType(Storage.OWNER_TYPE_USER);
        storage.setOwnerId(UserAccessHolder.currentUserId());
        StorageUtils.saveFile(file, storage);

        // TODO 保存关系
        //        StorageBusiness storageBusiness = new StorageBusiness();
        //        storageBusiness.setPublicFlag(Global.NO);
        //        storageBusiness.setBusinessType("");
        //        storageBusiness.setBusinessId("");
        //        storageBusiness.setBusinessParams("");

        return storageInterfaceAssembler.toUploadResponse(storage);
    }

    @RequestMapping(value = "upload", method = RequestMethod.POST)
    @ResponseBody
    public StorageUploadResponse upload(HttpServletRequest request) {
        if (!(request instanceof MultipartHttpServletRequest)) {
            return storageInterfaceAssembler.toUploadErrorResponse("错误的请求格式");

        } else {
            Map<String, MultipartFile> fileMap = ((MultipartHttpServletRequest) request).getFileMap();
            StorageUploadResponse response = new StorageUploadResponse();
            for (MultipartFile file : fileMap.values()) {
                try {
                    String originalFilename = file.getOriginalFilename();

                    List<String> validExtNameList = properties.getAllowSuffix();
                    String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));
                    if (!validExtNameList.contains(extendName)) {
                        return storageInterfaceAssembler.toUploadErrorResponse("无效的后缀名");
                    }

                    Storage storage = new Storage();
                    storage.setIsNewRecord(true);
                    storage.setId(IdGen.uuid());

                    storage.setName(FilenameUtils.getBaseName(originalFilename));
                    storage.setExtendName(extendName);
                    storage.setMimeType(file.getContentType());

                    storage.setOwnerType(Storage.OWNER_TYPE_USER);
                    storage.setOwnerId(currentUser().getId());

                    //                    File localFile = new File(properties.getStoragePath() +
                    // storage.getFilename());
                    //                    localFile.getParentFile().mkdirs();

                    //                    file.transferTo(localFile);
                    storageService.add(storage);

                    response = storageInterfaceAssembler.toUploadResponse(storage);

                } catch (Exception e) {
                    e.printStackTrace();
                    return storageInterfaceAssembler.toUploadErrorResponse("系统错误");
                }
            }
            return response;
        }
    }

    // 文件预览是静态资源支撑入口，保持 HttpServletResponse 流式输出边界。
    @RequestMapping(value = "file/{id}.{extendName}")
    public void preview(
            @PathVariable("id") String id, @PathVariable("extendName") String extendName, HttpServletResponse response)
            throws IOException {
        Storage storage = StorageServiceHolder.get(id);
        if (storage == null || !StringUtils.equalsAnyIgnoreCase(storage.getExtendName(), extendName)) {
            response.sendError(HttpStatus.SC_NOT_FOUND);
            return;
        }

        File file = new File("");
        if (!file.exists()) {
            response.sendError(HttpStatus.SC_NOT_FOUND);
            return;
        }

        response.setContentType(storage.getMimeType());
        OutputStream outputStream = response.getOutputStream();

        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, readBytes);
            }
        }
        outputStream.close();
    }

    // 服务端页面删除入口保留 RedirectAttributes 跳转反馈，不作为本轮核心 API 模型隔离目标。
    @RequestMapping(value = "delete")
    public String delete(String[] ids, RedirectAttributes redirectAttributes) {
        if (!validateDelete(ids, redirectAttributes)) {
            return "redirect:" + modulePath + "/list?reload";
        }

        int count = storageService.delete(new ArrayList<>(Arrays.asList(ids))
                .stream().map(id -> new Storage(id)).collect(Collectors.toList()));
        addSuccessMessage(redirectAttributes, "共删除" + count + "条记录");

        return "redirect:" + modulePath + "/list?reload";
    }

    @RequestMapping(value = "treeData")
    @ResponseBody
    public List<StorageTreeNodeResponse> treeData() {
        return storageService.findBusinessTypeList().stream()
                .map(businessType -> storageInterfaceAssembler.toBusinessTypeTreeNode(businessType))
                .collect(Collectors.toList());
    }

    @NotNull
    private Storage readQuery(HttpServletRequest request, HttpServletResponse response) {
        Storage query = new Storage();
        Storage.Query queryCondition = new Storage.Query();

        queryCondition.setMimeType(readReloadString("query.mimeType", "storage.query.mimeType", request, response));

        queryCondition.setBusinessType(
                readReloadString("query.businessId", "storage.query.businessId", request, response));

        queryCondition.setEnableFlag(
                readReloadString("query.enableFlag", "storage.query.enableFlag", request, response));

        queryCondition.setPublicFlag(
                readReloadString("query.publicFlag", "storage.query.publicFlag", request, response));

        queryCondition.setName(readReloadString("query.name", "storage.query.name", request, response));

        queryCondition.setRemarks(readReloadString("query.remarks", "storage.query.remarks", request, response));
        query.setQuery(queryCondition);

        return query;
    }

    private boolean validateExists(String id, RedirectAttributes redirectAttributes) {
        if (StringUtils.isBlank(id)) {
            addWarningMessage(redirectAttributes, "无效的请求");
            return false;
        }

        Storage bean = StorageServiceHolder.get(id);
        if (bean == null) {
            addWarningMessage(redirectAttributes, "无效的数据");
            return false;
        }

        return true;
    }

    private boolean validateDelete(String[] ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.length <= 0) {
            addWarningMessage(redirectAttributes, "无效的请求");
            return false;
        }

        for (String id : ids) {
            if (!validateExists(id, redirectAttributes)) {
                return false;
            }
        }
        return true;
    }
}
