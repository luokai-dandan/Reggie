package com.itheima.reggie.controller;

import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传和下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
@Api(tags = "公共接口(上传下载图片)")
public class CommonController {

    //是否启用linux保存图片
    @Value("${custom-parameters.path.enable-linux}")
    private String enableLinux;

    @Value("${custom-parameters.path.save-image-path-linux}")
    private String saveImagePathLinux;

    //从配置文件中拿到文件上传后的保存路径
    @Value("${custom-parameters.path.save-image-path-windows}")
    private String saveImagePathWindows;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation(value = "图片上传接口")
    @ApiImplicitParam(name = "file", value = "文件")
    //参数file必须和前端的name一致，否则无法收到参数
    public R<String> upload(MultipartFile file) {
        //file是个临时文件，需要转存到指定位置，否则本次请求完成后文件会删除
//        log.info(file.toString());

        //上传图片保存路径
        String imagePath = null;
        if (enableLinux.equals("true")) {
            //使用linux路径
            imagePath = saveImagePathLinux;
        } else if (enableLinux.equals("false")){
            //使用Windows路径
            imagePath = saveImagePathWindows;
        } else {
            throw new CustomException("图片上传路径异常");
        }

        //原始文件名，通过字符串分割拿到后缀
        String originalFilename = file.getOriginalFilename();
        String fileNameSuffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + fileNameSuffix;
//        log.info(fileName);

        //判断指定目录是否存在，不存在则创建
        File dir = new File(imagePath);
        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            //将临时文件转存到指定位置
            file.transferTo(new File(imagePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(fileName);
    }

    @GetMapping("/download")
    @ApiOperation(value = "图片下载接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "图片文件名", required = true),
            @ApiImplicitParam(name = "response", value = "响应对象", required = true)
    })
    public void download(String name, HttpServletResponse response) {

        //上传图片保存路径
        String imagePath = null;
        if (enableLinux.equals("true")) {
            //使用linux路径
            imagePath = saveImagePathLinux;
        } else if (enableLinux.equals("false")){
            //使用Windows路径
            imagePath = saveImagePathWindows;
        } else {
            throw new CustomException("图片上传路径异常");
        }

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(imagePath + name));
            //输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
