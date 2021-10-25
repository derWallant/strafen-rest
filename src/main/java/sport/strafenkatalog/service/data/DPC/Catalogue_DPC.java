package sport.strafenkatalog.service.data.DPC;

import sport.strafenkatalog.service.metadata.MPC.Catalogue_MPC;

public class Catalogue_DPC extends DPC {

	private static final String TAB_NAME = "catalogues";	
	
	public Catalogue_DPC() throws Exception {
		super(TAB_NAME);
		modelProvider = new Catalogue_MPC();
	}

}
