package sport.strafenkatalog.service.data.DPC;

import sport.strafenkatalog.service.metadata.MPC.Punishment_MPC;

public class Punishment_DPC extends DPC {

	private static final String TAB_NAME = "punishmentcatalogue";	
	
	public Punishment_DPC() throws Exception {
		super(TAB_NAME);
		modelProvider = new Punishment_MPC();
	}

}
