package com.github.thundax.modules.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 字典工具类
 *
 * @version 2013-5-29
 */
public class DictUtils {
	private final RedisClient redisClient;

	private static DictDao dictDao = SpringContextHolder.getBean(DictDao.class);

	public static final String CACHE_DICT_MAP = "dictMap";

	public DictUtils(RedisClient redisClient) {
		this.redisClient = redisClient;
	}

	public String getDictLabel(String value, String type, String defaultValue) throws IOException {
		if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(value)) {
			for (Dict dict : getDictList(type)) {
				if (type.equals(dict.getType()) && value.equals(dict.getValue())) {
					return dict.getLabel();
				}
			}
		}
		return defaultValue;
	}

	public List<String> getDictLabelList(String type) throws IOException {
		List<String> labelList = Lists.newArrayList();
		if (StringUtils.isNotBlank(type)) {
			for (Dict dict : getDictList(type)) {
				if (type.equals(dict.getType())) {
					labelList.add(dict.getLabel());
				}
			}
		}
		return labelList;
	}

	public String getDictLabels(String values, String type, String defaultValue) throws IOException {
		if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(values)) {
			List<String> valueList = Lists.newArrayList();
			for (String value : StringUtils.split(values, ",")) {
				valueList.add(getDictLabel(value, type, defaultValue));
			}
			return StringUtils.join(valueList, ",");
		}
		return defaultValue;
	}

	public String getDictValue(String label, String type, String defaultLabel) throws IOException {
		if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(label)) {
			for (Dict dict : getDictList(type)) {
				if (type.equals(dict.getType()) && label.equals(dict.getLabel())) {
					return dict.getValue();
				}
			}
		}
		return defaultLabel;
	}

	public List<String> getDictValueList(String type) throws IOException {
		List<String> valueList = Lists.newArrayList();
		if (StringUtils.isNotBlank(type)) {
			for (Dict dict : getDictList(type)) {
				if (type.equals(dict.getType())) {
					valueList.add(dict.getValue());
				}
			}
		}
		return valueList;
	}

	public List<Dict> getDictList(String type) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, List<Dict>> dictMap = mapper.readValue(redisClient.get(CACHE_DICT_MAP),new TypeReference<Map<String,List<Dict>>>(){});
		if (dictMap == null) {
			dictMap = Maps.newHashMap();
			for (Dict dict : dictDao.findList(new Dict())) {
				List<Dict> dictList = dictMap.get(dict.getType());
				if (dictList != null) {
					dictList.add(dict);
				} else {
					dictMap.put(dict.getType(), Lists.newArrayList(dict));
				}
			}
			redisClient.set(CACHE_DICT_MAP, dictMap);
		}
		List<Dict> dictList = dictMap.get(type);
		if (dictList == null) {
			dictList = Lists.newArrayList();
		}
		return dictList;
	}

}
