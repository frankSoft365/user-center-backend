package com.microsoft;

import com.microsoft.model.enums.UserRoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
public class UserCenterApplicationTests {
    @Test
    public void testEnum() {
        System.out.println((Arrays.stream(UserRoleEnum.values()).map(UserRoleEnum::getName)).toList());
        System.out.println(UserRoleEnum.getEnumByName(null));
    }
}
