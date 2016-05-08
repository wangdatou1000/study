package datou.core.exception;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.support.spring.FastJsonJsonView;

public class MyExceptionHandler implements HandlerExceptionResolver, ApplicationEventPublisherAware {
	private ApplicationEventPublisher pulisher;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		// TODO Auto-generated method stub
		this.pulisher = applicationEventPublisher;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {

		if (!(ex instanceof MyException)) {
			logger.error("unknown exception happened at:{}", request.getRequestURI());
			logger.error("unknown exception is ", ex);
			response.setStatus(500);
			response.addHeader("x-unknown-exception", ex.getMessage());
			return new ModelAndView();
		}
		MyException se = (MyException) ex;
		int code = se.getCode();
		String message = se.getErrorMessage();

		String params[] = se.getParams(); // 添加其他的参数，用于重组Exception
		if (params != null) {
			logger.error("exception params is {}", Arrays.asList(params));
		}
		// logger.error("exception params is {}", Arrays.asList(params));
		ModelAndView mv = getErrorJsonView(code, message);
		return mv;
	}

	/**
	 * 使用FastJson提供的FastJsonJsonView视图返回，不需要捕获异常
	 */
	public static ModelAndView getErrorJsonView(int code, String message) {
		ModelAndView modelAndView = new ModelAndView();
		FastJsonJsonView jsonView = new FastJsonJsonView();
		Map<String, Object> errorInfoMap = new HashMap<>();
		errorInfoMap.put("code", code);
		errorInfoMap.put("message", message);
		jsonView.setAttributesMap(errorInfoMap);
		modelAndView.setView(jsonView);
		return modelAndView;
	}

}
