# 魅族联运 SDK 2.1.0 接入指南

本文主要描述了魅族联运 SDK 2.1.0 版本新增「签约支付」功能的接入方法。

# 功能变更（相较于 2.0.0 版本）

1. 新增「签约支付」功能

2. 提升 SDK 稳定性

3. 如果您现在接的是 2.0.0 版本，请额外再添加下面这些依赖：
``` groovy
implementation "com.squareup.retrofit2:converter-gson:2.6.1"
implementation "com.squareup.retrofit2:adapter-rxjava2:2.6.1"
implementation "io.reactivex.rxjava2:rxjava:2.2.6"
implementation "io.reactivex.rxjava2:rxandroid:2.1.1"
```
4. 如果您现在接的是 [1.0.X](https://github.com/MeizuAppCenter/MzAppCenterSdkDemo/releases/tag/1.0.7) 版本，请先阅读 [魅族联运 SDK 2.0 迁移指南][2]

# 概念理解（必看）

1. 签约支付目只支持支付宝，不支持微信。如果用户没装支付宝客户端，SDK 内部会给出提示安装。
2. 用户签约完成后，之后每次到时间的周期性扣款，只能由 CP 主动调我们的接口发起，魅族与支付宝任何一方都不会自动去扣款。
3. 假设下方这个场景：

> 今天是 2020 年 11 月 1 日，用户以 0.99 元钱开通了某某会员，可免费体验 3 天，之后 10 元 / 月。

则 `totalFee` 是 `0.99`，`singleAmount` 是 `10.00`，`executeTime` 是 `2020-11-04`，`period` 是 `1`，`periodType` 是 `MONTH`。

到了 `2020-11-04` 这天 24 小时内任意时间 CP 可发起扣款。

超期未扣款或扣款失败，可以在下个周期到来前 3 天内（即 `2020-12-01` 至 `2020-12-03` ），执行补扣。

__CP 服务端需重点关注这边的逻辑，服务端文档可参考：https://github.com/MeizuAppCenter/MzAppCenterSdkServerDemo/blob/master/SDK3.0.md。__

# 时序图

[点击查看](https://www.processon.com/view/link/5f9785a77d9c0806f2912bd2)

# 效果演示

![](static/device-2020-11-24-113056.gif)

# 接入步骤

1-5.通用步骤：

SDK 所有接口都要先调用 `MzAppCenterPlatform.getInstance()?.login()`以确保用户已经登录并授权。如果用户已经登录并授权，可继续在 `onLoginSuccess()` 发起业务请求，否则在 `onActivityResult()`中根据 `requestCode` 发起业务请求。请参考[ MzAppCenterSdkDemo 接入文档「开始接入」部分的步骤1 - 5](https://github.com/MeizuAppCenter/MzAppCenterSdkDemo#%E5%BC%80%E5%A7%8B%E6%8E%A5%E5%85%A5)


6.在用户登录并且获取到 `token` 后，调用下述方法开始签约并支付：

``` kotlin
private fun invokeSdkToPayAndSign() {
    MzAppCenterPlatform.getInstance()?.payAndSign(activity, payAndSignInfo, object : IPayAndSignResultListener {
                override fun onPaySuccess() {
                    //支付成功，具体请以服务端回调为准
                    //签约的接口请以服务端回调为准
                }

                override fun onFailed(code: Int, message: String) {
                    //支付失败
                }
            })
}

```

`onFailed()` 中的 `code` 与 `message` 被定义在 `com.meizu.mstore.sdk.payandsign.PayAndSignResult`，您可以根据如下说明，结合实际情况给予用户相应引导：

| code | message | 建议操作 |
| ------ | ------ | ------ |
| `PayAndSignResult.CODE_ERROR_NETWORK_DISCONNECTED` | 无法连接网络，请检查网络设置 | 引导用户检查网络设置 |
| `PayAndSignResult.CODE_ERROR_PAY_SUCCESS_BUT_BOOL_FAILED` | 用户不再签约 | 用户关闭了支付宝签约窗口，引导用户重新发起 |
| 其它 code |  见 message | 只会在开发阶段出现，联系魅族 |

7.在用户登录并且获取到 `token` 后，调用下述方法开始解约：

``` kotlin
private fun invokeSdkToCancelSign() {
        MzAppCenterPlatform.getInstance()?.unSign(it, et_cp_sign_no.text.toString(), object : ICancelSignResultListener {
                override fun onCancelSignSuccess() {
                    //解约成功，具体请以服务端回调为准
                }

                override fun onFailed(code: Int, message: String) {
                    //解约失败
                }
            })
    }

```

`onFailed()` 中的 `code` 与 `message` 被定义在 `com.meizu.mstore.sdk.payandsign.PayAndSignResult`，您可以根据如下说明，结合实际情况给予用户相应引导：

| code | message | 建议操作 |
| ------ | ------ | ------ |
| `PayAndSignResult.CODE_ERROR_NETWORK_DISCONNECTED` | 无法连接网络，请检查网络设置 | 引导用户检查网络设置 |
| `PayAndSignResult.CODE_ERROR_CANCEL_SIGN_SUCCESS_BUT_BOOL_FAILED` | 用户不再解约 | 用户关闭了支付宝解约窗口，引导用户重新发起 |
| 其它 code |  见 message | 只会在开发阶段出现，联系魅族 |

8.当页面退出，记得在 `onDestroy()` 中调用此方法进行一些清理工作：
```kotlin
    MzAppCenterPlatform.getInstance()?.onDestroy()
```

#  参数说明

## `PayAndSignInfo`

`PayAndSignInfo` 为签约支付时的实体，它包含如下五个对象：

| 参数名 | 类型 | 是否必填 | 说明 | 示例参数 |
| ------ | ------ | ------ | ------ |------ |
| orderInfo | OrderInfo | 是 | 订单相关的信息 | `orderInfo` |
| productInfo | ProductInfo | 是 | 产品相关的信息 | `productInfo` |
| notifyUrlInfo | NotifyUrlInfo | 是 | 签约相关的信息 | `notifyUrlInfo` |
| cyclePayInfo | CyclePayInfo | 是 | 周期扣款相关的信息 | `productName` |
| attach | String | 否 | CP自定义信息 | `"attach"` |

### `OrderInfo`

| 参数名 | 类型 | 是否必填 | 说明 | 示例参数 |
| ------ | ------ | ------ | ------ |------ |
| tradeNo | String | 是 | CP 自己体系内的订单号 | `"202011230001"` |
| createTime | Long | 是 | 产品相关的信息 | `System.currentTimeMillis()` |

### `ProductInfo`

| 参数名 | 类型 | 是否必填 | 说明 | 示例参数 |
| ------ | ------ | ------ | ------ |------ |
| productId | String | 是 | 商品ID | `"id_0001"`|
| productName | String | 是 | 商品名称 |  `"会员大晒啊"` |
| productBody | String | 是 | 商品详情 | `"Sorry会员真係大晒"` |
| productUnit | String | 是 | 商品单位 | `"份"  ` |
| productPerPrice | BigDecimal | 是 | 商品单价，单位 `元`。__这个值决定了首次签约时要付多少钱__ | `0.01` |
| buyAmount | Integer | 是 | 购买数量 | `1` |
| totalFee | Double | 是 | 购买总价，单位 `元` | `0.01` |

### `NotifyUrlInfo`

| 参数名 | 类型 | 是否必填 | 说明 | 示例参数 |
| ------ | ------ | ------ | ------ |------ |
| payNotifyUrl | String | 是 | 支付成功后回调地址 | `"https://api.xx.com/receive_pay_notify"`|
| signNotifyUrl | String | 是 | 签约成功后回调地址 |  `"https://api.xx.com/receive_sign_notify"` |

### `CyclePayInfo`

| 参数名 | 类型 | 是否必填 | 说明 | 示例参数 |
| ------ | ------ | ------ | ------ | ------ |
| merchantName | String | 是 | 商户名称 | `"XXX公司"` |
| merchantServiceName | String | 是 | 服务名称 | `"应用商店会员服务"` |
| body | String | 是 | 服务描述| `"每月定期为会员续费"`  |
| signScene | SignScene | 是 | 签约场景，请[根据你的服务类型选择枚举](https://opendocs.alipay.com/pre-open/20170601105911096277new/sgaanb) | `CyclePayInfo.SignScene.APP_STORE` |
| cpSignNo | String | 是 | CP 签约号，用来唯一确定一份合同 | `"20201026194300"` |
| periodType | PeriodType | 是 | 扣款周期单位，枚举值为 `DAY` 或 `MONTH` | `CyclePayInfo.PeriodType.MONTH` |
| period | Integer | 是 | 扣款周期，与上面的`扣款周期单位`组合使用，确定扣款周期，例如 `扣款周期单位` 为 `DAY`，`扣款周期` 为 `90`，则扣款周期为 `90 天`。**扣款周期最短不得设置小于 7 天，最长没有限制**。 | `7` |
| singleAmount | String | 是 | 每次发起扣款时限制的最大金额，单位为元。商户每次发起扣款都不允许大于此金额。**对每个用户的单笔扣款不超过 100 元，当日扣款不超过 1000 元** | `10.00` |
| executeTime | String | 是 | 商户发起首次扣款的时间。精确到日，格式为 yyyy-MM-dd。 结合其他必填的扣款周期参数，会确定商户以后的扣款计划。发起扣款的时间需符合这里的扣款计划。__另外，如果扣款周日是`1个月`，那么这里的日期不能传每个月 28 号之后的日期。__ | `"2020-10-27"` |
| totalAmount | BigDecimal | 否 | 单位为元。如果传入此参数，商户多次扣款的累计金额不允许超过此金额。**对每个用户的当月扣款不超过 30000 元** | `70.00` |
| totalPayments | Int | 否 | 如果传入此参数，则商户成功扣款的次数不能超过此次数限制（扣款失败不计入） | `7` |

# 报告问题

将 `MzAppCenterPlatform.init()` 第三个参数传 `true`，完整地重现一次问题，然后过滤 `MzAppCenterPlatform` 后，将 log 上报给魅族。

[1]: https://github.com/MeizuAppCenter/MzAppCenterSdkDemo
[2]: https://github.com/MeizuAppCenter/MzAppCenterSdkDemo/blob/master/README_MIGRATE.md