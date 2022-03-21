package us.betahouse.util.utils;

import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.enums.RestResultCode;
import us.betahouse.util.exceptions.BetahouseException;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 将文件放入response下载
 */
public class HttpDownloadUtil {

    /**
     * 适用于小文件的下载
     *
     * @param fileName 下载的文件名
     * @param inputStream 文件流
     * @param response 响应的response
     */
    public static void  downloadByInputStream(String fileName,InputStream inputStream, HttpServletResponse response){
        OutputStream outputStream =null;
        try {
            outputStream=new BufferedOutputStream(response.getOutputStream());
            // 清空response
            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            //attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            // 告知浏览器文件的大小
            response.addHeader("Content-Length", String.valueOf(inputStream.available()));
            response.setContentType("application/octet-stream");
            byte[] bytes=new byte[inputStream.available()];
            inputStream.read(bytes);
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            throw new BetahouseException(CommonResultCode.SYSTEM_ERROR.getCode(),"系统出错，无法返回文件");
        }finally {

            try {
                if(inputStream!=null) inputStream.close();
                if(outputStream!=null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void downloadByValue(String fileName,String value,HttpServletResponse response){
        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(value.getBytes());
        downloadByInputStream(fileName,byteArrayInputStream,response);
    }

    public static void downloadByTemplate(String fileName, String Template, List<String[]> params,String append, HttpServletResponse response){
        StringBuffer sb=new StringBuffer();
        for (int i = 0; i < params.size(); i++) {
            String format = MessageFormat.format("【" + (i + 1) + "】" + Template+"\n", params.get(i));
            sb.append(format);
        }
        if(append==null||append.equals("")){ }else {
            sb.append(append);
        }
        downloadByValue(fileName,sb.toString(),response);
    }


    /**
     * 将文件流打包进zip下载    可用channel再优化
     *
     * @author xxj
     * @param fileName zip文件名
     * @param response
     * @param objects 先文件名 后输入流 两两一对
     */
    public static void  downloadInputStreamZIP(String fileName,HttpServletResponse response,Object... objects){
        BufferedOutputStream bufferedOutputStream= null;
        ZipOutputStream zipOutputStream=null;
        BufferedInputStream bufferedInputStream=null;
        int volume=0;
        try {
            bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
            zipOutputStream=new ZipOutputStream(bufferedOutputStream);
            for (int i = 0; i < objects.length; i+=2) {
                String name=(String)objects[i];
                InputStream stream=(InputStream) objects[i+1];
                volume+=stream.available();
                zipOutputStream.putNextEntry(new ZipEntry(name));
                bufferedInputStream=new BufferedInputStream(stream);
                byte[] bytes=new byte[1024*4];
                int j=-1;
                while ((j=bufferedInputStream.read(bytes))!=-1){
                    zipOutputStream.write(bytes,0,j);
                }
            }
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.addHeader("Content-Length", String.valueOf(volume));
            response.setContentType("application/octet-stream");
        } catch (IOException e) {
            throw new BetahouseException("压缩文件写入错误！！！");
        }finally {
            try {
                if(zipOutputStream!=null) zipOutputStream.flush();
                if(zipOutputStream!=null) zipOutputStream.close();
                if(bufferedOutputStream!=null) bufferedOutputStream.close();
                if(bufferedInputStream!=null) bufferedInputStream.close();
            } catch (IOException e) {
                throw new BetahouseException(RestResultCode.SYSTEM_ERROR,"压缩文件流关闭错误！！！");
            }
        }
    }
}
