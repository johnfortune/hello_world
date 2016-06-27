package com.zfq.app_client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.callback.Callback;

import com.zfq.app_client.R.id;

import android.R.string;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	final int X_OFFSET = 5;  //x轴（原点）起始位置偏移画图范围一点
	Timer timer = new Timer();       //一个时间控制的对象，用于控制实时的x的坐标，
    //使其递增，类似于示波器从前到后扫描
    TimerTask task = null;  //时间控制对象的一个任务

    private Paint paint = null;      //画笔
    private Paint paint1 = null;      //画笔
    private Paint paint_heart_rate = null;      //画笔
    private Paint paint_pulse_rate = null;      //画笔
    int max_width;
    int max_height;
    float count=20f;
    int stop_flag=0;
    int pulse_rate_stop_flag=0;
    int heart_rate_stop_flag=0;
   

	EditText ip;
	EditText editText;
	TextView text;
	TextView exception_text;
	TextView heart_rate_text;
	TextView pulse_rate_text;
	TextView heart_plot_0,heart_plot_1,heart_plot_2,heart_plot_3,heart_plot_4;
	
	//draw data
	SurfaceView sfv,sfv_1,sfv_2;
	SurfaceHolder sfh,sfh_1,sfh_2;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ip = (EditText) findViewById(R.id.serverip);
//		editText = (EditText) findViewById(R.id.edit);
//		text = (TextView) findViewById(R.id.text);
		exception_text = (TextView) findViewById(R.id.exception_text);
		heart_rate_text = (TextView) findViewById(R.id.heart_rate_text);
		pulse_rate_text = (TextView) findViewById(R.id.pulse_rate_text);
		heart_plot_0 = (TextView) findViewById(R.id.spo2_0_text);
//		heart_plot_1 = (TextView) findViewById(R.id.spo2_1_text);
//		heart_plot_2 = (TextView) findViewById(R.id.spo2_2_text);
//		heart_plot_3 = (TextView) findViewById(R.id.spo2_3_text);
//		heart_plot_4 = (TextView) findViewById(R.id.spo2_4_text);
		
		 /*设置波形的颜色等参数*/
	    paint = new Paint(); 
	    paint1 = new Paint();
	    paint_heart_rate=new Paint();
	    paint_pulse_rate=new Paint();
//		paint.setColor(Color.GREEN);  //画波形的颜色是绿色的，区别于坐标轴黑色
//	    paint.setStrokeWidth(3);
	    
	    paint.setColor(Color.RED);
    	paint.setStrokeWidth(6);
    	paint.setTextSize(12);
    	paint_heart_rate.setColor(Color.BLUE);
    	paint_heart_rate.setStrokeWidth(6);
    	paint_pulse_rate.setColor(Color.GREEN);
    	paint_pulse_rate.setStrokeWidth(6);
    	paint1.setColor(Color.BLACK);
    	paint1.setStrokeWidth(3);
    	paint1.setTextSize(36);
		
		
		for(int i=0;i<2;i++)
		{
			test_data[i]=30;
		}
		
		
		sfv = (SurfaceView) findViewById(R.id.SurfaceView03);
		sfh = sfv.getHolder();
		
		
		sfh.addCallback(new MyCallBack());//自动运行surfaceCreated和surfaceChanged
		
		
		sfv.setOnClickListener(new View.OnClickListener() {  
			  
            @Override  
            public void onClick(View v) {  
//                myLog("m_sv clicked");  
                // TODO Auto-generated method stub  
            }  
        });

		// 创建连结按钮的监听器
		findViewById(R.id.connectip).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				connect();

			}
		});

//		// 创建发送按钮的监听器
//		findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				send();
//			}
//		});
		
		// 创建连结按钮的监听器
		findViewById(R.id.plot).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(stop_flag==1)
				{
					stop_flag=0;
				}
				if(heart_rate_stop_flag==1)
				{
					heart_rate_stop_flag=0;
				}
				if(pulse_rate_stop_flag==1)
				{
					pulse_rate_stop_flag=0;
				}

				new DrawThread().start();
//				new DrawThread1().start();
//				new DrawThread2().start();

			}
		});
		
		findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(stop_flag==0)
				{
					stop_flag=1;
				}
				if(heart_rate_stop_flag==0)
				{
					heart_rate_stop_flag=1;
				}
				if(pulse_rate_stop_flag==0)
				{
					pulse_rate_stop_flag=1;
				}

			}
		});
	}
	
	
	class MyCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                int height) {
//            Log.i("Surface:", "Change");
        	//draw_plot();
        	drawBack(sfh);
        }
 
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        	
        	
        }
 
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i("Surface:", "Destroy");
        }
    }
	
	
	private void drawBack(SurfaceHolder holder){ 
        Canvas canvas = holder.lockCanvas(new Rect(0, 0, max_size+30, 221+222*2+330)); //锁定画布
        max_width=sfv.getWidth();
		max_height=sfv.getHeight();
        //绘制白色背景 
		//Canvas canvas = holder.lockCanvas(sfh); //锁定画布
        canvas.drawColor(Color.WHITE); 
//        heart_plot_0.setText(canvas.getHeight()+"\n");
//        heart_rate_text.setText(canvas.getWidth()+"\n");
//        heart_plot_0.setText(max_width+"\n");
        Paint p = new Paint(); 
        p.setColor(Color.BLACK); 
        p.setStrokeWidth(2);
        p.setTextSize(36);
         
        //绘制坐标轴 
//       canvas.drawLine(X_OFFSET, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2, p); //绘制X轴 前四个参数是起始坐标
//       canvas.drawLine(X_OFFSET, 20, X_OFFSET, HEIGHT, p); //绘制Y轴 前四个参数是起始坐标
        //绘制刻度标志
        canvas.drawText("200", 5, 30, p);
        canvas.drawText("120", 5, 165, p);
        canvas.drawText("0", 5, 310, p);
        canvas.drawText("160", 5, 350, p);
        canvas.drawText("80", 5, 480, p);
        canvas.drawText("0", 5, 630, p);
        canvas.drawText("160", 5, 670, p);
        canvas.drawText("80", 5, 800, p);
        canvas.drawText("0", 5, 950, p);
        //绘制刻度线
        canvas.drawLine(1, 10, 5, 10, p);
        canvas.drawLine(1, 165, 5, 165, p);
        canvas.drawLine(1, 310, 5, 310, p);
        canvas.drawLine(1, 330, 5, 330, p);
        canvas.drawLine(1, 480, 5, 480, p);
        canvas.drawLine(1, 630, 5, 630, p);
        canvas.drawLine(1, 650, 5, 650, p);
        canvas.drawLine(1, 800, 5, 800, p);
        canvas.drawLine(1, 950, 5, 950, p);
        
        //绘制边框
        canvas.drawLine(0, 0, max_size+1, 0, p);
        canvas.drawLine(0, 0, 0, 221+222*2+320, p);
        canvas.drawLine(0, 221+222*2+320, max_size+1, 221+222*2+320, p);
        canvas.drawLine(max_size+1, 0, max_size+1, 221+222*2+320, p);
        //绘制分界线
        canvas.drawLine(0, 320, max_size+1, 320, p);
        canvas.drawLine(0, 640, max_size+1, 640, p);
        
        holder.unlockCanvasAndPost(canvas);  //结束锁定 显示在屏幕上
//        holder.lockCanvas(new Rect(0,0,0,0)); //锁定局部区域，其余地方不做改变
//        holder.unlockCanvasAndPost(canvas);    
    }
 	
	//绘图线程，实时获取temp 数值即是y值
	public class DrawThread extends Thread {

		public void run() {
			// TODO Auto-generated method stub
			drawBack(sfh);    //画出背景和坐标轴
            if(task != null){ 
                task.cancel(); 
            } 
            task = new TimerTask() { //新建任务   
                @Override 
                public void run() { 
                	
                	if(stop_flag==0)
                	{
                		Canvas canvas = sfh.lockCanvas(new Rect(1, 1, max_size+5, 220+222*2+330));
                    	canvas.drawColor(Color.WHITE); 
                    	
//                    	if((heart_plot[0]/2)>canvas.getHeight())
//                    	{
//                    		heart_plot[0] = 20;
//                    	}
                    	//canvas.drawLine(X_OFFSET, heart_plot[0]/2, canvas.getWidth() ,heart_plot[0]/2, paint);
                    	//canvas.drawLine(spo_cache[0], spo_cache[1], spo_cache[2], spo_cache[3], paint);
                        //绘制刻度标志
                        canvas.drawText("200", 5, 30, paint1);
                        canvas.drawText("120", 5, 165, paint1);
                        canvas.drawText("0", 5, 310, paint1);
                        canvas.drawText("160", 5, 350, paint1);
                        canvas.drawText("80", 5, 480, paint1);
                        canvas.drawText("0", 5, 630, paint1);
                        canvas.drawText("160", 5, 670, paint1);
                        canvas.drawText("80", 5, 800, paint1);
                        canvas.drawText("0", 5, 950, paint1);
                        //绘制刻度线
                        canvas.drawLine(1, 10, 5, 10, paint1);
                        canvas.drawLine(1, 165, 5, 165, paint1);
                        canvas.drawLine(1, 310, 5, 310, paint1);
                        canvas.drawLine(1, 330, 5, 330, paint1);
                        canvas.drawLine(1, 480, 5, 480, paint1);
                        canvas.drawLine(1, 630, 5, 630, paint1);
                        canvas.drawLine(1, 650, 5, 650, paint1);
                        canvas.drawLine(1, 800, 5, 800, paint1);
                        canvas.drawLine(1, 950, 5, 950, paint1);
                                           
                        canvas.drawLine(1, 1, max_size+1, 1, paint1);
                        canvas.drawLine(1, 1, 0, 221+222*2+320, paint1);
                        canvas.drawLine(1, 221+222*2+320, max_size+1, 221+222*2+320, paint1);
                        canvas.drawLine(max_size+1, 1, max_size+1, 221+222*2+320, paint1);
                        
                        //绘制分界线
                        canvas.drawLine(0, 320, max_size+1, 320, paint1);
                        canvas.drawLine(0, 640, max_size+1, 640, paint1);
                    	
                    	
                    	canvas.drawLines(heart_rate_cache, paint_heart_rate);
                    	canvas.drawLines(spo_cache, paint);
                    	canvas.drawLines(pulse_rate_cache, paint_pulse_rate);
                    //	count+=10;
                    	sfh.unlockCanvasAndPost(canvas);
//                    	drawBack(sfh);
                	}   
                	
                } 
            }; 
            timer.schedule(task, 0,1); //隔1ms被执行一次该循环任务画出图形 
            //简单一点就是1ms画出一个点，然后依次下去
  
		}	 
	}

	Socket socket = null;
	BufferedWriter writer = null;
	BufferedReader reader = null;

	public void connect() {	
		// 网络读取字符
		AsyncTask<Void, String, Void> read = new AsyncTask<Void, String, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					socket = new Socket(ip.getText().toString(), 12345);
					writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					publishProgress("@success");
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					Toast.makeText(MainActivity.this, "无法建立连接", Toast.LENGTH_SHORT).show();
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					Toast.makeText(MainActivity.this, "无法建立连接", Toast.LENGTH_SHORT).show();
					e1.printStackTrace();
				}
				// TODO Auto-generated method stub
				try {
					String line;
					while ((line = reader.readLine())!= null) {
						
						data_process(line);
						publishProgress(line);
//						text.setText(" ");
//						text.append(Integer.toHexString(frame_length)+"\n");				
//						data_publish();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(String... values) {
				// TODO Auto-generated method stub
				if (values[0].equals("@success")) {
					Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();					
				}
				else{
					//String r_data=data_process(values[0]);
				
//					text.append("receive:"+"\n"+values[0]+"\n"+"frame_length="+Integer.toHexString(frame_length)+"\n");
//					text.append(heart_plot_data+"\n");
//					text.setText("receive:"+"\n"+values[0]+"\n"+"心率:"+"\n"+heart_rate+"\n");
//					text.setText("spo2:"+"\n"+heart_plot_data+"\n");
					exception_text.setText(exception_str+"\n");
					heart_rate_text.setText(heart_rate+"\n");
					pulse_rate_text.setText(pulse_rate+"\n");
					heart_plot_0.setText(heart_plot[0]+"\n");
//					heart_plot_1.setText(heart_rate_cache[1]+"\n");
//					heart_plot_2.setText(heart_rate_plot_num+"\n");
//					heart_plot_3.setText(heart_plot[3]+"\n");
//					heart_plot_4.setText(heart_plot[4]+"\n");
					
					//sfv.draw(test_data[2]);
					
					for(int i=0;i<2;i++)
					{
						test_data[i]=data+5;
					}
					if(data>60)
					{
						data=1;
					}				
				}
				super.onProgressUpdate(values);
			}
		};
		read.execute();

	}

//	public void send() {
//		try {
//			writer.write(editText.getText().toString());
//			writer.flush();
//			editText.setText("");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//	
	int rcv_data[];
	int frame_length;
	int flag_exception;
	String exception_str;
	int heart_rate;
	int heart_plot[]=new int[5];
	int heart_plot_num=0;
	int spo[]=new int[5];
	int pulse_rate;
	
	int max_size=980;
	
	int heart_rate_store[]=new int[max_size];
	int heart_rate_store_plot[]=new int[max_size];
	int heart_rate_plot_num=0;
	int heart_rate_insert_flag=0;
	int heart_rate_new_plot=0;
	
	int pulse_store[]=new int[max_size];
	int pulse_store_plot[]=new int[max_size];
	int pulse_plot_num=0;
	int pulse_insert_flag=0;
	int pulse_new_plot=0;
	
	
	int heart_store[]=new int[max_size];
	int heart_store_plot[]=new int[max_size];
	int insert_flag=0;
	int new_plot=0;
	String heart_plot_data;
	
	float spo_cache[]=new float[4*(max_size-1)];
	float heart_rate_cache[]=new float[4*(max_size-1)];
	float pulse_rate_cache[]=new float[4*(max_size-1)];
	
	int test_data[]=new int[2];
	int data=1;
	
	public void data_process(String values){
		if((values.equals("@success"))!= true)
		{
			rcv_data=new int[values.length()/2];
			for(int i=0;i<(values.length()/2);i++)
			{
				rcv_data[i]=Integer.parseInt(values.substring(i*2, i*2+2), 16);
			}
			frame_length=rcv_data[0];
			flag_exception=rcv_data[1];
			exception_analyse(flag_exception);
			heart_rate=rcv_data[2]*100+rcv_data[3];
//			heart_rate_plot_store();
			for(int num=0;num<5;num++)
			{
				heart_plot[num]=rcv_data[num+4];
			}
	
	
			if(frame_length>9)
			{
				for(int count=0;count<5;count++)
				{
					spo[count]=rcv_data[count+9];
				}
				pulse_rate=(0x80&rcv_data[11])*128+0x7f&rcv_data[12];
//				pulse_rate_plot_store();
			}
			
			heart_plot_store();
		}
	}
	
	public void exception_analyse(int exception_any)
	{
		exception_str="";
		if((exception_any&0x03)==0x03)
		{
			exception_str=exception_str+"间期不规律";
		}
		else if((exception_any&0x40)==0x40)
		{
			exception_str=exception_str+"心率稍低";
		}
		else if((exception_any&0x20)==0x20)
		{
			exception_str=exception_str+"心率稍高";
		}
		else if((exception_any&0x10)==0x10)
		{
			exception_str=exception_str+"心率过高";
		}
		else if((exception_any&0x08)==0x08)
		{
			exception_str=exception_str+"心率过低";
		}
		else if((exception_any&0x04)==0x04)
		{
			exception_str=exception_str+"基线漂移";
		}
		else if((exception_any&0x02)==0x02)
		{
			exception_str=exception_str+"间期增长";
		}
		else if((exception_any&0x01)==0x01)
		{
			exception_str=exception_str+"间期缩短";
		}
		else 
		{
			exception_str=exception_str+"心率正常";
		}	
	}
	
	public void heart_rate_plot_store()
	{
		heart_rate_new_plot=0;
		if(heart_rate_plot_num<max_size)
		{
			heart_rate_store[heart_rate_plot_num]=heart_rate;
			heart_rate_plot_num++;
		}
		else
		{
			heart_rate_new_plot=heart_rate_plot_num-max_size+1;
			heart_rate_plot_num=max_size;
			if(heart_rate_insert_flag>=max_size)
			{
				heart_rate_insert_flag=0;
			}
			for(int l=0;l<heart_rate_new_plot;l++)
			{
				heart_rate_store[l+heart_rate_insert_flag]=heart_rate;
			}
			heart_rate_insert_flag+=heart_rate_new_plot;
			
			int init_heart_rate_count=0;
			for (int n=heart_rate_insert_flag;n<max_size;n++)
			{			
				heart_rate_store_plot[init_heart_rate_count]=heart_rate_store[n];
				init_heart_rate_count++;
			}
			for (int n=0;n<heart_rate_insert_flag;n++)
			{
				heart_rate_store_plot[init_heart_rate_count]=heart_rate_store[n];
				init_heart_rate_count++;
			}
			
			for (int n=0;n<(max_size-1);n++)
			{
				heart_rate_cache[4*n]=n+1+222;
				if(heart_rate_store_plot[n]>200)
				{
					heart_rate_cache[4*n+1]=200+222;
				}
				else
				{
					heart_rate_cache[4*n+1]=heart_rate_store_plot[n]+222;
				}
				//spo_cache[4*n+1]=heart_store_plot[n];
				heart_rate_cache[4*n+2]=n+2+222;
				if(heart_rate_store_plot[n+1]>200)
				{
					heart_rate_cache[4*n+3]=200+222;
				}
				else
				{
					heart_rate_cache[4*n+3]=heart_rate_store_plot[n]+222;
				}
//				spo_cache[4*n+3]=heart_store_plot[n+1];
			}
		}
	}
	
	public void pulse_rate_plot_store()
	{
		pulse_new_plot=0;
		if(pulse_plot_num<max_size)
		{
			pulse_store[pulse_plot_num]=pulse_rate;
			pulse_plot_num++;
		}
		else
		{
			pulse_new_plot=pulse_plot_num-max_size+1;
			pulse_plot_num=max_size;
			if(pulse_insert_flag>=max_size)
			{
				pulse_insert_flag=0;
			}
			for(int l=0;l<pulse_new_plot;l++)
			{
				pulse_store[l+pulse_insert_flag]=pulse_rate;
			}
			pulse_insert_flag+=pulse_new_plot;
			
			int init_pulse_count=0;
			for (int n=pulse_insert_flag;n<max_size;n++)
			{			
				pulse_store_plot[init_pulse_count]=pulse_store[n];
				init_pulse_count++;
			}
			for (int n=0;n<pulse_insert_flag;n++)
			{
				pulse_store_plot[init_pulse_count]=pulse_store[n];
				init_pulse_count++;
			}
			
			for (int n=0;n<(max_size-1);n++)
			{
				pulse_rate_cache[4*n]=n+1+222*2;
				if(pulse_store_plot[n]>200)
				{
					pulse_rate_cache[4*n+1]=200+222*2;
				}
				else
				{
					pulse_rate_cache[4*n+1]=pulse_store_plot[n]+222*2;
				}
				//spo_cache[4*n+1]=heart_store_plot[n];
				pulse_rate_cache[4*n+2]=n+2+222*2;
				if(pulse_store_plot[n+1]>200)
				{
					pulse_rate_cache[4*n+3]=200+222*2;
				}
				else
				{
					pulse_rate_cache[4*n+3]=pulse_store_plot[n]+222*2;
				}
//				spo_cache[4*n+3]=heart_store_plot[n+1];
			}
		}
	}
	
	public void heart_plot_store()
	{
		new_plot=0;
		if(heart_plot_num<max_size)
		{
			for(int l=0;l<5;l++)
			{
				heart_store[heart_plot_num+l]=heart_plot[l];
				heart_rate_store[heart_plot_num+l]=heart_rate;
				pulse_store[heart_plot_num+l]=pulse_rate;
			}
			heart_plot_num+=5;
		}
		else
		{
			
			new_plot=heart_plot_num-max_size+5;
			heart_plot_num=max_size;
			if(insert_flag>=max_size)
			{
				insert_flag=0;
				
			}
			for(int l=0;l<new_plot;l++)
			{
				heart_store[l+insert_flag]=heart_plot[l];
				heart_rate_store[l+insert_flag]=heart_rate;
				pulse_store[l+insert_flag]=pulse_rate;
			}
			
			insert_flag+=new_plot;
			
			//convert to string
			heart_plot_data="";
			int init_count=0;
			for (int n=insert_flag;n<max_size;n++)
			{
				heart_plot_data=heart_plot_data+Integer.toHexString(heart_store[n]);
				heart_store_plot[init_count]=heart_store[n];
				heart_rate_store_plot[init_count]=heart_rate_store[n];
				pulse_store_plot[init_count]=pulse_store[n];
				init_count++;
			}
			for (int n=0;n<insert_flag;n++)
			{
				heart_plot_data=heart_plot_data+Integer.toHexString(heart_store[n]);
				heart_store_plot[init_count]=heart_store[n];
				heart_rate_store_plot[init_count]=heart_rate_store[n];
				pulse_store_plot[init_count]=pulse_store[n];
				init_count++;
			}
			//System.out.println(heart_plot_data+"\n");
			
			for (int n=0;n<(max_size-1);n++)
			{
				spo_cache[4*n]=n+1;
				if(heart_store_plot[n]>121)
				{
					spo_cache[4*n+1]=160-(heart_store_plot[n]-121)*6;
				}
				else
				{
					spo_cache[4*n+1]=160+(121-heart_store_plot[n])*6;
				}
				//spo_cache[4*n+1]=heart_store_plot[n];
				spo_cache[4*n+2]=n+2;
				if(heart_store_plot[n+1]>121)
				{
					spo_cache[4*n+3]=160-(heart_store_plot[n+1]-121)*6;
				}
				else
				{
					spo_cache[4*n+3]=160+(121-heart_store_plot[n+1])*6;
				}
				
				heart_rate_cache[4*n]=n+1;
				if(heart_rate_store_plot[n]>200)
				{
					heart_rate_cache[4*n+1]=330;//200+222;
				}
				else
				{
					heart_rate_cache[4*n+1]=480-(heart_rate_store_plot[n]-80)*4;//(heart_rate_store_plot[n]-80)*2+222+122;
					if(heart_rate_cache[4*n+1]<330)
					{
						heart_rate_cache[4*n+1]=330;
					}
				}
				//spo_cache[4*n+1]=heart_store_plot[n];
				heart_rate_cache[4*n+2]=n+2;
				if(heart_rate_store_plot[n+1]>200)
				{
					heart_rate_cache[4*n+3]=330;//200+222;
				}
				else
				{
					heart_rate_cache[4*n+3]=480-(heart_rate_store_plot[n+1]-80)*4;//(heart_rate_store_plot[n]-80)*2+222+122;
					if(heart_rate_cache[4*n+3]<330)
					{
						heart_rate_cache[4*n+3]=330;
					}
				}
				
				pulse_rate_cache[4*n]=n+1;
				if(pulse_store_plot[n]>200)
				{
					pulse_rate_cache[4*n+1]=650;//200+222*2;
				}
				else
				{
					pulse_rate_cache[4*n+1]=800-(pulse_store_plot[n]-80)*4;//pulse_store_plot[n]+222*2;
					if(pulse_rate_cache[4*n+1]<650)
					{
						pulse_rate_cache[4*n+1]=650;
					}
				}
				//spo_cache[4*n+1]=heart_store_plot[n];
				pulse_rate_cache[4*n+2]=n+2;
				if(pulse_store_plot[n+1]>200)
				{
					pulse_rate_cache[4*n+3]=650;//200+222*2;
				}
				else
				{
					pulse_rate_cache[4*n+3]=800-(pulse_store_plot[n+1]-80)*4;//pulse_store_plot[n]+222*2;
					if(pulse_rate_cache[4*n+3]<650)
					{
						pulse_rate_cache[4*n+3]=650;
					}
				}
			}
		}
	}
}
