package com.github.kusitms_bugi.domain.user.presentation

import com.github.kusitms_bugi.domain.user.presentation.dto.response.MyProfileResponse
import com.github.kusitms_bugi.domain.user.presentation.dto.response.toMyProfileResponse
import com.github.kusitms_bugi.global.response.ApiResponse
import com.github.kusitms_bugi.global.security.CustomUserDetails
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController : UserApi {

    override fun getMyProfile(userDetails: CustomUserDetails): ApiResponse<MyProfileResponse> {
        return ApiResponse.success(userDetails.user.toMyProfileResponse())
    }
}
