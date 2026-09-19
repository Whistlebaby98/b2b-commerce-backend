# B2B Commerce Backend Context

企业级对公商城后端服务，承载多租户组织、阶梯计价、授信结算、交易快照与企业审批流的领域核心。

## Language

### 组织与身份 (IAM)

**CustomerOrganization**:
使用商城进行企业对公采购的法人或经营主体，拥有独立资质、授信额度、协议价与订单归属。
_Avoid_: Company, Tenant, Enterprise

**BuyerUser**:
代表客户组织进行选品、加购与提交采购订单的自然人用户。
_Avoid_: Member, Customer, Consumer

**OrganizationMembership**:
买方用户与客户组织的隶属关系，定义用户的买方角色与在当前企业下的操作范围。
_Avoid_: UserRole, AccountRelation

**ActiveOrganizationContext**:
买方用户当前正在代表的客户组织上下文，决定当前会话中可见的协议价、库存、购物车与订单数据。
_Avoid_: CurrentTenant, SessionOrg

### 商品与计价 (Catalog & Pricing)

**Product**:
展示与归类的商品族（SPU），包含标题、类目、品牌和规格属性，不可直接作为交易结算项。
_Avoid_: SPU, Item, Goods

**SKU**:
商品的可交易库存单位，具有唯一业务编码，定义规格、单位、可售库存和交付承诺。
_Avoid_: SkuItem, Variant

**PriceOffer**:
针对客户组织、SKU、采购数量区间和有效期确定的可交易成交单价，包含协议价与阶梯价。
_Avoid_: Quotation, ProductPrice

**Campaign**:
在满足时间、商品、组织或金额门槛时生效的营销活动规则，订单结算时必须重新核验。
_Avoid_: Activity, Discount

### 交易与结算 (Trade & Finance)

**Cart**:
客户组织在选品过程中暂存 SKU、数量与价格提示的草稿容器，属于 ActiveOrganizationContext，不锁定库存与价格。
_Avoid_: ShoppingCart, Basket

**CheckoutSession**:
将购物车转换为正式订单前，进行收货地址、开票资质、支付条款与金额快照校验的短生命周期会话。
_Avoid_: OrderDraft, PreOrder

**Order**:
客户组织正式提交的对公采购请求，持有不可篡改的商品、价格、地址、发票与结算条款快照。
_Avoid_: PurchaseOrder, Trade

**OrderLine**:
采购订单中的交易行明细，包含 SKU、数量、成交单价快照与优惠分摊明细。
_Avoid_: OrderItem, SubOrder

**CreditLimit**:
客户组织在账期结算条款下可循环使用的信用额度及预占额度。
_Avoid_: Quota, Balance

### 审批与治理 (Approval)

**ApprovalRequest**:
采购订单提交后由系统触发的企业内部审批工作项，记录审批链、节点人与审批时间线。
_Avoid_: WorkflowTask, AuditRecord

**ApprovalPolicy**:
客户组织预设的审批流判定规则，根据订单金额、商品品类或买方部门决定审批层级。
_Avoid_: AuditRule, FlowConfig
