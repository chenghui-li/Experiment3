package com.ziroom.demo.jdbc;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GetAllRoomImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String city_;
    public String state_;

    /*z
      筛选后获得房屋列表
     */
    public JSONArray getRooms(String city, String state,String type,float minPrice,float maxPrice){
        JSONArray jsonArray = new JSONArray();
        city_ = city;
        state_ = state;
        String str1 = "select * from ";
        String str2 = " where area = ? and roomtype = ? and price >= ? and price <= ?";
        Object []obj = new Object[]{state,type,minPrice,maxPrice};
        List<Map<String,Object> >list = new ArrayList();
        System.out.println(list.size());
        do {
            if(city.equals("shenzhen")){
                list = jdbcTemplate.queryForList(str1+"ShenZhenRoom"+str2,obj);
                break;
            }
            if(city.equals("shanghai")){
                list = jdbcTemplate.queryForList(str1+"ShangHaiRoom"+str2,obj);
                break;
            }
            if(city.equals("beijing")){
                list = jdbcTemplate.queryForList(str1+"BeiJingRoom"+str2,obj);
                break;
            }
            if(city.equals("guangzhou")){
                list = jdbcTemplate.queryForList(str1+"GuangZhouRoom"+str2,obj);
                break;
            }
        }while(false);
        for(int i = 0;i<list.size();i++){
            JSONObject jsonObject = new JSONObject();
            try {

                jsonObject.put("id",list.get(i).get("id").toString());
                jsonObject.put("area",list.get(i).get("area").toString());
                jsonObject.put("url",list.get(i).get("url").toString());
                jsonObject.put("name",list.get(i).get("name").toString());
                jsonObject.put("roomtype",list.get(i).get("roomtype").toString());
                jsonObject.put("price",list.get(i).get("price").toString());
                jsonObject.put("closemetro",list.get(i).get("closemetro").toString());
                jsonObject.put("fitmenttype",list.get(i).get("fitmenttype").toString());
                jsonObject.put("size",list.get(i).get("size").toString());
                jsonArray.add(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        return jsonArray;
    }


}

