package com.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

import android.text.TextUtils;
import android.util.Log;
public class HttpGetProxy {  
    final static private String TAG = "HttpGetProxy";  
    final static private String LOCAL_IP_ADDRESS_1 = "127.0.0.1";
    final static private String LOCAL_IP_ADDRESS_2 = "10.0.2.2";
    final static private int HTTP_PORT = 80;
    final static private String HTTP_END="\r\n\r\n";
    
    /**代理服务器使用的端口*/
    private int proxy_ip_port;
    /**链接带的端口*/
    private String original_ip_port;
    /**远程服务器地址*/
    private String remoteHost;
    /**本地服务器地址*/
    private String localHost;
    private ServerSocket localServer = null;  
    /**收发Media Player请求的Socket*/
    private Socket sckPlayer = null;
    /**收发Media Server请求的Socket*/
    private Socket sckServer = null;
  
    private SocketAddress address;  
   
    /** 
     * 初始化代理服务器 
     * @param localport 代理服务器监听的端口 
     */  
    public HttpGetProxy(int localport) {  
    	try {
			_HttpGetProxy(LOCAL_IP_ADDRESS_1,localport);
		} catch (Exception e) {
			Log.e(TAG,LOCAL_IP_ADDRESS_1+"???"+e.toString());
			try {
				_HttpGetProxy(LOCAL_IP_ADDRESS_2,localport);
			}catch (Exception e1) {
				Log.e(TAG,LOCAL_IP_ADDRESS_2+"???"+e.toString());
				System.exit(0);
			}
		}
    }
    
    private void _HttpGetProxy(String ipAddress,int localport) throws UnknownHostException, IOException {  
    	proxy_ip_port=localport;  
        localServer = new ServerSocket(localport,1,InetAddress.getByName(ipAddress));
        localHost=ipAddress;
    }
   
    /** 
     * 把网络URL转为本地URL，127.0.0.1替换网络域名 
     * @param url 网络URL  
     * @return 本地URL 
     */  
    public String getLocalURL(String urlString){
    	//----排除HTTP特殊----//
    	String targetUrl=ProxyUtils.getRedirectUrl(urlString);
    	//----获取对应本地代理服务器的链接----//
        String result = null;
        URI originalURI=URI.create(targetUrl);  
        remoteHost=originalURI.getHost();  
        if(originalURI.getPort()!=-1){//URL带Port
            address = new InetSocketAddress(remoteHost,originalURI.getPort());//使用默认端口  
            original_ip_port = originalURI.getPort()+"";//保存端口，中转时替换
            result=targetUrl.replace(remoteHost+":"+originalURI.getPort(),  
            		localHost+":"+proxy_ip_port);  
        }  
        else{//URL不带Port  
            address = new InetSocketAddress(remoteHost,HTTP_PORT);//使用80端口 
            original_ip_port = "";
            result=targetUrl.replace(remoteHost,localHost+":"+proxy_ip_port);  
        }
        return result;     
    }  
    
    /** 
     * 启动代理服务器 
     * @throws IOException 
     */  
    public void asynStartProxy(){  
        new Thread() {  
            public void run() {
                int bytes_read;  
                byte[] local_request = new byte[1024];  
                byte[] remote_reply = new byte[1024];  
                while (true) {  
                    try {  
                        //--------------------------------------  
                        //监听MediaPlayer的请求，MediaPlayer->代理服务器  
                        //--------------------------------------  
                        sckPlayer = localServer.accept();  
                        Log.e(TAG, "..........sckPlayer connected..........");   
                        
                        String requestStr = "";
                        while ((bytes_read = sckPlayer.getInputStream().read(local_request)) != -1) {
                            byte[] tmpBuffer=new byte[bytes_read]; 
                            System.arraycopy(local_request,0,tmpBuffer,0,bytes_read);
                            String str = new String(tmpBuffer);
                            //Log.e("from MediaPlayer---->", str);  
                            requestStr = requestStr + str;  
                            if (requestStr.contains("GET")  
                                    && requestStr.contains(HTTP_END)) {  
                                break;
                            }   
                        }
                       
                        //把request中的本地ip改为远程ip
                        requestStr = requestStr.replace(localHost,remoteHost);
                        //改 use-agent
                        requestStr = requestStr.replace("User-Agent: ", "User-Agent: GGwlPlayer/QQ243944493\nReferer: http://flv.cntv.wscdns.com/\nUser-Name: ") ;
                        //把代理服务器端口改为原URL端口
                        if(TextUtils.isEmpty(original_ip_port))
                        	requestStr = requestStr.replace(":"+proxy_ip_port, "");
                        else
                        	requestStr = requestStr.replace(":"+proxy_ip_port, ":"+original_ip_port);

                        Log.e("to Media Server---->", requestStr);
                        //--------------------------------------  
                        //把MediaPlayer的请求发到网络服务器，代理服务器->网络服务器  
                        //--------------------------------------
                        sckServer = new Socket();  
                        sckServer.connect(address);  
                        Log.e(TAG,"..........remote Server connected..........");  
                        sckServer.getOutputStream().write(requestStr.getBytes());//发送MediaPlayer的请求  
                        //------------------------------------------------------  
                        //把网络服务器的反馈发到MediaPlayer，网络服务器->代理服务器->MediaPlayer  
                        //------------------------------------------------------
                        Log.e(TAG,"..........remote start to receive..........");
                        String responseStr = "";
                        boolean isCaptured=false;
                        while ((bytes_read = sckServer.getInputStream().read(remote_reply)) != -1) {
                        	byte[] tmpBuffer=new   byte[bytes_read]; 
                            System.arraycopy(remote_reply,0,tmpBuffer,0,bytes_read);
                            //----捕获收到的Response文本内容----//
							if (!isCaptured) {
								String str = new String(tmpBuffer);
								responseStr += str;
								if (responseStr.contains("HTTP/")  
	                                    && responseStr.contains(HTTP_END)) {
									
									int endIndex=responseStr.indexOf(HTTP_END, 0);
									responseStr=responseStr.substring(0, endIndex);
									Log.e("from Media Server---->", responseStr);
									isCaptured=true;
	                            }
							}
                            sckPlayer.getOutputStream().write(tmpBuffer);  
                            sckPlayer.getOutputStream().flush();
                        }
                        Log.e(TAG, "..........over.........."); 
                       
                        //关闭对内，对内 2个SOCKET
                        sckPlayer.close();  
                        sckServer.close();  
                    } catch (IOException e) {  
                        // TODO Auto-generated catch block  
                        e.printStackTrace();  
                    }  
                }  
            }  
        }.start();  
    }  
}  