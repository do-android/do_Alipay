package doext.app;
import android.content.Context;
import core.interfaces.DoIAppDelegate;

/**
 * APP启动的时候会执行onCreate方法；
 *
 */
public class do_Alipay_App implements DoIAppDelegate {

	private static do_Alipay_App instance;
	
	private do_Alipay_App(){
		
	}
	
	public static do_Alipay_App getInstance() {
		if(instance == null){
			instance = new do_Alipay_App();
		}
		return instance;
	}
	
	@Override
	public void onCreate(Context context) {
		// ...do something
	}
	
	public String getModuleTypeID() {
		return "do_Alipay";
	}

	@Override
	public String getTypeID() {
		return getModuleTypeID();
	}
}
