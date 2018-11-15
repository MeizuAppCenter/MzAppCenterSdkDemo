# MzAppCenterSdkDemo
魅族联运 SDK 接入 Demo。演示了如何初始化 SDK 与发起支付请求。

特别注意：

 1. Demo 不支持在 Android 8.0 手机运行。但因 Flyme 系统没有 Android 8.0 的版本，因此您无需担心。
 2. **Demo 中的 libs 不会随时保持更新，请不要直接拷贝使用！请不要直接拷贝使用！请不要直接拷贝使用！
    要获取最新的 SDK，请转至[官方文档][1]查看。**

# 时序图

![](static/Timing_diagram.png)

# SDK 下载

[下载地址][1]

# 接入说明

1.在项目 `app` 模块下新建 `libs/meizu` 目录，将下述文件拷贝至该目录

 - colortheme-crimson-2.0.170614.aar
 - flyme-appcompat-2.0.170614.aar
 - IndPayProcess-release.aar
 - meizu-common-2.0.170614.aar
 - MzAppCenterSdk_X.X.X(Build_X.X.X).aar
 - MzUsageStats-3.2.0.aar
 - res-meizu-common-2.0.170614.aar

2.打开 `app` 的 `build.gradle`，在根节点声明：

``` groovy
repositories {
    flatDir {
        dirs 'libs/meizu'
    }
}
```
接着在 `dependencies{}` 闭包内添加如下声明：
``` groovy
//递归 'libs/meizu` 下所有的 aar 并引用
def meizuLibs = project.file('libs/meizu')
meizuLibs.traverse(nameFilter: ~/.*\.aar/) { file ->
    def name = file.getName().replace('.aar', '')
    implementation(name: name, ext: 'aar')
}

//以下第三方库为 SDK 内部引用，即使您的应用没有用到，也必须声明在此；
//相反，如果您的应用已经在使用，则可保留您自己的版本，不必再次声明
implementation "com.android.support:appcompat-v7:25.3.1"
implementation "com.google.code.gson:gson:2.5"
implementation "com.squareup.picasso:picasso:2.5.2"
implementation "com.squareup.retrofit2:retrofit:2.4.0"
implementation "com.squareup.retrofit2:converter-gson:2.4.0"
```
3.在应用的 `Application` 类中初始化 SDK:

``` kotlin
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MzAppCenterPlatform.init(this, "your_appKey")
    }
}
```
`MzAppCenterPlatform.init()` 方法的参数说明如下：

| 参数名 | 类型 | 是否必填 | 说明 |
| ------ | ------ | ------ | ------ |
| application | Application | 是 | 应用的 Application |
| appKey | String | 是 | 您在魅族开放平台申请签约时，魅族提供给您的一串值 |
| debug | Boolean | 否 | SDK 内部 Log 开关，建议传 `<您的应用包名>.BuildConfig.Debug` |

4.调用接口发起支付请求：
``` kotlin
// Android 6.0 开始需要请求运行时权限
// 请确保您的应用在调用支付接口前，已经拥有“android.Manifest.permission.READ_PHONE_STATE”权限
if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_PHONE_STATE)
         == PackageManager.PERMISSION_GRANTED) {
      //权限正常，调用 SDK 接口发起支付请求
      MzAppCenterPlatform.getInstance()?.pay(activity, payInfo, listener)
} else {
     //如果没有权限，调用系统框架去请求用户授权
     ActivityCompat.requestPermissions(this,
     arrayOf(android.Manifest.permission.READ_PHONE_STATE), MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
}

//如果你在上面调用了 ActivityCompat.requestPermissions()，则需要重写此方法获得用户响应的结果
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                        grantResults: IntArray) {
    when (requestCode) {
        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE -> {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //太好了，用户终于授权了，调用 SDK 接口发起支付请求
                MzAppCenterPlatform.getInstance()?.pay(activity, payInfo, listener)
            } else {
                //用户不授予“android.Manifest.permission.READ_PHONE_STATE”权限，无法完成支付
            }
       }
       else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
   }
}
```

`MzAppCenterPlatform.getInstance()?.pay()` 方法的参数说明如下：

| 参数名 | 类型 | 说明 |
| ------ | ------ | ------ |
| activity | Activity |调用支付接口的页面 |
| payInfo | PayInfo | 支付信息 |
| listener | IPayResultListener | 支付结果回调 |

`PayInfo` 为支付信息，包含订单标题、扣款金额等信息，由您构造后传入，具体说明如下：

| 参数名 | 类型 | 是否必填 | 说明 |
| ------ | ------ | ------ | ------ |
| createTime | Long | 是 | cp订单创建时间 |
| tradeNo | String | 是 | cp订单号 |
| productId | String | 是 | 商品ID |
| productName | String | 是 | 商品名称 |
| productBody | String | 是 | 商品详情 |
| productUnit | String | 是 | 商品单位 |
| buyAmount | Integer | 是 | 购买数量 |
| perPrice | Double | 是 | 商品单价 |
| totalFee | Double | 是 | 购买总价 |
| attach | String | 否 | CP自定义信息 |

`IPayResultListener` 为支付结果回调，具体说明如下：
``` kotlin
override fun onSuccess() {
     //支付成功
}

override fun onFailed(code: Int, message: String) {
	//支付失败
}
```
`onFailed()` 中的 `code` 与 `message` 被定义在 `com.meizu.mstore.sdk.pay.PayResult`，您可以根据如下说明，结合实际情况给予用户相应引导：

| code | message | 建议操作 |
| ------ | ------ | ------ |
| -1 | 无法连接网络，请检查网络设置 | 引导用户检查网络设置 |
| -2 | 用户主动取消支付 | 引导用户重新发起支付 |
| -3 | 获取预支付订单失败 | 检查是否已与魅族签约<br>检查 SDK 初始化时传入的 `appKey` 是否正确<br>检查填写在魅族开放平台的`应用签名`是否由当前应用的签名生成<br>检查 `PayInfo` 构造是否正确<br>查看是否混淆导致<br>查看编译时控制台输出信息是否有异常 |
| -4 | 支付 SDK 检查订单签名失败 | 检查 SDK 初始化时传入的 `appKey` 是否正确<br>检查 `PayInfo` 构造是否正确<br>查看是否混淆导致<br>查看编译时控制台输出信息是否有异常 |
| -5 | 无法读取手机状态信息 | SDK 在处理支付请求时需要获取手机 IMEI 等信息，[引导用户授予][4] `android.permission.READ_PHONE_STATE` 权限|
| 其它 | 其它未知错误 | 联系魅族技术支持 |


### ProGuard
1.请先确保您的应用已经引用了 [Android SDK 默认的混淆规则](https://developer.android.com/studio/build/shrink-code#shrink-code)，这也是 Android 开发规范：
``` proguard
    proguardFiles getDefaultProguardFile('proguard-android.txt')
```
2.其它混淆相关的配置文件已经包含在了 `aar` 中，您无需再关心 SDK 混淆问题。

# 常见问题

> 编译报错`uses-sdk:minSdkVersion 14 cannot be smaller than version 19 declared in library [:MzAppCenterSdk_1.0.0(Build_201808301651):]`

解决办法：请查看 `MzAppCenterSdk_X.X.X`后面的版本号，确保接入 `1.0.1` 或以上版本。

> 运行报错`java.lang.NoClassDefFoundError:Failed resolution of: Lkotlin/jvm/internal/Intrinsics`

解决办法：请确保编译时添加了 `Kotlin` 插件，请参考[第一节《环境准备》中第 2 点描述](#环境准备)。

> 运行报错`you need to use a theme.appcompat theme (or descendant) with this activity. material`

解决办法：

* 方法一（推荐）

检查 `AndroidManifestx.xml` 的 `Applicaiton`节点，确保声明了 `android:theme="@style/AppTheme"`,并且 `AppTheme` 继承自 `Theme.Appcompat`，比如 `<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">`。可以通过新建一个全新的 AS 工程，或者参考[官方文档](https://developer.android.com/guide/topics/ui/look-and-feel/themes#Theme)来规范您的工程结构。

* 方法二（侵入式，不推荐）
1. 打开您 app 模块的 `res/values/styles.xml`，添加如下声明：

```xml

    <style name="Theme.AppCompat.Translucent">
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowBackground">@android:color/transparent</item>
        <item name="android:colorBackgroundCacheHint">@null</item>
        <item name="android:windowIsTranslucent">true</item>
        <item name="android:windowAnimationStyle">@android:style/Animation</item>
        <item name="android:windowContentOverlay">@null</item>
        <item name="android:windowTranslucentStatus" tools:ignore="NewApi">true</item>
        <item name="android:actionBarStyle">@null</item>
        <item name="android:backgroundDimEnabled">true</item>
        <item name="android:backgroundDimAmount">0.6</item>
    </style>
```

2. 打开您 app 模块的 `AndroidManifest.xml`，添加如下声明：

```xml
<activity
            android:name="com.meizu.flyme.indpay.process.pay.activity.IndPayActivity"
            android:theme="@style/Theme.AppCompat.Translucent"
            tools:replace="android:theme" />
```

> 编译时提示 `utdid` 库冲突

解决办法：您的项目是否引用了`友盟统计 SDK` 或 `支付宝 SDK`？请删除其中一个 `utdid4all-X.X.X.jar`。

> 编译时提示 `okhttp` 或 `okio` 等库冲突

解决办法：您的项目是否已经接入了`支付宝 SDK` 或有其它 SDK 引用了这些库？请使用 `gradle` 尝试 `exclude{}` 掉它们或移除。

  [1]: http://open-wiki.flyme.cn/doc-wiki/index#id?118
  