package campusAsst.chen;


import java.util.LinkedList;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataManipulator 
{
	private static Connection conn;
	private static Statement stmt;
	private static ResultSet rs;
    
	//connect to the oracle database
	public static void connectDB() throws ClassNotFoundException
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/campusAsst?useUnicode=true&characterEncoding=gb2312";
			String password = "3745";
			String username = "root";
			conn = DriverManager.getConnection(url, username, password);
			stmt = conn.createStatement();
		} catch (ClassNotFoundException e) {
			System.out.println("加载驱动失败，请检查驱动包是否正确放置！！");
		} catch (SQLException e) {
			System.out.println("数据库操作异常 ！！");
		}
	}
	
	public static String[] getUserInterests(int userID) {
		try
		{
			String sql = "select Interest from UserInterest where UserID = " + userID;
			rs = stmt.executeQuery( sql );	
		}catch( Exception e )
		{
			e.printStackTrace();
		} 
		return toStringArray(rs, "Interest");
	}
	
	private static String[] toStringArray(ResultSet rs, String columLable) {
	    LinkedList<String> resultList = new LinkedList<String>();	    
	    try {
	        while (rs.next()) {
	            resultList.add(rs.getString(columLable));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return resultList.toArray(new String[0]);
	}
	
	//store user interests
	public static void storeInterests(int id, String interest) {
		try
		{			
			String sql = "insert into UserInterest values(" + id + ","+"'" +interest+"')";		
		    stmt.executeUpdate(sql);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//get information
	public static JSONObject getUserInfo( int id )
	{
		JSONObject result = new JSONObject();
		try
		{
			String sql = "select * from UserInfo where UserID = " + id ;
			rs = stmt.executeQuery( sql );			
			while( rs.next() )
			{
				result.put("Major", new String(rs.getString("Major").getBytes("gb2312"),"GB2312"));
				result.put("Gender", rs.getString("Gender"));
			}

			System.out.println("in getUserInfo() result: "+result.toString());///////////////////////
			rs.close();
		}catch( Exception e )
		{
			e.printStackTrace();
		} 
		return result;
	}
	
	public static int getLocID(double lng, double lat) {
		try{
			String sql = "select * from LocationInfo";
			rs = stmt.executeQuery(sql);
			while(rs.next()) {
				if(rs.getDouble("MinLong")*1000<=lng*1000 && rs.getDouble("MaxLong")*1000>=lng*1000 
				&& rs.getDouble("MinLat")*1000<=lat*1000 && rs.getDouble("MaxLat")*1000>=lat*1000) {
					int i = rs.getInt("LocID");
					rs.close();
					return i;
				}				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static int[] getServiceIDByKeyLoc(String keywords[], int locID) {
		int services[] = new int[10];
		for(int k=0; k<10; k++)
			services[k]=0;
		try
		{
			int count=0;
			String sql = "select * from ServiceInfo where LocID=" + locID;
			rs = stmt.executeQuery( sql );
			System.out.println("in getServiceKeyByKeyLoc() rs result: "+rs.toString());////////////////////
			boolean flag = false;
			while( rs.next()) {
				int id = rs.getInt("ServiceID");
				System.out.println("in getServiceKeyByKeyLoc while loop, ServiceID by loc is: "+id);//////////////////////
				ResultSet r = stmt.executeQuery("select * from ServiceKey where ServiceID=" + id);
				while(r.next()) {
					for(int i=0; i<keywords.length; i++)
						if(keywords[i].compareToIgnoreCase(r.getString("Keyword"))==0){
							if(count<10)
								services[count++] = rs.getInt("ServiceID");
							System.out.println("serviceID selected: " + services[count-1]);/////////
							flag = true;
							break;
						}
					if(flag) break;
				}
				flag = false;
			}
			rs.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	return services;
	}
	
	public static int createUser(String major, String gender, String[] interests) {

		String sql = "insert UserInfo(Major,Gender) values('"+major+"','"+gender+"')";
		try {
			stmt.execute(sql, Statement.RETURN_GENERATED_KEYS );
			System.out.println("stmt.execute();newUser done....");
			String maxID = "select MAX(UserID) from UserInfo";
			rs = stmt.executeQuery(maxID);
			System.out.println(rs.toString());/////////////////////////
			int id = 0;
			while(rs.next())
				id = rs.getInt("MAX(UserID)");
			System.out.println("id " + id);/////////////////////////
			for(int i=0; i<interests.length; i++)
				storeInterests(id,interests[i]);
			System.out.println(id + " id. Create new user successfully here in DataManipulator!");/////////////
			return id; //返回UserID
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;			
	}
	
	public static String getLocation(int locID) throws SQLException {
		String sql = "select Name from LocationInfo where LocID="+locID;
		rs = stmt.executeQuery(sql);
		return rs.getString("Name");
	}
	
	public static JSONObject getServiceInfoByID(int serviceID) throws SQLException, JSONException {
		JSONObject result = new JSONObject();
		String sql = "select * from ServiceInfo where ServiceID="+serviceID;
		rs = stmt.executeQuery(sql);
		System.out.println("serviceID is : " + serviceID);//////////////////////////////
		int locID = 0;
		while(rs.next()) {
			locID = rs.getInt("LocID");
			String address = getLocation(locID);
			result.put("Address", address);
			result.put("Content", rs.getString("Content"));
			System.out.println("address: " + address);
			System.out.println("content: " + rs.getString("Content"));////////////////////////
		}
		rs.close();
		return result;
	}
	
	public void deleteOldInfo() {
		
	}
	
	public void addServiceInfo() {
		
	}
	
	public void addLocation() {
		
	}
	
	public static void close() {
		try {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			System.out.println("关闭连接异常！！");
		}
	}
}

/**
 * 将文件名中的汉字转化为UTF8编码的串，以便下载时能正确显示另存文件的名
 * @param s 原文件名
 * @return 重新编码后的文件名
*/

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>JavaScript获取页面停留时间并提交-1</title>
<script language=JavaScript>
var start = new Date();;
var end;
window.onbeforeunload = function(){
   end = new Date();
   var len = (end.getTime() - start.getTime()) / 1000;
   var img = new Image();
   img.src = "log.php?visitlength=" + len + "&visitpage=1.html";
}
</script>
</head>
<body>
<h2>JavaScript获取页面停留时间并提交-1</h2>
  <a href="1.html">JavaScript获取页面停留时间并提交-1</a><br /><br />
  <a href="2.html">JavaScript获取页面停留时间并提交-2</a><br /><br />
  <a href="http://witmax.cn">晴枫</a>
</body>
</html>
	
	
    }






