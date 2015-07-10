package doext.implement;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;

import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.alipay.PayResult;
import doext.alipay.SignUtils;
import doext.define.do_Alipay_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_Alipay_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_Alipay_Model extends DoSingletonModule implements do_Alipay_IMethod {

	public do_Alipay_Model() throws Exception {
		super();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("pay".equals(_methodName)) {
			this.pay(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 支付；
	 * 
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void pay(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {

		String _rsaPrivate = DoJsonHelper.getString(_dictParas, "rsaPrivate", "");// 商户私钥
		if (TextUtils.isEmpty(_rsaPrivate))
			throw new Exception("rsaPrivate 不能为空");

		String _rsaPublic = DoJsonHelper.getString(_dictParas, "rsaPublic", "");// 支付宝公钥
		if (TextUtils.isEmpty(_rsaPublic))
			throw new Exception("rsaPublic 不能为空");

		String _partner = DoJsonHelper.getString(_dictParas, "partner", "");// 合作者身份ID,签约的支付宝账号对应的支付宝唯一用户号。以2088开头的16位纯数字组成
		if (TextUtils.isEmpty(_partner))
			throw new Exception("partner 不能为空");

		String _notifyUrl = DoJsonHelper.getString(_dictParas, "notifyUrl", "");// 服务器异步通知页面路径,支付宝服务器把处理结果返回该url，长度不能超过200个字符
		if (TextUtils.isEmpty(_notifyUrl))
			throw new Exception("notifyUrl 不能为空");

		String _tradeNo = DoJsonHelper.getString(_dictParas, "tradeNo", "");// 支付宝合作商户网站唯一订单号,长度不能超过64个字符
		if (TextUtils.isEmpty(_tradeNo))
			throw new Exception("tradeNo 不能为空");

		String _subject = DoJsonHelper.getString(_dictParas, "subject", "");// 商品的标题/交易标题/订单标题/订单关键字等,长度不能超过128个字符
		if (TextUtils.isEmpty(_subject))
			throw new Exception("subject 不能为空");

		String _sellerId = DoJsonHelper.getString(_dictParas, "sellerId", "");// 卖家支付宝账号（邮箱或手机号码格式）或其对应的支付宝唯一用户号,长度不能超过16个字符
		if (TextUtils.isEmpty(_sellerId))
			throw new Exception("sellerId 不能为空");

		String _totalFee = DoJsonHelper.getString(_dictParas, "totalFee", "");// 该笔订单的资金总额，单位为RMB-Yuan。取值范围为[0.01，100000000.00]，精确到小数点后两位
		if (TextUtils.isEmpty(_totalFee))
			throw new Exception("totalFee 不能为空");

		String _body = DoJsonHelper.getString(_dictParas, "body", "");// 对一笔交易的具体描述信息。如果是多种商品，请将商品描述字符串累加传给body,长度不能超过512个字符
		if (TextUtils.isEmpty(_body))
			throw new Exception("body 不能为空");

		final Activity _activity = DoServiceContainer.getPageViewFactory().getAppContext();
		this.scriptEngine = _scriptEngine;
		this.callbackFuncName = _callbackFuncName;
		String _timeOut = DoJsonHelper.getString(_dictParas, "timeOut", "15d");// 设置未付款交易的超时时间，一旦超时，该笔交易就会自动被关闭。\\n当用户输入支付密码、点击确认付款后（即创建支付宝交易后）开始计时。\\n取值范围：1m～15d，或者使用绝对时间（示例格式：2014-06-13
		// 订单
		String _orderInfo = getOrderInfo(_partner, _notifyUrl, _tradeNo, _subject, _sellerId, _totalFee, _body, _timeOut);

		// 对订单做RSA 签名
		String _sign = SignUtils.sign(_orderInfo, _rsaPrivate);
		// 仅需对sign 做URL编码
		_sign = URLEncoder.encode(_sign, "UTF-8");

		// 完整的符合支付宝参数规范的订单信息
		final String payInfo = _orderInfo + "&sign=\"" + _sign + "\"&" + getSignType();
		Runnable payRunnable = new Runnable() {
			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(_activity);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo);
				Message msg = new Message();
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};
		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			PayResult payResult = new PayResult((String) msg.obj);
			// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
			DoInvokeResult _invokeResult = new DoInvokeResult(do_Alipay_Model.this.getUniqueKey());
			try {
				JSONObject _result = new JSONObject();
				_result.put("code", payResult.getResultStatus());
				_result.put("msg", payResult.getResult() + payResult.getMemo());
			} catch (Exception e) {
				DoServiceContainer.getLogEngine().writeError("do_Alipay_Model pay \n\t", e);
			} finally {
				scriptEngine.callback(callbackFuncName, _invokeResult);
			}
		};
	};

	// 商户私钥，pkcs8格式
//	public static final String RSA_PRIVATE = "";
	// 支付宝公钥
//	public static final String RSA_PUBLIC = "";

	private DoIScriptEngine scriptEngine;
	private String callbackFuncName;

	/**
	 * get the sign type we use. 获取签名方式
	 * 
	 */
	public String getSignType() {
		return "sign_type=\"RSA\"";
	}

	private String getOrderInfo(String _partner, String _notifyUrl, String _tradeNo, String _subject, String _sellerId, String _totalFee, String _body, String _timeOut) {

		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + _partner + "\"";
		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + _sellerId + "\"";
		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + _tradeNo + "\"";
		// 商品名称
		orderInfo += "&subject=" + "\"" + _subject + "\"";
		// 商品详情
		orderInfo += "&body=" + "\"" + _body + "\"";
		// 商品金额
		orderInfo += "&total_fee=" + "\"" + _totalFee + "\"";
		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + _notifyUrl + "\"";
		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";
		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";
		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";
		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=" + "\"" + _timeOut + "\"";
		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";
		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
//		orderInfo += "&return_url=\"m.alipay.com\"";
		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;

	}
}