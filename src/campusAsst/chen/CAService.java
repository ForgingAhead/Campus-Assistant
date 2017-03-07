package campusAsst.chen;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;


public class CAService extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			double longitude = Double.valueOf(request.getParameter("Longitude"));
			double latitude = Double.valueOf(request.getParameter("Latitude"));
			int userID = Integer.valueOf(request.getParameter("UserID"));
			System.out.println("Longitude:" + longitude + "UserID: " + userID);/////////////////////
			
			JSONObject result = getInfo(longitude,latitude,userID);//try to get updates of info
			if(result == null) {
				JSONObject re = new JSONObject();
				re.put("Status","failure");
				System.out.println(re.toString());///////////////////////////
				response.getWriter().write(re.toString());
			}else {				
				System.out.println(result.toString());///////////////////////////
				response.getWriter().write(result.toString());				
			}
		}
		catch(Exception e){e.printStackTrace();}
		}
	
	private JSONObject getInfo(double lng, double lat, int id) throws JSONException, SQLException, ClassNotFoundException {
		InfoProvider infoProvider = new InfoProvider();
		return infoProvider.getInfo(lng, lat, id);
	}
	

}
