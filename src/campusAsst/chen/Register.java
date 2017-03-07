package campusAsst.chen;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Register extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

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
		String major = request.getParameter("Major");
		String gender = request.getParameter("Gender");
		String interests[] = request.getParameterValues("Interests");
		
		System.out.println("major:  "+major);
		System.out.println("gender: " + gender);
		System.out.println("length£º" + interests.length);
		try {
			DataManipulator.connectDB();
			int id = DataManipulator.createUser(major,gender,interests);
			if(id>0) {
				System.out.println("id is " + id);///////////////////////////
				response.getWriter().write(Integer.toString(id));
			}
			else {
				System.out.println("×¢²áÓÃ»§Ê§°Ü£¡");///////////////////////////
				response.getWriter().write("Register new user failed!");				
			}
			DataManipulator.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
