package com.wise.wawc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.wise.data.CarData;
import com.wise.data.IllegalCity;
import com.wise.extend.AbstractSpinerAdapter;
import com.wise.extend.CarAdapter;
import com.wise.extend.SpinerPopWindow;
import com.wise.pubclas.Constant;
import com.wise.pubclas.GetSystem;
import com.wise.pubclas.NetThread;
import com.wise.pubclas.Variable;
import com.wise.sql.DBExcute;
import com.wise.sql.DBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract.Instances;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
/**
 * 我的爱车
 * @author 王庆文
 */
public class MyVehicleActivity extends Activity implements  AbstractSpinerAdapter.IOnItemSelectListener {
	
	private static final int getCarSeries = 1;
	private static final int refreshCarSeries = 2;
	private static final int getCarType = 7;
	private ImageView menu = null;
	private TextView editVehicle = null;
	private TableRow brand = null;
	private TableRow device = null;
	private TableRow insuranceCompany = null;
	private TableRow selectCity = null;
	private TableRow engineNumLayout = null;
	private TableRow vehicleNumLayout = null;
	private TableRow registerNumLayout = null;
	private TextView showInsuranceCompany;   //显示保险公司
	public static final int resultCodeInsurance = 2;   //选择保险公司的识别码
	public static final int resultCodeBrank = 3;       //选择汽车品牌的识别码
	public static final int resultCodeMaintain = 6;       //选择汽车品牌的识别码
	public static final int resultCodeDevice = 7;       //选择汽车品牌的识别码
	public static final int showCarData = 8;       //显示汽车数据
	public static final int deleteCarData = 10;       //删除汽车数据
	private static final int setCarLogo = 11;      // 动态设置汽车Logo
	private static final int getCityViolateRegulationsCode = 41;      // 获取违章城市代码
	
	
	private EditText etDialogMileage = null;   //输入里程
	private TableRow choiceMaintian = null;
	
	private TextView tvMileage = null;  //显示里程
	private TextView myVehicleBrank = null;
	private TextView tvMaintain = null;
	private EditText vehicleNumber = null;
	private EditText engineNum = null;
	private EditText frameNum = null;
	private EditText lastMaintain = null;
	private EditText vehicleRegNum = null;
	private TextView lastMaintainTime = null;
	private TextView buyTime = null;
	private TextView ivInsuranceDate = null;
	private LinearLayout buttomView = null;
	
	private GridView vehicleGridView = null;
	private CarAdapter carAdapter = null;
	AlertDialog dlg = null;
	boolean isJump = false; //false从菜单页跳转过来返回打开菜单，true从首页跳转返回关闭页面
	private boolean buttomViewIsShow = false;
	
	private int width = 0 ; 
	
	private TableRow ivCarSeries = null; //车型
	private TableRow ivCarType = null;  //车款
	private TextView tvCarSeries = null;
	private TextView tvCarType = null;
	private TextView btSaveVehicleData = null;
	private TextView btDeleteVehicle = null;
	private TextView selectCityTv = null;
	
	
	private SpinerPopWindow mSpinerPopWindow;
	
	private MyHandler myHandler = null;
	private DBHelper dBhalper = null;
	
	private ProgressDialog myDialog = null;
	
	private DBExcute dBExcute = null;
	
	private List<String> carSeriesNameList = new ArrayList<String>();
	private List<String> carSeriesIdList = new ArrayList<String>();
	
	//保险时间段
	String time1 = "";
	String time2 = "";
	private String carBrankId = "";
	private String carSeriesId = "";
	private static String carSeriesTitle = "carSeries";
	private static String carTypeTitle = "carType";
	private String brank = ""; 
	private CarData oneCarData = null;  //存储修改的车辆数据
//	private String vehicleId = "";     //存储车牌号   用户更改数据库
	private String vehicleNum = "";
	
	private int chickIndex = 0;
	private CarData newCarImage = null;
	private Bitmap imageBitmap = null;
	
	private SharedPreferences preferences = null;
	private String city_code = null;
	
	//违章城市相关
	private int engine = 0;
	private int car = 0;
	private int register = 0; 
	
	private int engineNo = 0;
	private int carNo = 0;
	private int registerNo = 0;
	private Intent illegalCityIntent;
	private IllegalCity illegalCity;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_vehicle);
		menu = (ImageView)findViewById(R.id.my_vechile_menu);
		editVehicle = (TextView) findViewById(R.id.my_vechile_edit);
		brand = (TableRow) findViewById(R.id.iv_my_vehicle_brank);
		device = (TableRow) findViewById(R.id.vehicle_device_layout);
		selectCity = (TableRow) findViewById(R.id.select_city_layout);
		insuranceCompany = (TableRow)findViewById(R.id.insurance_company_layout);
		showInsuranceCompany = (TextView) findViewById(R.id.show_insurance_company);
		tvMileage = (TextView) findViewById(R.id.my_vehicle_mileage);
		myVehicleBrank = (TextView) findViewById(R.id.tv_my_vehicle_beank);
		choiceMaintian = (TableRow) findViewById(R.id.choice_maintain_image_layout);
		tvMaintain = (TextView) findViewById(R.id.show_maintain);
		vehicleNumber = (EditText)findViewById(R.id.my_vehicle_ed_vehicle_number);
		engineNum = (EditText)findViewById(R.id.my_vehicle_ed_engine_num);
		frameNum = (EditText)findViewById(R.id.my_vehilce_ed_fram_num);
		vehicleRegNum = (EditText) findViewById(R.id.my_vehilce_reg_num);
		lastMaintain = (EditText)findViewById(R.id.my_vehicle_ed_last_maintain);
		lastMaintainTime = (TextView) findViewById(R.id.my_vehicle_last_maintain_time);
		buyTime = (TextView)findViewById(R.id.my_vehicle_ed_buy_time);
		selectCityTv = (TextView) findViewById(R.id.my_vehicle_select_city);
		engineNumLayout = (TableRow) findViewById(R.id.my_vehicle_engine_num_layout);
		vehicleNumLayout = (TableRow) findViewById(R.id.my_vehicle_num_layout);
		registerNumLayout = (TableRow) findViewById(R.id.my_vehicle_register_num_layout);
		getDateView(buyTime);
		ivInsuranceDate = (TextView) findViewById(R.id.my_vehicle_tv_insurance);
		getDateView(ivInsuranceDate);
		btSaveVehicleData = (TextView) findViewById(R.id.new_vehilce_tv);
		btDeleteVehicle = (TextView) findViewById(R.id.my_vehilce_delete);
		buttomView = (LinearLayout) findViewById(R.id.my_vehicle_buttom_view);
		
		
		ivCarSeries = (TableRow) findViewById(R.id.car_series_layout);
		ivCarType = (TableRow) findViewById(R.id.car_type_layout);
		tvCarSeries = (TextView) findViewById(R.id.tv_car_series);
		tvCarType = (TextView) findViewById(R.id.tv_car_type);
		ivCarSeries.setOnClickListener(new ClickListener());
		ivCarType.setOnClickListener(new ClickListener());
		mSpinerPopWindow = new SpinerPopWindow(MyVehicleActivity.this);
		mSpinerPopWindow.setItemListener(this);
		selectCity.setOnClickListener(new ClickListener());
		
		preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		chickIndex = preferences.getInt(Constant.DefaultVehicleID, 0);
		Log.e("默认的车辆id",chickIndex + "");
		illegalCityIntent = getIntent();
		illegalCity = (IllegalCity) illegalCityIntent.getSerializableExtra("IllegalCity");
	}
	protected void onResume() {
		super.onResume();
		
		myHandler = new MyHandler();
		dBhalper = new DBHelper(MyVehicleActivity.this);
		dBExcute = new DBExcute();
		
		//车辆数据
		int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constant.ImageWidth, getResources().getDisplayMetrics());
		LayoutParams params = new LayoutParams(Variable.carDatas.size() * (px + 10),LayoutParams.WRAP_CONTENT);
		vehicleGridView = (GridView) findViewById(R.id.gv_my_vehicle);
		//汽车品牌Logo
		carAdapter = new CarAdapter(MyVehicleActivity.this,Variable.carDatas);
		vehicleGridView.setAdapter(carAdapter);
		vehicleGridView.setLayoutParams(params);
		vehicleGridView.setColumnWidth(px);
		vehicleGridView.setHorizontalSpacing(6);
		vehicleGridView.setStretchMode(GridView.NO_STRETCH);
		vehicleGridView.setNumColumns(Variable.carDatas.size());
		vehicleGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
					oneCarData = Variable.carDatas.get(arg2);
					Message msg = new Message();
					msg.obj = Variable.carDatas.get(arg2);
					msg.what = showCarData;
					myHandler.sendMessage(msg);
					for(int i = 0 ; i < Variable.carDatas.size() ; i++){
						Variable.carDatas.get(i).setCheck(false);
					}
					Variable.carDatas.get(arg2).setCheck(true);
					carAdapter.notifyDataSetChanged();
					chickIndex = arg2;
			}
		});
		//设置默认选择第一辆汽车
		for(int i = 0 ; i < Variable.carDatas.size() ; i++){
			Variable.carDatas.get(i).setCheck(false);
		}
		Variable.carDatas.get(chickIndex).setCheck(true);
		carAdapter.notifyDataSetChanged();
		
		btSaveVehicleData.setOnClickListener(new ClickListener());
		btDeleteVehicle.setOnClickListener(new ClickListener());
		choiceMaintian.setOnClickListener(new ClickListener());
		device.setOnClickListener(new ClickListener());
		menu.setOnClickListener(new ClickListener());
		editVehicle.setOnClickListener(new ClickListener());
		brand.setOnClickListener(new ClickListener());
		insuranceCompany.setOnClickListener(new ClickListener());
		
		Intent intent = getIntent();
		isJump = intent.getBooleanExtra("isJump", false);

		width = getWindowManager().getDefaultDisplay().getWidth();
		Message msg = new Message();
		msg.obj = Variable.carDatas.get(chickIndex);
		oneCarData = Variable.carDatas.get(chickIndex);
		msg.what = showCarData;
		myHandler.sendMessage(msg);
	}


	
	//点击监听
	class ClickListener implements OnClickListener{
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.my_vechile_menu:
				if(isJump){
					finish();
				}else{
					ActivityFactory.A.LeftMenu();
				}
				commitData();
				break;
			case R.id.iv_my_vehicle_brank:    //选择汽车品牌
				Variable.carDatas.remove(newCarImage);
				Intent intent = new Intent(MyVehicleActivity.this,CarBrankListActivity.class);
				intent.putExtra("code", resultCodeBrank);
				intent.putExtra("carDataIndex", chickIndex);
				startActivityForResult(intent, resultCodeBrank);
				break;
			case R.id.vehicle_device_layout:    //我的终端
				Intent intent2 = new Intent(MyVehicleActivity.this,MyDevicesActivity.class);
                intent2.putExtra("isJump", false);
                startActivityForResult(intent2, 0);
				break;
			case R.id.insurance_company_layout:  //选择保险公司
				Intent intent1 = new Intent(MyVehicleActivity.this,ChoiceInsuranceActivity.class);
				intent1.putExtra("code", resultCodeInsurance);
				startActivityForResult(intent1, resultCodeInsurance);
				break;
			case R.id.dialog_mileage_sure:  //确定同步里程
				String mileageValue = etDialogMileage.getText().toString();
				if("".equals(mileageValue.trim())){
					showToast("请输入正确的里程");
				}else{
					tvMileage.setText(mileageValue.trim() + "Km");
					dlg.cancel();
				}
				break;
			case R.id.dialog_mileage_cancle:  //取消同步里程
				dlg.cancel();
				break;
				
			case R.id.choice_maintain_image_layout:  //选择保养店
				Intent intent3 = new Intent(MyVehicleActivity.this,MaintainShopActivity.class);
				intent3.putExtra("code", resultCodeMaintain);
				intent3.putExtra("brank", brank);
				startActivityForResult(intent3, resultCodeMaintain);
				break;
			case R.id.car_series_layout:  //选择车型    根据品牌id获取
				myDialog = ProgressDialog.show(MyVehicleActivity.this, getString(R.string.dialog_title), getString(R.string.dialog_message));
				myDialog.setCancelable(true);
				if("".equals(carBrankId)){
					showToast("请选择品牌");
					myDialog.dismiss();
					return;
				}
				getCarDatas(carSeriesTitle,"base/car_series?pid=",getCarSeries,carBrankId);
				break;
			case R.id.car_type_layout:  //选择车款
				if(carSeriesNameList.size() > 0){
					mSpinerPopWindow.setWidth(width);
					mSpinerPopWindow.setHeight(300);
					mSpinerPopWindow.showAsDropDown(tvCarType);
				}else{
					showToast("请选择车型");
				}
				break;
				
			case R.id.new_vehilce_tv:
				startActivity(new Intent(MyVehicleActivity.this,NewVehicleActivity.class));
				break;
			case R.id.my_vehilce_delete:
				myDialog = ProgressDialog.show(MyVehicleActivity.this, "提示", "正在删除...");
				myDialog.setCancelable(true);
				new Thread(new NetThread.DeleteThread(myHandler, Constant.BaseUrl + "vehicle/" + Variable.carDatas.get(chickIndex).getObj_id() + "?auth_code=" + Variable.auth_code, deleteCarData)).start();
				break;
			case R.id.my_vechile_edit:
				if(!buttomViewIsShow){
					buttomView.setVisibility(View.VISIBLE);
					buttomViewIsShow = true;
				}else if(buttomViewIsShow){
					buttomView.setVisibility(View.GONE);
					buttomViewIsShow = false;
				}
				break;
			case R.id.select_city_layout:
				Intent intent6 = new Intent(MyVehicleActivity.this,IllegalCitiyActivity.class);
				intent6.putExtra("requestCode", getCityViolateRegulationsCode);
				startActivityForResult(intent6, getCityViolateRegulationsCode);
			default:
				return;
			}
		}
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == this.resultCodeInsurance){
			//设置选择的保险公司
			String insurance = (String) data.getSerializableExtra("ClickItem");
			showInsuranceCompany.setText(insurance);
		}
		//选择品牌
		if(resultCode == this.resultCodeBrank){
			brank = (String) data.getSerializableExtra("brank");
			carBrankId = (String) data.getSerializableExtra("carId");
			myVehicleBrank.setText(brank);
			Variable.carDatas.get(chickIndex).setCar_brand(brank);
			Bitmap logo = logoImageIsExist(Constant.VehicleLogoPath,(String) data.getSerializableExtra("carLogo"));
			Log.e("logo == null",(String) data.getSerializableExtra("carLogo"));
		}
		//选择保养店
		if(resultCode == this.resultCodeMaintain){
			String maintain = (String) data.getSerializableExtra("maintain");
			tvMaintain.setText(maintain);
		}
		if(resultCode == this.getCityViolateRegulationsCode){
			//  TODO
			illegalCity = (IllegalCity) data.getSerializableExtra("IllegalCity");
			if(illegalCity != null){
				Log.e("illegalCity.getEngine()",illegalCity.getEngine());
				Log.e("illegalCity.getVehiclenum()",illegalCity.getVehiclenum());
				Log.e("illegalCity.getRegist()",illegalCity.getRegist());
				
				engineNumLayout.setVisibility(View.VISIBLE);
				vehicleNumLayout.setVisibility(View.VISIBLE);
				registerNumLayout.setVisibility(View.VISIBLE);
				city_code = illegalCity.getCityCode();
				selectCityTv.setText(illegalCity.getCityName());
				if(Integer.valueOf(illegalCity.getEngine()) == 0){  //隐藏发动机
					engineNumLayout.setVisibility(View.GONE);
					Log.e("隐藏发动机号","隐藏发动机号");
					engine = 0;
				}else if(Integer.valueOf(illegalCity.getEngine()) == 1){
					engine = 1;
					engineNo = Integer.valueOf(illegalCity.getEngineno());
					if(engine == 0){
						engineNum.setHint(this.getResources().getString(R.string.all_engine_num_hint));
					}else{
						engineNum.setHint(this.getResources().getString(R.string.engine_num_hint) + engineNo + this.getResources().getString(R.string.hint_wei));
						engineNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(engineNo)});
					}
				}
				if(Integer.valueOf(illegalCity.getVehiclenum()) == 0){   //隐藏车架号
					Log.e("隐藏车架号","隐藏发车架号");
					vehicleNumLayout.setVisibility(View.GONE);
					car = 0;
				}else if(Integer.valueOf(illegalCity.getVehiclenum()) == 1){
					car = 1;
					carNo = Integer.valueOf(illegalCity.getVehiclenumno());
					if(carNo == 0){
						frameNum.setHint(this.getResources().getString(R.string.all_vehicle_num_hint));
					}else{
						frameNum.setHint(this.getResources().getString(R.string.vehicle_num_hint) + carNo + this.getResources().getString(R.string.hint_wei));
						frameNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(carNo)});
					}
				} 
				if(Integer.valueOf(illegalCity.getRegist()) == 0 ){    // 隐藏车辆登记证号
					registerNumLayout.setVisibility(View.GONE);
					Log.e("隐藏登记证号","隐藏登记证号");
					register = 0;
				}else if(Integer.valueOf(illegalCity.getRegist()) == 1){
					register = 1;
					registerNo = Integer.valueOf(illegalCity.getRegistno());
					if(registerNo == 0){
						vehicleRegNum.setHint(this.getResources().getString(R.string.all_register_num_hint));
					}else{
						vehicleRegNum.setHint(this.getResources().getString(R.string.register_num_hint) + registerNo + this.getResources().getString(R.string.hint_wei));
						vehicleRegNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(registerNo)});
					}
				}
				illegalCity = null;
			}
		}
		if(resultCode == resultCodeDevice){
		    String DeviceId = data.getStringExtra("DeviceId");
		    System.out.println("DeviceId = " + DeviceId);
		}
	}
	
	class MyHandler extends Handler{
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case getCarSeries:
				myDialog.dismiss();
				String carSeries = msg.obj.toString();
				//存到数据库 
				ContentValues values = new ContentValues();
				values.put("Title", carSeriesTitle + carBrankId);
				values.put("Content", carSeries);
				dBExcute.InsertDB(MyVehicleActivity.this, values, Constant.TB_Base);
				JSONArray jsonArray = null;
				try {
					jsonArray = new JSONArray(carSeries);
					parseJSONToList(jsonArray,msg.what);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case refreshCarSeries:
				break;
				//获取车款
			case getCarType:
				myDialog.dismiss();
				String carType = msg.obj.toString();
				ContentValues carTypeValues = new ContentValues();
				carTypeValues.put("Title", carTypeTitle + carSeriesId);
				carTypeValues.put("Content", carType);
				dBExcute.InsertDB(MyVehicleActivity.this, carTypeValues, Constant.TB_Base);
				JSONArray typeJsonArray = null;
				try {
					typeJsonArray = new JSONArray(carType);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				parseJSONToList(typeJsonArray,msg.what);
				break;
			case showCarData:
				Log.e("carData里面的数据","carData里面的数据");
				//  显示车辆数据  TODO
				CarData carData = (CarData) msg.obj;
//				if(carData.getFrame_no() == null || "".equals(carData.getFrame_no())){
//					vehicleNumLayout.setVisibility(View.GONE);
//				}else{
//					vehicleNumLayout.setVisibility(View.VISIBLE);
//				}
//				if(carData.getEngine_no() == null || "".equals(carData.getEngine_no())){
//					engineNumLayout.setVisibility(View.GONE);
//				}else{
//					engineNumLayout.setVisibility(View.VISIBLE);
//				}
//				
//				if(carData.getRegNo() == null || "".equals(carData.getRegNo())){
//					registerNumLayout.setVisibility(View.GONE);
//				}else{
//					registerNumLayout.setVisibility(View.VISIBLE);
//				}
				vehicleNum = carData.getObj_name();
				myVehicleBrank.setText(carData.getCar_brand());
				vehicleNumber.setText(carData.getObj_name());
				tvCarSeries.setText(carData.getCar_series());
				tvCarType.setText(carData.getCar_type());
				engineNum.setText(carData.getEngine_no());
				Log.e("显示数据时",carData.getObj_id()+"");
				frameNum.setText(carData.getFrame_no());
				showInsuranceCompany.setText(carData.getInsurance_company());
				ivInsuranceDate.setText(carData.getInsurance_date());
				tvMaintain.setText(carData.getMaintain_company());
				lastMaintain.setText(carData.getMaintain_last_mileage());
				buyTime.setText(carData.getBuy_date());
				vehicleRegNum.setText(carData.getRegNo());
				
				
				//获取违章城市数据
				
				
				break;
			case deleteCarData:
				JSONObject jsonObject = null;
				myDialog.dismiss();
				try {
					jsonObject = new JSONObject(msg.obj.toString());
					if(Integer.parseInt(jsonObject.getString("status_code")) == 0){
						dBExcute.DeleteDB(getApplicationContext(), Constant.TB_Vehicle, "obj_id = ?", new String[]{String.valueOf(Variable.carDatas.get(chickIndex).getObj_id())});
						Log.e("删除前" + Variable.carDatas.size() ,Variable.carDatas.size() + "");
						Variable.carDatas.remove(Variable.carDatas.get(chickIndex));
						Log.e("删除后" + Variable.carDatas.size() ,Variable.carDatas.size() + "");
						carAdapter.notifyDataSetChanged();
						if(Variable.carDatas.size() > 0){
							vehicleNum = Variable.carDatas.get(0).getObj_name();
							myVehicleBrank.setText(Variable.carDatas.get(0).getCar_brand());
							vehicleNumber.setText(Variable.carDatas.get(0).getObj_name());
							tvCarSeries.setText(Variable.carDatas.get(0).getCar_series());
							tvCarType.setText(Variable.carDatas.get(0).getCar_type());
							engineNum.setText(Variable.carDatas.get(0).getEngine_no());
							Log.e("显示数据时",Variable.carDatas.get(0).getObj_id()+"");
							frameNum.setText(Variable.carDatas.get(0).getFrame_no());
							showInsuranceCompany.setText(Variable.carDatas.get(0).getInsurance_company());
							ivInsuranceDate.setText(Variable.carDatas.get(0).getInsurance_date());
							tvMaintain.setText(Variable.carDatas.get(0).getMaintain_company());
							lastMaintain.setText(Variable.carDatas.get(0).getMaintain_last_mileage());
							buyTime.setText(Variable.carDatas.get(0).getBuy_date());
						}else{
							startActivity(new Intent(MyVehicleActivity.this,NewVehicleActivity.class));
						}
						showToast("删除成功");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				Log.e("删除车辆执行结果--->",msg.obj.toString());
				break;
				
				case setCarLogo:
					carAdapter.notifyDataSetChanged();
					break;
				case getCityViolateRegulationsCode:
					Log.e("result:",msg.obj.toString());
					break;
			default:
				return;
			}
		}
	}

	private void parseJSONToList(JSONArray jsonArray,int codeType) {
		if(codeType == getCarSeries){
			Log.e("获取车型----","获取车型");
			int jsonLength = jsonArray.length();
			if(carSeriesNameList.size() > 0){
				carSeriesNameList.clear();
			}
			if(carSeriesIdList.size() > 0){
				carSeriesIdList.clear();
			}
			for(int i = 0 ; i < jsonLength ; i ++){
				try {
					carSeriesNameList.add(jsonArray.getJSONObject(i).getString("show_name"));
					carSeriesIdList.add(jsonArray.getJSONObject(i).getString("id"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			mSpinerPopWindow.refreshData(carSeriesNameList, 0);
			mSpinerPopWindow.setWidth(width);
			mSpinerPopWindow.setHeight(300);
			mSpinerPopWindow.showAsDropDown(tvCarSeries);
			mSpinerPopWindow.setType(codeType);
		}else if(codeType == getCarType){
			Log.e("获取车款----","获取车款");
			int jsonLength = jsonArray.length();
			//解析车款数据
			if(carSeriesNameList.size() > 0){
				carSeriesNameList.clear();
			}
			for(int i = 0 ; i < jsonLength ; i ++){
				try {
					carSeriesNameList.add(jsonArray.getJSONObject(i).getString("name"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			mSpinerPopWindow.refreshData(carSeriesNameList, 0);
			mSpinerPopWindow.setWidth(width);
			mSpinerPopWindow.setHeight(300);
			mSpinerPopWindow.setType(codeType);
			mSpinerPopWindow.showAsDropDown(tvCarType);
		}
		
	}
	public void getCarDatas(String title,String url,int code,String id){
		Log.e("访问地址--->",Constant.BaseUrl + url + id);
		SQLiteDatabase read = dBhalper.getReadableDatabase();
		//查询数据库
		Cursor cursor = read.rawQuery("select * from " + Constant.TB_Base + " where Title = ?", new String[]{title + id});
		JSONArray jsonArray = null;
//		String carSeries = "";
		if(cursor.moveToFirst()){
			try {
//				carSeries = cursor.getString(cursor.getColumnIndex("Content"));
				jsonArray = new JSONArray(cursor.getString(cursor.getColumnIndex("Content")));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			myDialog.dismiss();
			parseJSONToList(jsonArray,code);
		}else{
			//请求服务器
			Log.e("请求服务器","请求服务器");
			new Thread(new NetThread.GetDataThread(myHandler, Constant.BaseUrl + url + id, code)).start();
		}
	}
	public void onItemClick(int pos, int type) {
		if(type == getCarSeries){
			String value = "";
			if (pos >= 0 && pos <= carSeriesNameList.size()){
				value = carSeriesNameList.get(pos);
				tvCarSeries.setText(value);
			}
			
			for(int i = 0 ; i < carSeriesNameList.size() ; i ++){
				if(carSeriesNameList.get(i).equals(value)){
					carSeriesId = carSeriesIdList.get(i);
				}
			}
			myDialog = ProgressDialog.show(MyVehicleActivity.this, getString(R.string.dialog_title), getString(R.string.dialog_message));
			myDialog.setCancelable(true);
			getCarDatas(carTypeTitle,"base/car_type?pid=",getCarType,carSeriesId);
			Log.e("点击车型所得","点击车型所得");
		}else if(type == getCarType){
			Log.e("点击车款所得","点击车款所得");
			String value = "";
			if (pos >= 0 && pos <= carSeriesNameList.size()){
				value = carSeriesNameList.get(pos);
				tvCarType.setText(value);
			}
		}
	}
	public Bitmap logoImageIsExist(final String imagePath,final String name){
		File filePath = new File(imagePath);
		File imageFile = new File(imagePath + name);
		if(!filePath.exists()){
			filePath.mkdir();
		}
		if(imageFile.exists()){
			//将图片读取出来 
			imageBitmap = BitmapFactory.decodeFile(imagePath + name);
			Log.e("本地存在图片","本地存在图片");
		}else{
			  new Thread(new Runnable() {
				public void run() {
					//服务器获取logo图片
					String imageUrl = Constant.ImageUrl + name;
					imageBitmap = GetSystem.getBitmapFromURL(imageUrl);
		              //存储到SD卡
		              if(imageBitmap != null){
		            	  createImage(imagePath + brank + ".jpg",imageBitmap);
		            	  Message msg = new Message();
		            	  msg.what = setCarLogo;
		            	  myHandler.sendMessage(msg);
		              }
				}
			}).start();
              Log.e("服务器的图片","服务器的图片");
		}
		return imageBitmap;
	}
	
	//向SD卡中添加图片
	public void createImage(String fileName,Bitmap bitmap){
		FileOutputStream b = null;
		try {  
            b = new FileOutputStream(fileName);  
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } finally {  
            try {  
                b.flush();  
                b.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }
	}
	
	 long waitTime = 2000;
	 long touchTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(isJump){
    		MyVehicleActivity.this.finish();
    	}else{
	        if (keyCode == KeyEvent.KEYCODE_BACK) {
	            long currentTime = System.currentTimeMillis();
	            if (touchTime == 0 || (currentTime - touchTime) >= waitTime) {
	            	showToast("再按一次退出客户端");
	                touchTime = currentTime;
	            } else {
	            	commitData();
	                finish();
	            }
	            return true;
	        }
    	}
        return super.onKeyDown(keyCode, event);
    }
    
    public void getDateView(final TextView textView){
    	switch(textView.getId()){
    	case R.id.my_vehicle_ed_buy_time:  //购车时间
    		textView.setOnClickListener(new View.OnClickListener() {  
    			public void onClick(View v) {
    				Calendar c = Calendar.getInstance();
    				new DatePickerDialog(MyVehicleActivity.this,
    						new DatePickerDialog.OnDateSetListener() {
    							public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
    								textView.setText(year + "/"+ (monthOfYear + 1) + "/" + dayOfMonth);
    							}
    						}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    			}
    	    });
    		break;
    	case R.id.my_vehicle_tv_insurance:   //保险时间段
    		textView.setOnClickListener(new View.OnClickListener() {  
    			public void onClick(View v) {
    				
    				Calendar c = Calendar.getInstance();
    				DatePickerDialog datePickerDialog = new DatePickerDialog(MyVehicleActivity.this, new DateDialogListener("start"), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    				datePickerDialog.setTitle("开始时间");
    				datePickerDialog.show();
    			}
    	    });
    		break;
    	default:
    		return;
    	}
	}
    
    class DateDialogListener implements OnDateSetListener{
    	String DataTypes = "";
    	DateDialogListener(String DataTypes){
    		this.DataTypes = DataTypes;
    	}
		public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
			if("start".equals(this.DataTypes)){
				time1 = year + "/"+ (monthOfYear + 1) + "/" + dayOfMonth;
				Calendar c = Calendar.getInstance();
				DatePickerDialog datePickerDialog = new DatePickerDialog(MyVehicleActivity.this, new DateDialogListener("end"), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
				datePickerDialog.setTitle("结束时间");
				datePickerDialog.show();
			}else if("end".equals(this.DataTypes)){
				time2 = year + "/"+ (monthOfYear + 1) + "/" + dayOfMonth;
				if(time1.equals(time2)){
					showToast("非法的时间段");
				}
				if(Integer.valueOf(time1.substring(0, 4)) > Integer.valueOf(time2.substring(0, 4))){
					showToast("非法的时间段");
					return;
				}else{
					if(Integer.valueOf(time1.substring(5, 6)) > Integer.valueOf(time2.substring(5, 6))){
						showToast("非法的时间段");
						return;
					}else{
						if(Integer.valueOf(time1.substring(7)) > Integer.valueOf(time2.substring(7))){
							showToast("非法的时间段");
							return;
						}else{
							ivInsuranceDate.setText(time1+"-"+time2);
						}
					}
				}
			}
		}
    }
    public void commitData(){
    	Editor editor = preferences.edit();
		editor.putInt(Constant.DefaultVehicleID, chickIndex);
		Variable.defaultCenter = Variable.carDatas.get(chickIndex).getObj_name();
		editor.putString(Constant.defaultCenter_key, Variable.carDatas.get(chickIndex).getObj_name());
		String str = Variable.carDatas.get(chickIndex).getObj_name();
		String str2 = str==null?"null":str;
		editor.commit();
		
		
		//输入内容校验
		if(city_code == null || "".equals(city_code)){
			Toast.makeText(MyVehicleActivity.this, "请选择违章城市", 0).show();
			return;
		}
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("obj_name", vehicleNumber.getText().toString().trim()));
        params.add(new BasicNameValuePair("car_brand", myVehicleBrank.getText().toString()));
        params.add(new BasicNameValuePair("car_series", tvCarSeries.getText().toString()));
        params.add(new BasicNameValuePair("car_type", tvCarType.getText().toString()));
        params.add(new BasicNameValuePair("vio_location", city_code));
        //违章查询城市代码  TODO
        if(engine == 0){
        	params.add(new BasicNameValuePair("engine_no", ""));
        }else if(engine == 1){
        	params.add(new BasicNameValuePair("engine_no", engineNum.getText().toString().trim()));
        }
        if(car == 0){
        	params.add(new BasicNameValuePair("frame_no", ""));
        }else if(car == 1){
        	params.add(new BasicNameValuePair("frame_no", frameNum.getText().toString().trim()));
        }
        if(register == 0){
        	 params.add(new BasicNameValuePair("reg_no", ""));
        }else if(register == 1){
        	params.add(new BasicNameValuePair("reg_no", vehicleRegNum.getText().toString().trim()));
        }
        
        params.add(new BasicNameValuePair("insurance_company", showInsuranceCompany.getText().toString()));
        params.add(new BasicNameValuePair("insurance_date", ivInsuranceDate.getText().toString()));
        params.add(new BasicNameValuePair("annual_inspect_data", "年检时间"));
        params.add(new BasicNameValuePair("maintain_company", tvMaintain.getText().toString()));
        params.add(new BasicNameValuePair("maintain_last_mileage", lastMaintain.getText().toString().trim()));
        params.add(new BasicNameValuePair("maintain_last_date", lastMaintainTime.getText().toString()));
        params.add(new BasicNameValuePair("maintain_next_mileage", "2013"));
        params.add(new BasicNameValuePair("buy_time", buyTime.getText().toString().trim()));
        
        
//        Log.e("车牌号：",vehicleNumber.getText().toString().trim());
//        Log.e("品牌：",myVehicleBrank.getText().toString());
//        Log.e("型号：",tvCarSeries.getText().toString());
//        Log.e("车款：",tvCarType.getText().toString());
//        Log.e("城市代码：","城市代码");
//        
//        Log.e("发动机号：", engineNum.getText().toString().trim());
//        Log.e("车架号：",frameNum.getText().toString().trim());
//        Log.e("登记证号：",vehicleRegNum.getText().toString().trim());
//        
//        Log.e("保险公司：",showInsuranceCompany.getText().toString());
//        Log.e("保险到期时间：",ivInsuranceDate.getText().toString());
//        Log.e("4s店：",tvMaintain.getText().toString());
//        Log.e("最后保养里程：",lastMaintain.getText().toString().trim());
//        Log.e("最后保养时间：",lastMaintainTime.getText().toString());
//        Log.e("购车时间：",buyTime.getText().toString().trim());
//		new Thread(new NetThread.postDataThread(myHandler, Constant.BaseUrl + "vehicle/" + Variable.carDatas.get(chickIndex).getObj_id() + "?auth_code=" + Variable.auth_code, params, saveVehicleData)).start();
        new Thread(new Runnable() {
			public void run() {
				HttpPost httpPost = new HttpPost(Constant.BaseUrl + "vehicle/" + Variable.carDatas.get(chickIndex).getObj_id() + "?auth_code=" + Variable.auth_code);
    			try {
    				 httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
    				 HttpClient client = new DefaultHttpClient();
    				 client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
    				 client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
    				 HttpResponse httpResponse = client.execute(httpPost);
    				 //正常请求返回数据
    				 if(httpResponse.getStatusLine().getStatusCode() == 200){
    					 String strResult = EntityUtils.toString(httpResponse.getEntity());
    					 Log.e("保存成功",strResult);
    				 }else{
    					 Log.e("失败","失败");
    				 }
    			} catch (Exception e) {
    				Log.e("失败","失败");
    				e.printStackTrace();
    			}
			}
		}).start();
		
    }
   public void showToast(String showContent){
	   Toast.makeText(getApplicationContext(), showContent, 0).show();
    }
}
