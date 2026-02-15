package com.microsoft.model.vo;

import com.microsoft.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserImportVO {
    // 是否导入成功 一旦有失败就是导入不成功
    private Boolean isSuccess;
    // 总共有多少条数据
    private Integer total;
    // 成功数据数
    private Integer successCount;
    // 不符合要求数据数
    private Integer errorCount;
    // 导入失败数据详情
    private List<String> errorMessageList;
    // 成功数据示例
    private List<UserVO> succesList;

    public static UserImportVO success(Integer total, List<UserVO> list) {
        UserImportVO response = new UserImportVO();
        response.setIsSuccess(true);
        response.setTotal(total);
        response.setSuccessCount(total);
        response.setErrorCount(0);
        response.setErrorMessageList(new ArrayList<>());
        response.setSuccesList(list);
        return response;
    }

    public static UserImportVO error(Integer total, Integer errorCount, List<String> list) {
        UserImportVO response = new UserImportVO();
        response.setIsSuccess(false);
        response.setTotal(total);
        response.setSuccessCount(total - errorCount);
        response.setErrorCount(errorCount);
        response.setErrorMessageList(list);
        response.setSuccesList(new ArrayList<>());
        return response;
    }
}
