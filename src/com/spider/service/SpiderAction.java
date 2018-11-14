package com.spider.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import com.spider.utils.FTPUtils;
class StreamDrainer implements Runnable {
    private InputStream ins;

    public StreamDrainer(InputStream ins) {
        this.ins = ins;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(ins,"gb2312"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
@WebServlet("/Spider")
public class SpiderAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	static String fileName = "";
	 public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{//time，爬虫爬取日期，name,文件名
		//调用爬虫，并返回状态,爬虫存放路径根据实际情况修改，注意\\方向和在资源管理器中方向一样
		
		String time = request.getParameter("ChoiceTime");
		// /home/louyu/info/a.xls
		String name = "施工许可-"+time+".xls";
		String local_url="/home/louyu/info/"+name;
		fileName = local_url;
    	int state=call_spider(time,local_url);
    	//info为返回信息，判断是否ftp文件上传成功。
    	String info = "failure";
    	if(state==0){
			try {	
				FileInputStream fis = new FileInputStream(new File(local_url));
				//参数顺序为ip,端口，用户名，密码，文件夹名称（直接在当前用户ftp权限的目录下创建），文件名，传入文件流
				//boolean results = FTPUtils.storeFile("132.95.12.34", 21, "admin", "admin", "dd", name, fis);				
				boolean results = FTPUtils.storeFile("132.95.12.34", 21, "test1", "test1", "测试", name, fis);
				System.out.println(fileName);
				if(results){
					info = "success";
					System.out.println(info);					
				}else{
					System.out.println("错误信息:"+info);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    		
    	}
	}
	 public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			// TODO Auto-generated method stub
			doGet(request, response);
		}
	 
	
	public static void writeToFile(String info,String filename) throws IOException {
		
		File file =new File(filename);
		Writer out =new FileWriter(file);
		out.write(info);
		out.close();
	}

	public static int call_spider(String date ,String filename){

		    //爬虫执行命令，写入脚本文件
            String info = "cd /home/louyu/ && source env/bin/activate && cd spider/tutorial && scrapy crawl build -a date="+date +" -a filename="+filename;
	       //脚本文件
	        String shellname = "/home/louyu/shell.sh";
	        //执行脚本文件的命令
            String call="/bin/sh " +shellname;
          
            System.out.println("call：" + call);
        	Runtime runtime = Runtime.getRuntime();
    	
		try {
			//将执行的命令写入脚本文件
			writeToFile(info,shellname);
			Process process = runtime.exec(call);
			
			new Thread(new StreamDrainer(process.getInputStream())).start();
			new Thread(new StreamDrainer(process.getErrorStream())).start();
	    	process.getOutputStream().close();
			int exitValue = process.waitFor();//0运行正确
			System.out.println("返回值：" + exitValue);
			return exitValue;
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		   e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return 1;
	}

	
}
