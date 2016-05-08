package com.datou.springstudy.service.impl;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datou.springstudy.service.SortService;

import algorithm.QuickSort;

@Component
public class SortServiceImpl implements SortService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Logger MyTestLogger = LoggerFactory.getLogger("mytestlog");
	@Override
	public void quickSort(int[] array) {
		logger.info("begin quickSort for array {}", Arrays.asList(ArrayUtils.toObject(array)));
		MyTestLogger.info("hello world ,my first loginfo");
		QuickSort qsort = new QuickSort();
		qsort.sort(array);
		tools.tools.printArray(array);
		logger.info("end quickSort for array {}", Arrays.asList(ArrayUtils.toObject(array)));
	}

}
