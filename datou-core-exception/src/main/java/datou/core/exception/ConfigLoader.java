package datou.core.exception;



import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datou.core.exception.error.ErrorCode;
import datou.core.exception.error.MyError;

/**
 * 读取配置文件，并且设置枚举类型。 注意配置文件 error-info.yml 使用UTF-8格式
 * <p>
 * Created on 2015/11/3.
 */
public class ConfigLoader {

    private final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
	private Map<String, Object> myErrorMap;

    /**
     * 初始化error code. 由spring 初始化
     */
    public void init() {

        logger.info("start to init error code info");

		for (MyError service_error : MyError.values()) {
			this.initErrorContent(service_error, this.myErrorMap);
        }
		logger.info("init error code success. error size {}", this.myErrorMap.size());

    }

    @SuppressWarnings("unchecked")
    private void initErrorContent(ErrorCode errorCode, Map<String, Object> map) {
        int code = errorCode.getCode();
        String key = "[" + code + "]";  //yml读取的map，key为[422]

        Object messageObj = map.get(key);
        if (messageObj != null) {

            Map<String, Object> errorInfo = (Map<String, Object>) messageObj;

            errorCode.setErrorContent(errorInfo);
        }
    }

	public void setMyErrorMap(Map<String, Object> myErrorMap) {
		this.myErrorMap = myErrorMap;
	}



}
