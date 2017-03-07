package campusAsst.chen;

import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InfoProvider {

	public JSONObject getInfo(double lng, double lat, int id) throws JSONException, SQLException, ClassNotFoundException {
		DataManipulator.connectDB();
		int locID = DataManipulator.getLocID(lng, lat);
		System.out.println("LocID: " + locID);/////////////////////
		JSONObject result = new JSONObject();
		JSONArray services = getServices(id,locID);
		if(services.length() > 0) {
			result.put("Status", "success");
			result.put("ServiceInfo", services.toString());
		}
		else {
			result.put("Status", "failure");
			result.put("ServiceInfo", new String());
		}
		DataManipulator.close();
		return result;
	}
	
	private JSONArray getServices(int id, int locID) throws JSONException, SQLException {
		int i;
		JSONArray info = new JSONArray();	
		String keys[] = DataManipulator.getUserInterests(id);
		String major = (String) DataManipulator.getUserInfo(id).get("Major");
		System.out.println("major: " + major);///////////////////////
		String keywords[] = new String[keys.length+1];
		for(i=0;i<keys.length;i++)
			keywords[i] = keys[i];
		keywords[i] = major;
		int serviceIDs[] = DataManipulator.getServiceIDByKeyLoc(keywords, locID);
		for(i=0; i<serviceIDs.length; i++) {
			if(serviceIDs[i]!=0)
				info.put(DataManipulator.getServiceInfoByID(serviceIDs[i]));
		}
		return info;
	}


}
