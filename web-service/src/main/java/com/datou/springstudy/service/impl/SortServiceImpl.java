package com.datou.springstudy.service.impl;

import org.springframework.stereotype.Component;

import com.datou.springstudy.service.SortService;

import algorithm.QuickSort;

@Component
public class SortServiceImpl implements SortService
{

	@Override
	public void quickSort(int[] array) {
		QuickSort qsort = new QuickSort();
		qsort.sort(array);

	}

}
