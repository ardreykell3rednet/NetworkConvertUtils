package ij.macro;

	public interface Debugger {

		int NOT_DEBUGGING=0, STEP=1, TRACE=2, FAST_TRACE=3,
			RUN_TO_COMPLETION=4, RUN_TO_CARET=5;
		
		int debug(Interpreter interp, int mode);
			
}
