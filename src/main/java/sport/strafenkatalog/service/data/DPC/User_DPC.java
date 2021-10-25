package sport.strafenkatalog.service.data.DPC;

import sport.strafenkatalog.service.metadata.MPC.User_MPC;

public class User_DPC extends DPC {

	private static final String TAB_NAME = "user";	
	
	public User_DPC() throws Exception {
		super(TAB_NAME);
		modelProvider = new User_MPC();
	}

}
