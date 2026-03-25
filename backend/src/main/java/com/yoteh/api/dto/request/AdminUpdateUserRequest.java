package com.yoteh.api.dto.request;

import com.yoteh.api.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {

    private UserRole role;

    private Boolean isActive;

    private Boolean isVerified;
}
