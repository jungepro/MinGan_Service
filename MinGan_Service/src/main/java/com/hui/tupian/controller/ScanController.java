package com.hui.tupian.controller;

import com.alibaba.fastjson.JSONObject;
import com.hui.tupian.util.ImgCensor;
import com.hui.tupian.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/scan")
public class ScanController {

    public static String rootPath = "D:/pic";

    @GetMapping("doScan")
    public Result<?> doScan(@RequestParam("list") List<String> list) {
        System.out.println(list);
        List<JSONObject> objList = new ArrayList<>();
        for (String s : list) {
            String filePath = rootPath + File.separator + s;
            JSONObject obj = ImgCensor.ImgCensor(filePath);
            obj.put("img",s);
            objList.add(obj);
        }

        return Result.OK(objList);
    }


}
