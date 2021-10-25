package sport.strafenkatalog.service.data.DPC;

import sport.strafenkatalog.service.metadata.MPC.Team_MPC;

public class Team_DPC extends DPC {

	private static final String TAB_NAME = "team";	
	
	public Team_DPC() throws Exception {
		super(TAB_NAME);
		modelProvider = new Team_MPC();
	}

}
