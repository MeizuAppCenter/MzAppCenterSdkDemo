# MzAppCenterSdkDemo
魅族联运 SDK 接入 Demo。演示了如何初始化 SDK 与发起支付请求。

特别注意：

 1. 如您在开发调试阶段使用非魅族手机，并且系统版本为 Android O，请确保其版本为 Android 8.1，Demo 暂不支持在 Android 8.0 运行。
 2. **Demo 中的 libs 不会随时保持更新，请不要直接拷贝使用！请不要直接拷贝使用！请不要直接拷贝使用！**
    请转至 [Release][1] 下载最新版本的 SDK。

# 环境准备

1.在接入前，您需要按照[《应用联运服务接入指南》](http://open-wiki.flyme.cn/doc-wiki/index#id?119)完成`新建联运版本`的工作。

2.在`新建联运版本`时，您需要提供您应用**最终上架的生产环境版本**的签名信息，请访问[《应用联运SDK接入说明》][2]，使用文档中提到的 `MzSignfetcher` 工具获取它。

# 技术准备

1.魅族应用中心联运 SDK 目前只适用于 `Android Studio` 工程结构的项目。

2.魅族应用中心联运 SDK 目前不要求登录魅族账号，亦不要求魅族手机。

3.您的项目需要[添加 Kotlin 支持](https://developer.android.com/studio/projects/add-kotlin)。过程非常简单，也可以使用 Andriod Studio -> Tools -> Kotlin -> Configure Kotlin in project，Android Studio 会自动帮助项目添加依赖插件。请放心，这不会影响现有项目以及 APK 大小，如果您还不熟悉 `Kotlin`，可以继续使用 `Java` 编写代码。但不管用何种语言开发，该插件必须添加以顺利通过编译。

4.【重要】因为联运 SDK 自身已经包含了支付宝、微信、银联渠道的支付 SDK，如果您的项目之前单独接入了这些支付方式或其它类型的支付 SDK，请先将它们**移除以免编译时提示冲突**。

# 关于 API 29

为了确保支付安全，联运 SDK 目前依赖了 `android.Manifest.permission.READ_PHONE_STATE` 权限来生成设备安全指纹，用作唯一性校验。但从 API 29 开始，Google 严格控制了此权限的使用。我们已经开始新方案的改版工作，此项工作预计会在 2019 年底完成。在此之前，__请不要将您应用的 `targetSdkVersion` 设置成 `29`__。

# 时序图

![](static/Timing_diagram.png)

# 下载 SDK 与 视觉资源

[下载地址][1]

# 开始接入

1.在项目 `app` 模块下新建 `libs/meizu` 目录，将下述文件拷贝至该目录，其中 `X` 以 [Release][1] 下载到的最新版本为准。

 - colortheme-crimson-X.X.XXXXXX.aar
 - flyme-appcompat-X.X.XXXXXX.aar
 - IndPayProcess-release-X.X.X.aar
 - meizu-common-X.X.XXXXXX.aar
 - MzAppCenterSdk_X.X.X(Build_X.X.X).aar
 - res-meizu-common-X.X.XXXXXX.aar

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

// 遍历 'libs/meizu' 下的所有 `aar` 并引用
def meizuLibs = project.file('libs/meizu')
meizuLibs.traverse(nameFilter: ~/.*\.aar/) { file ->
    def name = file.getName().replace('.aar', '')
    implementation(name: name, ext: 'aar')
}

// 或者，以上也可以简写成下面这句，但老版本的 Android Gradle Plugin 可能无法识别，请按需启用
// implementation fileTree(dir: 'libs/meizu', include: ['*.aar'])

//以下第三方库为 SDK 内部引用，即使您的应用没有用到，也必须声明在此；
//相反，如果您的应用已经在使用，则可保留您自己的版本，不必再次声明
implementation "com.android.support:appcompat-v7:25.3.1"
implementation "com.google.code.gson:gson:2.5"
implementation "com.squareup.picasso:picasso:2.71828"
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
| appKey | String | 是 | 您在魅族开放平台[签约](http://open-wiki.flyme.cn/doc-wiki/index#id?119)完成后，魅族提供给您的一串值 |
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

| 参数名 | 类型 | 是否必填 | 说明 | 示例参数 |
| ------ | ------ | ------ | ------ |------ |
| createTime | Long | 是 | cp订单创建时间 | System.currentTimeMillis() |
| tradeNo | String | 是 | cp订单号 | "tradeNo" |
| productId | String | 是 | 商品ID | "productId"|
| productName | String | 是 | 商品名称 |  "productName" |
| productBody | String | 是 | 商品详情 | "productBody" |
| productUnit | String | 是 | 商品单位 | "份" |
| buyAmount | Integer | 是 | 购买数量 | 1 |
| perPrice | Double | 是 | 商品单价，单位 `元` | 0.01 |
| totalFee | Double | 是 | 购买总价，单位 `元` | 0.01 |
| attach | String | 否 | CP自定义信息 | "" |

`IPayResultListener` 为支付结果回调，具体说明如下：
``` kotlin
override fun onSuccess() {
     //支付成功
}

override fun onFailed(code: Int, message: String) {
	//支付失败
}
```
**`onSuccess()` 只代表用户本地支付成功，后台需要处理以及最终确认状态。您不应该在这个回调里做任何的发货操作，因为这不完全可靠。请务必以魅族服务器回调您的支付通知 URL 为准，以此来认为用户最终支付成功。详见上方的时序图。**

`onFailed()` 中的 `code` 与 `message` 被定义在 `com.meizu.mstore.sdk.pay.PayResult`，您可以根据如下说明，结合实际情况给予用户相应引导：

| code | message | 建议操作 |
| ------ | ------ | ------ |
| `PayResult.CODE_ERROR_NETWORK_DISCONNECTED` | 无法连接网络，请检查网络设置 | 引导用户检查网络设置 |
| `PayResult.CODE_ERROR_USER_CANCEL` | 用户主动取消支付 | 告知用户取消了支付，引导用户重新发起 |
| `PayResult.CODE_ERROR_PREPAY_ORDER_ERROR` | 获取预支付订单失败 | 检查是否已与魅族签约<br>检查 SDK 初始化时传入的 `appKey` 是否正确<br>检查填写在魅族开放平台的`应用签名`是否由当前应用的签名生成<br>检查 `PayInfo` 构造是否正确<br>查看是否混淆导致<br>查看编译时控制台输出信息是否有异常 |
| `PayResult.CODE_ERROR_CHECK_SIGN_FAILED` | 支付 SDK 检查订单签名失败 | 检查 SDK 初始化时传入的 `appKey` 是否正确<br>检查 `PayInfo` 构造是否正确<br>查看是否混淆导致<br>查看编译时控制台输出信息是否有异常 |
| `PayResult.CODE_ERROR_READ_PHONE_STATE_NO_PERMISSION` | 无法读取手机状态信息 | SDK 在处理支付请求时需要获取手机 IMEI 等信息，[引导用户授予](https://developer.android.com/training/permissions/requesting) `android.permission.READ_PHONE_STATE` 权限|
| `211013` | 应用签名校验失败(应用上线后不应遇到) | 开发者后台联运参数里填写的应用签名必须用 [MzSignfetcher][2] 获取，不接受其他任何随意填写的签名串<br>检查当前应用的签名，与开发者后台预留的签名是否一致<br>检查是不是在后台预留了正式环境的签名，而调用时却使用了 [debug.keystore](https://developer.android.com/studio/publish/app-signing#debug-mode)|
| `20003` | 支付信息验签不通过 | SDK 不支持`在 A 设备生成订单，B设备付款`，如果存在这种场景，请生成一个新的订单，在构造`PayInfo`时 传给`tradeNo` |
| 其它 `211XXX` | 服务端透传的失败信息 | 查看[服务端文档](https://github.com/MeizuAppCenter/MzAppCenterSdkServerDemo#%E5%B8%B8%E8%A7%81%E9%94%99%E8%AF%AF%E7%A0%81)或联系魅族技术支持 |

## ProGuard
1.请先确保您的应用已经引用了 [Android SDK 默认的混淆规则](https://developer.android.com/studio/build/shrink-code#shrink-code)，这也是 Android 开发规范：
``` proguard
    proguardFiles getDefaultProguardFile('proguard-android.txt')
```
2.其它混淆相关的配置文件已经包含在了 `aar` 中，您无需再关心 SDK 混淆问题。

# 常见问题

* 运行报错`java.lang.NoClassDefFoundError:Failed resolution of: Lkotlin/jvm/internal/Intrinsics`

  * 解决方法：请确保编译时添加了 `Kotlin` 插件。

* 运行报错 `IndPayActivity crash，setOrientation() 方法报错`

  * 解决方法：请不要在 Android 8.0 版本运行此程序。


* 运行报错 `IndPayActivity crash, you need to use a theme.appcompat theme (or descendant) with this activity. material`

  * 方法一（推荐）

    检查 `AndroidManifestx.xml` 的 `Applicaiton`节点，确保声明了 `android:theme="@style/AppTheme"`,并且 `AppTheme` 继承自 `Theme.Appcompat`，比如 `<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">`。可以通过新建一个全新的 AS 工程，或者参考[官方文档](https://developer.android.com/guide/topics/ui/look-and-feel/themes#Theme)来规范您的工程结构。
  
  * 方法二（侵入式，不推荐）
    1. 打开您 `app` 模块的 `res/values/styles.xml`，添加如下声明：
    ```xml
        <style name="Theme.AppCompat.Translucent">
            <item name="android:windowNoTitle">true</item>
            <item name="android:windowBackground">@android:color/transparent</item>
            <item name="android:colorBackgroundCacheHint">@null</item>
            <item name="android:windowIsTranslucent">true</item>
            <item name="android:windowAnimationStyle">@android:style/Animation</item>
            <item name="android:windowContentOverlay">@null</item>
            <item name="android:windowTranslucentStatus">true</item>
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

* 编译时提示 `utdid` 库冲突

  * 请使用 [MzAppCenterSdk_1.0.3][1] 以上版本，该版本已经集成了 [剥离UTDID的支付宝SDK](https://alipay.open.taobao.com/doc2/detail.htm?treeId=54&articleId=104509&docType=1)

  * 如果升级了最新版本的 SDK 仍不生效，请检查您的项目是否引用了`友盟统计 SDK` ？请删除其中的 `utdid4all-X.X.X.jar`。参考：https://developer.umeng.com/docs/66632/detail/67131

* 引用了 `android-aspectjx` 导致支付宝闪退，提示 `java.lang.NoClassDefFoundError:Failed resolution of: Lcom/alipay/sdk/app/PayTask`
  * 解决方法：请参考如下方法将 `支付宝 SDK` 加入 `aspectjx` 的白名单：
  
    ``` groovy
        aspectjx { exclude 'com.alipay' }
    ```

    参考：https://github.com/HujiangTechnology/gradle_plugin_android_aspectjx/issues/124

* 编译时提示 `okhttp` 或 `okio` 等库冲突

  * 解决方法：您的项目是否已经接入了`支付宝 SDK` 或有其它 SDK 引用了这些库？请使用 `gradle` 尝试 `exclude{}` 掉它们或移除。

* 运行时界面显示为英文

  * 解决方法：检查您 `app` 模块的 `build.gradle`，移除 `resConfigs` 相关的配置。

* `Payinfo` 的 `totalFee` 已经变了，拉起的收银台显示价格却还是原来的

  * 解决方法：我们的订单体系，一旦`预付单`生成并且等待用户支付，价格是不允许再变化的。因此如果价格发生了变动，您需要重新 new 一个 `Payinfo` 并将 `tradeNo` 传新的，再重新调用 `pay()` 方法，同时请在自身订单系统内建立好对应关系方便后期对账。关于`预付单`的解释请参见上方时序图。

* 老的订单重试支付一直失败

  * 您在我们订单体系内的`预付单`可能过期了。请尝试重新 new 一个 `Payinfo` 并将 `tradeNo` 传新的，再重新调用 `pay()` 方法，同时请在自身订单系统内建立好对应关系方便后期对账。关于`预付单`的解释请参见上方时序图。


* 我的商品有优惠活动，价格应该怎么传？

  * 对于 `PayInfo` 的三个参数，魅族做了价格校验，即必须满足：
    > `buyAmount * perPrice = totalFee`  //数量 * 单价 = 总价
    
    因此如果您的商品有优惠活动，建议把优惠后的实际仍需扣款价格作为 `perPrice` 传入，同时请在自身订单系统内建立好对应关系方便后期对账，此时联运 SDK 仅作为收银台角色处理。

* 我的项目因为历史原因，`Android Gradle Plugin` 只能用 `2.3.X` 版本，`Kotlin` 插件要怎么接？

  * 请[参考这里](https://github.com/general-mobile/kotlin-architecture-components-notes-demo/blob/master/build.gradle)配置 `build.gradle`
  
* 用户支付完成后我该怎样查询订单最终状态？

  * 方法一
  
    客户端起一个轮询，每间隔数秒向服务端查询订单状态，确认最终成功后，执行发货操作，超时时间应当控制在 30s 以内。因为涉及钱财交易，超时后可以先报“支付成功，待确认”，不给用户造成心理负担，后续用户再次打开 app 或者查看订单的时候，再次查询订单状态并进行后续操作。
  * 方法二
  
    与客户端的 Push 结合，服务端收到支付状态更新后，给客户端 Push 一下，但要**注意验证 Push 的来源与真实性**。


  [1]: https://github.com/MeizuAppCenter/MzAppCenterSdkDemo/releases
  [2]: http://open-wiki.flyme.cn/doc-wiki/index#id?118
  
