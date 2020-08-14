# 魅族联运 SDK 2.0 迁移指南

本文主要描述了魅族联运 SDK 2.0 相对于 1.0 版本的变化，并介绍了如何从老版本迁移到新版本。

# 功能变更

1. SDK 现在**仅支持**搭载 [Flyme](https://www.flyme.cn/) 系统的魅族手机，请使用魅族手机开发调试
2. SDK 现在要求用户在支付前必须登录 [Flyme 账户](https://login.flyme.cn/)，否则无法完成支付
3. SDK 新增了优惠券功能，具体使用方式请咨询魅族商务或者运营
4. SDK 体积大幅减小
5. SDK 不再需要 `android.Manifest.permission.READ_PHONE_STATE` 等敏感权限
6. SDK 不再支持银联支付渠道
7. 您不需要对服务端做任何改变，因为我们处理了兼容性

# 迁移步骤

## 删除不再需要的依赖或声明

 1.如果您的应用放置了下述形态的 `aar`，请将其全部删除，因为不再需要

 - colortheme-crimson-2.0.170614.aar
 - flyme-appcompat-2.0.170614.aar
 - IndPayProcess-release-2.1.9.aar
 - meizu-common-2.0.170614.aar
 - res-meizu-common-2.0.170614.aar
 - MzUsageStats-3.2.0.aar

2.如果您的应用没有用到下述依赖，请将其删除，因为不再需要

```groovy
implementation "com.squareup.picasso:picasso:2.71828"
```
3.如果您的应用没有用到下述权限或声明，请将其删除，因为不再需要

```xml
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />

    <activity
                android:name="com.meizu.flyme.indpay.process.pay.activity.IndPayActivity"
                android:theme="@style/Theme.AppCompat.Translucent"
                tools:replace="android:theme" />
```
## 修改调用步骤

1. 新版 SDK 不再依赖 `android.permission.READ_PHONE_STATE`权限，因此第一步不需要再 `ContextCompat.checkSelfPermission`，请留意您原先这部分的代码。
2. 在正式支付之前，请务必调用 `MzAppCenterPlatform.getInstance()?.login()`进行 `OAuth` 授权，并确保在 `onActivityResult()` 中调用 `MzAppCenterPlatform.getInstance()?.onActivityResult(requestCode, resultCode, data)` 以获得正确的逻辑处理。
3. `PayInfo` 在构造时，现在强制要求传递 `notifyUrl`。

    **注意：不要把原来的 `attach` 传到 `notifyUrl`。另外，您在接入 1.0.X 版本时已经在后台[预留的支付通知 URL](http://image.res.meizu.com/image/openapi/997fcaf004834a9bbd715d6992d84704z)，在接入 2.0.X 版本后，将以每次调用时传入的为准。但除非原来预留的这个地址真的发生变化，否则请不要随意删除或修改，因为用户手上您 App 的版本仍可能接的是 1.0.X 版本 SDK，此时仍会从此处读取支付通知 URL。**

4. 原先的`pay()`方法现更名为 `payV2()`，详见下方 API 对照。
5. 新增 `onDestroy()` 方法，请在 `Activity` 或者 `Fragment` 的 `onDestroy()` 调用以释放资源。

#  API 对照

## 新增的 API

API | 类型 | 描述 |
 -|-|-|
`login(requestCode: Int, activity: Activity, listener: ILoginResultListener)` | `Unit` | 登录 Flyme 账号，并引导用户授权获取 `token`
`onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean` | `Boolean` | 一个封装好的方法，用来配合 `login()`。<br> `true` - SDK 成功处理请求 <br> `false` - SDK 未处理请求，请判断 `requestCode` 后处理
`onDestroy()` | `Unit` | 用来释放一些 SDK 用到的资源
## 修改的 API

旧 API | 新 API |  描述 |
-|-|-|
`Payinfo(...)` |  `Payinfo(notifyUrl: String)` | `Payinfo` 现在强制要求传递 `notifyUrl`

## 移除的 API

旧 API | 新 API |
-|-|
`pay(activity: Activity, payInfo: PayInfo, listener: IPayResultListener)` | `payV2(activity: Activity, payInfo: PayInfo, listener: IPayResultListener)`

