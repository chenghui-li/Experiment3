package com.ziroom.demo.Controller;
import com.ziroom.demo.jdbc.GetAllRoomImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
  附加推荐功能
 */
@RestController
@RequestMapping("GetAllRoom")
public class GetAllRoom {

    @Autowired
    public GetAllRoomImpl getAllRoom;

    //获取筛选后的数据
    @PostMapping("/all")
    public JSONArray getall(@RequestParam(value = "city") String city,
                            @RequestParam(value = "area") String area,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "minprice") float minprice,
                            @RequestParam(value = "maxprice") float maxprice){
        return getAllRoom.getRooms(city,area,type,minprice,maxprice);
    }

}
