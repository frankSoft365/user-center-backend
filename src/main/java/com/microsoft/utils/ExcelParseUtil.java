package com.microsoft.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.microsoft.commen.ErrorCode;
import com.microsoft.exception.BusinessException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelParseUtil {

    // 允许的Excel格式
    private static final String[] ALLOWED_EXCEL_SUFFIX = {"xlsx", "xls"};

    /**
     * 解析Excel文件为指定DTO列表
     * @param file 上传的Excel文件
     * @param clazz 要转换的DTO类，如 UserImportDTO.class
     * @return 解析后的DTO列表
     */
    public static <T> List<T> parseExcel(MultipartFile file, Class<T> clazz) {
        // 1. 校验文件是否为合法Excel
        validateExcelFile(file);

        // 2. 使用EasyExcel解析
        List<T> dataList = new ArrayList<>();
        try {
            EasyExcel.read(
                    file.getInputStream(),
                    clazz,
                    // 分页读取，避免大文件OOM
                    new PageReadListener<T>(dataList::addAll)
            ).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException("Excel解析失败：" + e.getMessage());
        }

        return dataList;
    }

    /**
     * 校验文件是否为合法Excel
     */
    private static void validateExcelFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "上传的文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件名称为空");
        }

        // 校验文件后缀
        String suffix = FilenameUtils.getExtension(originalFilename).toLowerCase();
        boolean isExcel = false;
        for (String allowed : ALLOWED_EXCEL_SUFFIX) {
            if (allowed.equals(suffix)) {
                isExcel = true;
                break;
            }
        }
        if (!isExcel) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请上传 .xlsx 或 .xls 格式的文件");
        }
    }
}