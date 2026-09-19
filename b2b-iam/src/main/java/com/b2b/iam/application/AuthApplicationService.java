package com.b2b.iam.application;

import com.b2b.common.api.ResultCode;
import com.b2b.common.exception.BizException;
import com.b2b.iam.api.dto.LoginRequest;
import com.b2b.iam.api.dto.LoginResponse;
import com.b2b.iam.api.dto.OrganizationDTO;
import com.b2b.iam.api.dto.RefreshTokenRequest;
import com.b2b.iam.api.dto.SessionSnapshotResponse;
import com.b2b.iam.api.dto.SwitchContextRequest;
import com.b2b.iam.api.dto.UserDTO;
import com.b2b.iam.infrastructure.persistence.entity.CompanyPO;
import com.b2b.iam.infrastructure.persistence.entity.MembershipPO;
import com.b2b.iam.infrastructure.persistence.entity.UserPO;
import com.b2b.iam.infrastructure.persistence.mapper.CompanyMapper;
import com.b2b.iam.infrastructure.persistence.mapper.MembershipMapper;
import com.b2b.iam.infrastructure.persistence.mapper.UserMapper;
import com.b2b.iam.infrastructure.security.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.b2b.iam.api.dto.RegisterRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 组织与身份认证应用服务 (AuthApplicationService)
 *
 * <p>提供买方用户登录认证、双 Token 颁发、多组织会话快照与企业上下文切换等用例编排。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;
    private final MembershipMapper membershipMapper;
    private final JwtUtils jwtUtils;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 买方用户账号密码登录
     *
     * @param request 登录请求入参（包含邮箱与明文密码）
     * @return 包含访问令牌、买方档案与可选企业列表的响应对象
     * @throws BizException 邮箱不存在、密码错误或未加入任何企业时抛出
     */
    public LoginResponse login(LoginRequest request) throws BizException {
        // 1. 根据企业邮箱检索买方用户
        UserPO user = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>()
                        .eq(UserPO::getEmail, request.getEmail().trim())
        );
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "企业邮箱或密码错误");
        }

        // 2. 校验密码哈希 (兼容 BCrypt 哈希与明文兜底校验)
        boolean passwordMatched = false;
        if (user.getPasswordHash().startsWith("$2a$") || user.getPasswordHash().startsWith("$2b$")) {
            passwordMatched = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        } else {
            passwordMatched = user.getPasswordHash().equals(request.getPassword());
        }
        if (!passwordMatched) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "企业邮箱或密码错误");
        }

        // 3. 查询买方关联的所有合法企业组织关系 (org_membership)
        List<MembershipPO> memberships = membershipMapper.selectList(
                new LambdaQueryWrapper<MembershipPO>()
                        .eq(MembershipPO::getUserId, user.getId())
        );
        if (memberships.isEmpty()) {
            throw new BizException(ResultCode.MEMBERSHIP_INVALID.getCode(), "该买方用户尚未加入任何客户企业组织");
        }

        // 4. 批量查询企业组织档案
        List<String> companyIds = memberships.stream().map(MembershipPO::getCompanyId).toList();
        List<CompanyPO> companyPOs = companyMapper.selectBatchIds(companyIds);
        Map<String, CompanyPO> companyMap = companyPOs.stream()
                .collect(Collectors.toMap(CompanyPO::getId, c -> c));

        List<OrganizationDTO> availableCompanies = companyPOs.stream()
                .map(this::toOrganizationDTO)
                .toList();

        // 5. 默认选中第一个企业作为当前 ActiveOrganizationContext
        MembershipPO defaultMembership = memberships.get(0);
        CompanyPO currentCompanyPO = companyMap.get(defaultMembership.getCompanyId());
        if (currentCompanyPO == null) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND);
        }
        OrganizationDTO currentCompany = toOrganizationDTO(currentCompanyPO);

        // 6. 颁发双 Token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), currentCompany.getId());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .role(defaultMembership.getRole())
                .department(user.getDepartment())
                .avatarText(user.getAvatarText())
                .companyId(currentCompany.getId())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200L)
                .user(userDTO)
                .currentCompany(currentCompany)
                .availableCompanies(availableCompanies)
                .build();
    }

    /**
     * 买方用户自主注册并直接生成登录会话
     *
     * @param request 注册请求参数（姓名、企业邮箱、密码、可选企业名）
     * @return 包含访问令牌与企业上下文的登录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        String email = request.getEmail().trim();
        UserPO existing = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getEmail, email)
        );
        if (existing != null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "该企业邮箱已被注册: " + email);
        }

        // 1. 创建买方用户档案
        String userId = "usr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String name = request.getName().trim();
        String avatarText = name.length() > 2 ? name.substring(0, 2) : name;

        UserPO user = UserPO.builder()
                .id(userId)
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .department("采购部")
                .avatarText(avatarText)
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userMapper.insert(user);

        // 2. 关联或创建客户企业
        CompanyPO company;
        if (StringUtils.hasText(request.getCompanyName())) {
            String companyName = request.getCompanyName().trim();
            company = companyMapper.selectOne(
                    new LambdaQueryWrapper<CompanyPO>().eq(CompanyPO::getName, companyName)
            );
            if (company == null) {
                String companyId = "org_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                company = CompanyPO.builder()
                        .id(companyId)
                        .name(companyName)
                        .shortName(companyName.length() > 6 ? companyName.substring(0, 6) : companyName)
                        .taxId("91440300" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                        .customerTier("gold")
                        .isVerified(true)
                        .creditLimit(new BigDecimal("1000000.00"))
                        .creditUsed(BigDecimal.ZERO)
                        .paymentTermDays(30)
                        .currency("CNY")
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .deleted(false)
                        .build();
                companyMapper.insert(company);
            }
        } else {
            company = companyMapper.selectById("company-lantu");
            if (company == null) {
                company = companyMapper.selectOne(new LambdaQueryWrapper<CompanyPO>().last("LIMIT 1"));
            }
            if (company == null) {
                String companyId = "org_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                company = CompanyPO.builder()
                        .id(companyId)
                        .name("深圳市蓝图精密制造有限公司")
                        .shortName("蓝图精密")
                        .taxId("91440300MA5F8N7X2K")
                        .customerTier("gold")
                        .isVerified(true)
                        .creditLimit(new BigDecimal("1280000.00"))
                        .creditUsed(BigDecimal.ZERO)
                        .paymentTermDays(30)
                        .currency("CNY")
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .deleted(false)
                        .build();
                companyMapper.insert(company);
            }
        }

        // 3. 创建成员身份关系
        MembershipPO membership = MembershipPO.builder()
                .userId(userId)
                .companyId(company.getId())
                .role("buyer")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        membershipMapper.insert(membership);

        // 4. 签发双 Token 并构造登录响应
        String accessToken = jwtUtils.generateAccessToken(userId, company.getId());
        String refreshToken = jwtUtils.generateRefreshToken(userId);

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .role("buyer")
                .department(user.getDepartment())
                .avatarText(user.getAvatarText())
                .companyId(company.getId())
                .build();

        OrganizationDTO orgDTO = toOrganizationDTO(company);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200L)
                .user(userDTO)
                .currentCompany(orgDTO)
                .availableCompanies(Collections.singletonList(orgDTO))
                .build();
    }

    /**
     * 获取当前买方会话快照 (SessionSnapshot)
     *
     * @param userId    当前登录用户 ID
     * @param companyId 当前请求企业 ID
     * @return 会话快照 DTO（对齐前端 SessionState）
     */
    public SessionSnapshotResponse getSession(String userId, String companyId) {
        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        CompanyPO company = companyMapper.selectById(companyId);
        if (company == null) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND);
        }

        MembershipPO membership = membershipMapper.selectOne(
                new LambdaQueryWrapper<MembershipPO>()
                        .eq(MembershipPO::getUserId, userId)
                        .eq(MembershipPO::getCompanyId, companyId)
        );
        String role = membership != null ? membership.getRole() : "buyer";

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .role(role)
                .department(user.getDepartment())
                .avatarText(user.getAvatarText())
                .companyId(company.getId())
                .build();

        return SessionSnapshotResponse.builder()
                .authenticated(true)
                .email(user.getEmail())
                .user(userDTO)
                .company(toOrganizationDTO(company))
                .build();
    }

    /**
     * 切换当前代表的企业组织上下文 (ActiveOrganizationContext)
     *
     * @param userId  买方用户 ID
     * @param request 切换请求参数（目标企业 ID）
     * @return 重新签发带有新企业上下文的访问令牌与企业信息
     */
    public LoginResponse switchOrganization(String userId, SwitchContextRequest request) {
        String targetCompanyId = request.getTargetCompanyId();

        // 1. 校验该用户对目标企业的成员资格
        MembershipPO membership = membershipMapper.selectOne(
                new LambdaQueryWrapper<MembershipPO>()
                        .eq(MembershipPO::getUserId, userId)
                        .eq(MembershipPO::getCompanyId, targetCompanyId)
        );
        if (membership == null) {
            throw new BizException(ResultCode.MEMBERSHIP_INVALID);
        }

        CompanyPO targetCompany = companyMapper.selectById(targetCompanyId);
        if (targetCompany == null || Boolean.TRUE.equals(targetCompany.getDeleted())) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND);
        }

        UserPO user = userMapper.selectById(userId);

        // 2. 颁发绑定新企业上下文的 Access Token
        String accessToken = jwtUtils.generateAccessToken(userId, targetCompanyId);
        String refreshToken = jwtUtils.generateRefreshToken(userId);

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .role(membership.getRole())
                .department(user.getDepartment())
                .avatarText(user.getAvatarText())
                .companyId(targetCompanyId)
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200L)
                .user(userDTO)
                .currentCompany(toOrganizationDTO(targetCompany))
                .availableCompanies(Collections.singletonList(toOrganizationDTO(targetCompany)))
                .build();
    }

    /**
     * 使用 Refresh Token 刷新 Access Token
     *
     * @param request 刷新令牌请求
     * @return 新签发的 Access Token 响应
     */
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "刷新令牌已失效，请重新登录");
        }

        String userId = jwtUtils.getUserIdFromToken(refreshToken);
        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        // 确定企业上下文
        String companyId = request.getCompanyId();
        if (!StringUtils.hasText(companyId)) {
            List<MembershipPO> memberships = membershipMapper.selectList(
                    new LambdaQueryWrapper<MembershipPO>().eq(MembershipPO::getUserId, userId)
            );
            if (memberships.isEmpty()) {
                throw new BizException(ResultCode.MEMBERSHIP_INVALID);
            }
            companyId = memberships.get(0).getCompanyId();
        }

        CompanyPO company = companyMapper.selectById(companyId);
        String newAccessToken = jwtUtils.generateAccessToken(userId, companyId);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200L)
                .currentCompany(toOrganizationDTO(company))
                .build();
    }

    private OrganizationDTO toOrganizationDTO(CompanyPO po) {
        return OrganizationDTO.builder()
                .id(po.getId())
                .name(po.getName())
                .shortName(po.getShortName())
                .taxId(po.getTaxId())
                .customerTier(po.getCustomerTier())
                .isVerified(po.getIsVerified())
                .creditLimit(po.getCreditLimit())
                .creditUsed(po.getCreditUsed())
                .paymentTermDays(po.getPaymentTermDays())
                .currency(po.getCurrency())
                .build();
    }
}
