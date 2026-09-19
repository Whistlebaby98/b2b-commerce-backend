package com.b2b.finance.service;

import com.b2b.finance.api.dto.AddressDTO;
import com.b2b.finance.api.dto.CreateAddressRequest;
import com.b2b.finance.api.dto.CreateInvoiceRequest;
import com.b2b.finance.api.dto.InvoiceDTO;
import com.b2b.finance.infrastructure.persistence.entity.CompanyAddressPO;
import com.b2b.finance.infrastructure.persistence.entity.InvoiceProfilePO;
import com.b2b.finance.infrastructure.persistence.mapper.CompanyAddressMapper;
import com.b2b.finance.infrastructure.persistence.mapper.CompanyCreditMapper;
import com.b2b.finance.infrastructure.persistence.mapper.InvoiceProfileMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资金与企业结算档案服务单元测试 (FinanceApplicationServiceTest)
 *
 * @author b2b-commerce-backend
 */
@ExtendWith(MockitoExtension.class)
class FinanceApplicationServiceTest {

    @Mock
    private CompanyCreditMapper companyCreditMapper;

    @Mock
    private CompanyAddressMapper companyAddressMapper;

    @Mock
    private InvoiceProfileMapper invoiceProfileMapper;

    @InjectMocks
    private FinanceApplicationService financeApplicationService;

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CompanyAddressPO.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), InvoiceProfilePO.class);
    }

    @Test
    @DisplayName("测试新增企业地址")
    void testCreateAddress() {
        CreateAddressRequest req = CreateAddressRequest.builder()
                .label("东莞分仓")
                .kind("shipping")
                .recipient("李经理")
                .phone("13800000000")
                .province("广东省")
                .city("东莞市")
                .district("长安镇")
                .detail("振安东路1号")
                .isDefault(true)
                .build();

        AddressDTO dto = financeApplicationService.createAddress("company-lantu", req);

        assertThat(dto).isNotNull();
        assertThat(dto.getLabel()).isEqualTo("东莞分仓");
        assertThat(dto.getRecipient()).isEqualTo("李经理");
        assertThat(dto.getIsDefault()).isTrue();
        verify(companyAddressMapper).insert(any(CompanyAddressPO.class));
    }

    @Test
    @DisplayName("测试新增企业开票资质")
    void testCreateInvoice() {
        CreateInvoiceRequest req = CreateInvoiceRequest.builder()
                .type("vat_special")
                .title("深圳市蓝图精密制造有限公司")
                .taxId("91440300MA5F8N7X2K")
                .receiveEmail("finance@lantu.com")
                .isDefault(true)
                .build();

        InvoiceDTO dto = financeApplicationService.createInvoice("company-lantu", req);

        assertThat(dto).isNotNull();
        assertThat(dto.getTitle()).isEqualTo("深圳市蓝图精密制造有限公司");
        assertThat(dto.getReceiveEmail()).isEqualTo("finance@lantu.com");
        assertThat(dto.getIsDefault()).isTrue();
        verify(invoiceProfileMapper).insert(any(InvoiceProfilePO.class));
    }
}
