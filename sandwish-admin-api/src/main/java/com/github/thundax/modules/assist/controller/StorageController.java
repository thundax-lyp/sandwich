package com.github.thundax.modules.assist.controller;

import com.google.common.collect.Maps;
import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.MapUtils;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.web.BaseAdminController;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.utils.StorageServiceHolder;
import com.github.thundax.modules.storage.utils.StorageUtils;
import com.github.thundax.modules.storage.vo.StorageVo;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author thundax
 */
@Controller
@RequestMapping(value = "/api/assist/storage")
public class StorageController extends BaseAdminController {

    private final VltavaProperties.UploadProperties properties;
    private final StorageService storageService;

    @Autowired
    public StorageController(VltavaProperties properties,
                             StorageService storageService,
                             Validator validator) {
        super(validator);
        this.properties = properties.getUpload();
        this.storageService = storageService;
    }

    //@RequiresPermissions("assist:storage:view")
    @RequestMapping(value = {"", "index"})
    public String index() {
        return "modules/assist/storageIndex";
    }


    //@RequiresPermissions("assist:storage:view")
    @RequestMapping(value = "list")
    public String list(HttpServletRequest request, HttpServletResponse response, Model model) {
        Storage storage = readQuery(request, response);
        Page<Storage> page = storageService.findPage(storage, new Page<>(request, response));

        model.addAttribute("storage", storage);
        model.addAttribute("page", page);
        model.addAttribute("mimeTypeList", storageService.findMimeTypeList());

        return "modules/assist/storageList";
    }


    //@RequiresPermissions("user")
    @RequestMapping(value = "upload")
    public String uploadForm(@RequestParam(required = false) String theme,
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
    public StorageVo uploadTest(MultipartFile file) throws ApiException {
        Storage storage = new Storage();

        storage.setOwnerType(Storage.OWNER_TYPE_USER);
        storage.setOwnerId(UserAccessHolder.currentUserId());
        StorageUtils.saveFile(file, storage);

        //TODO 保存关系
//        StorageBusiness storageBusiness = new StorageBusiness();
//        storageBusiness.setPublicFlag(Global.NO);
//        storageBusiness.setBusinessType("");
//        storageBusiness.setBusinessId("");
//        storageBusiness.setBusinessParams("");

        return StorageUtils.entityToVo(storage);
    }



    //@RequiresPermissions("user")
    @RequestMapping(value = "upload", method = RequestMethod.POST)
    @ResponseBody
    public Object upload(HttpServletRequest request) {
        Map<String, Object> result = Maps.newHashMap();

        if (!(request instanceof MultipartHttpServletRequest)) {
            result.put("error", "错误的请求格式");
            return result;

        } else {
            Map<String, MultipartFile> fileMap = ((MultipartHttpServletRequest) request).getFileMap();
            for (MultipartFile file : fileMap.values()) {
                try {
                    String originalFilename = file.getOriginalFilename();

                    List<String> validExtNameList = properties.getAllowSuffix();
                    String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));
                    if (!validExtNameList.contains(extendName)) {
                        result.put("error", "无效的后缀名");
                        return result;
                    }

                    Storage storage = new Storage();
                    storage.setIsNewRecord(true);
                    storage.setId(IdGen.uuid());

                    storage.setName(FilenameUtils.getBaseName(originalFilename));
                    storage.setExtendName(extendName);
                    storage.setMimeType(file.getContentType());

                    storage.setOwnerType(Storage.OWNER_TYPE_USER);
                    storage.setOwnerId(currentUser().getId());

//                    File localFile = new File(properties.getStoragePath() + storage.getFilename());
//                    localFile.getParentFile().mkdirs();

//                    file.transferTo(localFile);
                    storageService.save(storage);

                    result.put("id", storage.getId());
                    result.put("name", storage.getName());
                    result.put("extendName", storage.getExtendName());

                } catch (Exception e) {
                    e.printStackTrace();
                    result.put("error", "系统错误");
                    return result;
                }
            }
        }

        return result;
    }


    @RequestMapping(value = "file/{id}.{extendName}")
    public void preview(@PathVariable("id") String id,
                        @PathVariable("extendName") String extendName,
                        HttpServletResponse response) throws IOException {
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


    //@RequiresPermissions("member:member:edit")
    @RequestMapping(value = "delete")
    public String delete(String[] ids, RedirectAttributes redirectAttributes) {
        if (!validateDelete(ids, redirectAttributes)) {
            return "redirect:" + modulePath + "/list?reload";
        }

        int count = storageService.delete(ListUtils.map(ListUtils.newArrayList(ids), Storage::new));
        addSuccessMessage(redirectAttributes, "共删除" + count + "条记录");

        return "redirect:" + modulePath + "/list?reload";
    }

    //@RequiresPermissions("assist:storage:view")
    @RequestMapping(value = "treeData")
    public List<Map<String, Object>> treeData() {
        return ListUtils.map(storageService.findBusinessTypeList(), businessType -> {
            Map<String, Object> map = MapUtils.newHashMap();
            map.put("id", businessType);
            map.put("pId", "ROOT");
            map.put("name", businessType);
            return map;
        });
    }

    @NotNull
    private Storage readQuery(HttpServletRequest request, HttpServletResponse response) {
        Storage query = new Storage();

        query.setQueryProp(Storage.Query.PROP_MIME_TYPE,
                readReloadString("query.mimeType", "storage.query.mimeType", request, response));

        query.setQueryProp(Storage.Query.PROP_BUSINESS_TYPE,
                readReloadString("query.businessId", "storage.query.businessId", request, response));

        query.setQueryProp(Storage.Query.PROP_ENABLE_FLAG,
                readReloadString("query.enableFlag", "storage.query.enableFlag", request, response));

        query.setQueryProp(Storage.Query.PROP_PUBLIC_FLAG,
                readReloadString("query.publicFlag", "storage.query.publicFlag", request, response));

        query.setQueryProp(Storage.Query.PROP_NAME,
                readReloadString("query.name", "storage.query.name", request, response));

        query.setQueryProp(Storage.Query.PROP_REMARKS,
                readReloadString("query.remarks", "storage.query.remarks", request, response));

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
