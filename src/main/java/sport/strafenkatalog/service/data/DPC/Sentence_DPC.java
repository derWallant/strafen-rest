package sport.strafenkatalog.service.data.DPC;

import sport.strafenkatalog.service.metadata.MPC.Sentence_MPC;

public class Sentence_DPC extends DPC {

	private static final String TAB_NAME = "sentence";	
	
	public Sentence_DPC() throws Exception {
		super(TAB_NAME);
		modelProvider = new Sentence_MPC();
	}
}
