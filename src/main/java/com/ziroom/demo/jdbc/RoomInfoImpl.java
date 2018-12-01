package com.ziroom.demo.jdbc;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
@Service
public class RoomInfoImpl {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public Judge judge;

    @Autowired GetAllRoomImpl getAllRoom;
    int rows[][] = {{22,24215},{23,35006},{18,16022},{20,12407}};
    String filenames[] = {
            "/Users/chenghuili/Downloads/beijing_result.xls",
            "/Users/chenghuili/Downloads/shanghai_result.xls",
            "/Users/chenghuili/Downloads/guangzhou_result.xls",
            "/Users/chenghuili/Downloads/shenzhen_result.xls"};
    String text[] = {
            "/Users/chenghuili/Downloads/beijing.xls",
            "/Users/chenghuili/Downloads/shanghai.xls",
            "/Users/chenghuili/Downloads/guangzhou.xls",
            "/Users/chenghuili/Downloads/shenzhen.xls"
    };
    String bjstate[] = {"昌平区","东城区","西城区","海淀区","朝阳区","丰台区","门头沟区","石景山区","房山区","通州区","顺义区","大兴区","密云区","亦庄开发区"};
    String shstate[] = {"浦东区","徐汇区","长宁区","普陀区","闸北区","虹口区","杨浦区","黄浦区","静安区","宝山区","闵行区","嘉定区","松江区","青浦区","奉贤区"};
    String gzstate[] = {"越秀区","荔湾区","海珠区","天河区","白云区","黄埔区","番禺区","花都区","南沙区","增城区"};
    String szstate[] = {"福田区","罗湖区","南山区","盐田区","宝安区","龙岗区","坪山区","坪山新区","龙华区","龙华新区","光明新区","大鹏新区"};
    //public String city,state;
    public JSONArray res;
    public JSONArray getRoomInfo(String id,String city,String state) throws IOException, BiffException, WriteException {
        res = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        String tablename = "";
        do {
            if(city.equals("shenzhen")){
                tablename += "ShenZhenRoom";
                break;
            }
            if(city.equals("shanghai")){
                tablename += "ShangHaiRoom";
                break;
            }
            if(city.equals("beijing")){
                tablename += "BeiJingRoom";
                break;
            }
            if(city.equals("guangzhou")){
                tablename += "GuangZhouRoom";
                break;
            }
        }while(false);
        String roomtype = "";
        float price = 0;
        List<Map<String ,Object>> list = jdbcTemplate.queryForList("select * from "+tablename+" where id = ?",new Object[]{id});
        try {
            jsonObject.put("id",list.get(0).get("id").toString());
            jsonObject.put("area",list.get(0).get("area").toString());
            jsonObject.put("url",list.get(0).get("url").toString());
            jsonObject.put("name",list.get(0).get("name").toString());
            jsonObject.put("roomtype",list.get(0).get("roomtype").toString());
            jsonObject.put("price",list.get(0).get("price").toString());
            jsonObject.put("closemetro",list.get(0).get("closemetro").toString());
            jsonObject.put("fitmenttype",list.get(0).get("fitmenttype").toString());
            jsonObject.put("size",list.get(0).get("size").toString());
            roomtype = list.get(0).get("roomtype").toString();
            price = Float.parseFloat(list.get(0).get("price").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        res.add(jsonObject);
        return getRecommendList(city,state,roomtype,price);
    }
    /*
      根据房屋id，找出房屋标签，根据标签进行相关推荐
     */
    public JSONArray getRecommendList(String city,String state,String roomtype,float price) throws IOException, BiffException, WriteException {
        int num[];
        int len = 0;
        int whichfile = -1;
        String states[] = { };
        if(city.equals("beijing")){
            len = rows[0][0];
            states = bjstate;
            System.out.println(city+"\n"+state);
            whichfile = 0;
        }else if(city.equals("shanghai")){
            len = rows[1][0];
            states = shstate;
            whichfile = 1;
            System.out.println(city+"\n"+state);
        }else if(city.equals("guangzhou")){
            len = rows[2][0];
            states = gzstate;
            whichfile = 2;
            System.out.println(city+"\n"+state);
        }else if(city.equals("shenzhen")){
            len = rows[3][0];
            states = szstate;
            whichfile = 3;
            System.out.println(city+"\n"+state);
        }
        num = new int[len];
        for(int i = 0;i<len;i++){
            num[i] = 0;
        }
        for(int i = 0;i<states.length;i++){
            if(states[i].equals(state)){
                num[i] = 1;
                break;
            }
        }
        int beg = states.length-1;
        if(roomtype.equals("整租")){
            num[beg+1] = 1;
        }else if(roomtype.equals("合租")){
            num[beg+2] = 1;
        }else if(roomtype.equals("单间")){
            num[beg+3] = 1;
        }
        if(price < 1000){
            num[beg+4] = 1;
        }else if(price >= 1000 && price <2000){
            num[beg+5] = 1;
        }else if(price >= 2000 && price <3000){
            num[beg+6] = 1;
        }else if(price >= 3000 && price < 4000){
            num[beg+7] = 1;
        }else if(price >=4000){
            num[beg+8] = 1;
        }
        PriorityQueue<Judge> minHeap = new PriorityQueue<>(11, new Comparator<Judge>() {
            @Override
            public int compare(Judge o1, Judge o2) {
                return o1.sum.compareTo(o2.sum);
            }
        });
        Sheet sheet = getSheet(text[whichfile]);
        for(int i = 0;i<sheet.getRows();i++){
            int sum = 0;
            int nownum[] = new int[len];
            for(int j = 0;j<len;j++){
                String now = sheet.getCell(j,i).getContents();
                sum = sum + num[j]*Integer.parseInt(now);
                nownum[j] = Integer.parseInt(now);
            }
            Judge now = new Judge();
            now.id = Integer.toString(i);
            now.sum = sum;
            now.num1 = num;
            now.num2 = nownum;
            if(minHeap.size()<10){
                minHeap.add(now);
            }else{
                if(sum > minHeap.peek().sum) {
                    minHeap.poll();
                    minHeap.add(now);
                }
            }
        }
        sheet = getSheet(filenames[whichfile]);
        while(minHeap.size()>0){
            Judge now = minHeap.poll();
            int id = Integer.parseInt(now.id)+1;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("area",sheet.getCell(0,id).getContents());
            jsonObject.put("url",sheet.getCell(1,id).getContents());
            jsonObject.put("name",sheet.getCell(2,id).getContents());
            jsonObject.put("roomtype",sheet.getCell(3,id).getContents());
            jsonObject.put("price",sheet.getCell(4,id).getContents());
            jsonObject.put("closemetro",sheet.getCell(5,id).getContents());
            jsonObject.put("fitmenttype",sheet.getCell(6,id).getContents());
            jsonObject.put("size",sheet.getCell(7,id).getContents());
            jsonObject.put("id",String.valueOf(id));
            jsonObject.put("sum",now.sum);
            for(int j = 0;j<len;j++){
                System.out.print(now.num1[j]+" ");
            }
            System.out.println();
            for(int j = 0;j<len;j++){
                System.out.print(now.num2[j]+" ");
            }
            System.out.println();
            System.out.println();
            res.add(jsonObject);
        }
        //init();

        return res;
    }
    Sheet getSheet(String filename) throws IOException, BiffException {
        File file = new File(filename);
        // 创建输入流，读取Excel
        InputStream is = new FileInputStream(file.getAbsolutePath());
        // jxl提供的Workbook类
        Workbook wb = Workbook.getWorkbook(is);
        Sheet sheet = wb.getSheet(0);
        return sheet;
    }
    WritableSheet getwrite(String filename) throws IOException {
        WritableWorkbook book = Workbook.createWorkbook(new File(filename));
        WritableSheet sheet = book.createSheet("sheet1", 0);
        return sheet;
    }
    public void init(String city,String state) throws IOException, BiffException, WriteException {
        city = getAllRoom.city_;
        state = getAllRoom.state_;


        for (int i = 0;i<4;i++) {
            String openfilename = filenames[i];
            Sheet sheet = getSheet(openfilename);
            int row = rows[i][0];
            int col = rows[i][1];
            int dphash[][] = new int[row][col];
            for(int j1 = 0 ;j1<row;j1++){
                for(int j2 = 0 ;j2<col;j2++){
                    dphash[j1][j2] = 0;
                }
            }
            String nowcity[] = {};
            switch (i){
                case 0:
                    nowcity = bjstate;
                    break;
                case 1:
                    nowcity = shstate;
                    break;
                case 2:
                    nowcity = gzstate;
                    break;
                case 3:
                    nowcity = szstate;
                    break;

            }
            for(int j = 1;j<sheet.getRows();j++){
                //区
                String stateInfo = sheet.getCell(0,j).getContents();
                for(int index = 0;index<nowcity.length;index++){
                    if(stateInfo.equals(nowcity[index])){
                        dphash[index][j-1] = 5;   //区初始化
                        break;
                    }
                }
                int beg = nowcity.length-1;
                //房屋类型
                String roomType = sheet.getCell(3,j).getContents();
                if(roomType.equals("整租")){
                    dphash[beg+1][j-1] = 1;
                }else if(roomType.equals("合租")){
                    dphash[beg+2][j-1] = 1;
                }else if(roomType.equals("单间")){
                    dphash[beg+3][j-1] = 1;
                }
                //价格范围
                String price = sheet.getCell(4,j).getContents();
                //System.out.println(roomType);
                float priceint = Float.parseFloat(price);
                if(priceint<1000){
                    dphash[beg+4][j-1] = 1;
                }else if(priceint>=1000 && priceint<2000){
                    dphash[beg+5][j-1] = 1;
                }else if(priceint >=2000 && priceint < 3000){
                    dphash[beg+6][j-1] = 1;
                }else if(priceint >= 3000 && priceint <4000){
                    dphash[beg+7][j-1] = 1;
                }else if(priceint >=4000){
                    dphash[beg+8][j-1] = 1;
                }

            }
            WritableWorkbook book = Workbook.createWorkbook(new File(text[i]));
            WritableSheet writableSheet = book.createSheet("sheet1", 0);
            for(int m = 0;m<row;m++){
                for(int n = 0;n<col;n++){
                    Number number = new Number(m,n,dphash[m][n]);
                    writableSheet.addCell(number);
                }

            }
            book.write();
            book.close();
        }


    }

}
