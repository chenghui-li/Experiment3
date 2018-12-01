package com.ziroom.demo.Controller;

import com.ziroom.demo.jdbc.RoomInfoImpl;
import jxl.read.biff.BiffException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jxl.write.WriteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("roominfo")
public class RoomInfo {
    @Autowired
    public RoomInfoImpl roomInfo;

    @PostMapping("/query")
    public JSONArray query(
                           @RequestParam(value = "id") String id,
                           @RequestParam(value = "city") String city,
                           @RequestParam(value = "area") String state) throws IOException, BiffException, WriteException {
        return roomInfo.getRoomInfo(id,city,state);
        //return roomInfo.getRecommendList("aaa",3000);
    }
}
