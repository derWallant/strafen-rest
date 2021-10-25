package sport.strafenkatalog.service.data.DPC;

import sport.strafenkatalog.service.metadata.MPC.Player_MPC;

public class Player_DPC extends DPC {

	private static final String TAB_NAME = "player";	
	
	public Player_DPC() throws Exception {
		super(TAB_NAME);
		modelProvider = new Player_MPC();
	}

}
