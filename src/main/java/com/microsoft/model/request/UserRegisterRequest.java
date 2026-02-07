package com.microsoft.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {
    private String userAccount;
    private String password;
    private String checkPassword;
}
