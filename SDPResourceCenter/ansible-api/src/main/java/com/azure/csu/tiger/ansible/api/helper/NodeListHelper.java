package com.azure.csu.tiger.ansible.api.helper;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NodeListHelper {

    public static List partition(List list, int size) {
        if (list == null || size <= 0) {
            throw new IllegalArgumentException("The list cannot be null and the size should be greater than 0");
        }
        //Create an index stream, Create sublists, Collect sublists into a List
        return IntStream.range(0, (list.size() + size - 1) / size).mapToObj(i -> list.subList(i * size, Math.min(list.size(), (i + 1) * size))).collect(Collectors.toList());

    }
}
