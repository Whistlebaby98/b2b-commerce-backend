package com.b2b.iam.application;

import com.b2b.iam.api.dto.LoginResponse;
import com.b2b.iam.api.dto.RegisterRequest;
import com.b2b.iam.infrastructure.persistence.entity.CompanyPO;
import com.b2b.iam.infrastructure.persistence.mapper.CompanyMapper;
import com.b2b.iam.infrastructure.persistence.mapper.MembershipMapper;
import com.b2b.iam.infrastructure.persistence.mapper.UserMapper;
import com.b2b.iam.infrastructure.security.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 买方注册应用服务单元测试 (AuthRegisterTest)
 *
 * @author b2b-commerce-backend
 */
@ExtendWith(MockitoExtension.class)
class AuthRegisterTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private MembershipMapper membershipMapper;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthApplicationService authApplicationService;

    @Test
    @DisplayName("测试买方用户自主注册并直接签发登录凭证")
    void testRegisterSuccess() {
        RegisterRequest req = RegisterRequest.builder()
                .name("张采购")
                .email("zhang@example.com")
                .password("password123")
                .companyName("示范采购有限公司")
                .build();

        when(userMapper.selectOne(any())).thenReturn(null);
        when(companyMapper.selectOne(any())).thenReturn(null);
        when(jwtUtils.generateAccessToken(any(), any())).thenReturn("mock-access-token");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("mock-refresh-token");

        LoginResponse response = authApplicationService.register(req);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getName()).isEqualTo("张采购");
        assertThat(response.getCurrentCompany()).isNotNull();
        assertThat(response.getCurrentCompany().getName()).isEqualTo("示范采购有限公司");
    }
}
